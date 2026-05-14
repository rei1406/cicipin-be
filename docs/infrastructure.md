# Infrastructure

## Docker Compose

The project ships two compose files:

| File | Purpose |
|---|---|
| `docker-compose.yml` | Production — builds fat JARs, no source mounts |
| `docker-compose.dev.yml` | Development — mounts source for hot-reload, uses `mvn spring-boot:run` |

### Services

| Container | Image / Build | Ports | Depends On |
|---|---|---|---|
| `cicipin-user-service-db` | `postgres:18.3-alpine` | `7001:7001` | — |
| `cicipin-user-service` | Built from `user-service/Dockerfile[.dev]` | `8081:8081` | `user-service-db` (healthy) |
| `cicipin-email-service` | Built from `email-service/Dockerfile[.dev]` | `8082:8082` | — |
| `cicipin-api-gateway` | Built from `api-gateway/Dockerfile[.dev]` | `8080:8080` | `user-service`, `email-service` |

### Volumes

| Volume | Used By | Purpose |
|---|---|---|
| `cicipin-user-service-db-data` | `user-service-db` | Persists PostgreSQL data |
| `cicipin-maven-cache` | All services (dev only) | Caches Maven dependencies across rebuilds |

### Health Check

The `user-service-db` container has a health check:
```bash
pg_isready -U ${USER_SERVICE_DB_USER} -d ${USER_SERVICE_DB_NAME} -p ${USER_SERVICE_DB_CONTAINER_PORT}
```
Interval: 10s, timeout: 5s, retries: 5. The `user-service` container only starts after the DB is healthy.

---

## Environment Variables

### Production (`.env`) / Development (`.env.dev`)

Copy `.env.example` to `.env` and `.env.dev` and fill in real values. Never commit either file.

| Variable | Example Value | Description |
|---|---|---|
| `USER_SERVICE_DB_USER` | `cicipin` | PostgreSQL username |
| `USER_SERVICE_DB_PASSWORD` | `change_me` | PostgreSQL password |
| `USER_SERVICE_DB_NAME` | `user_db` | Database name |
| `USER_SERVICE_DB_HOST_PORT` | `7001` | DB port exposed on host |
| `USER_SERVICE_DB_CONTAINER_PORT` | `7001` | DB port inside container (also `PGPORT`) |
| `USER_SERVICE_HOST_PORT` | `8081` | User service port on host |
| `USER_SERVICE_CONTAINER_PORT` | `8081` | User service port inside container |
| `API_GATEWAY_HOST_PORT` | `8080` | Gateway port on host |
| `API_GATEWAY_CONTAINER_PORT` | `8080` | Gateway port inside container |
| `EMAIL_SERVICE_HOST_PORT` | `8082` | Email service port on host |
| `EMAIL_SERVICE_CONTAINER_PORT` | `8082` | Email service port inside container |
| `MAIL_HOST` | `smtp.gmail.com` | SMTP server hostname |
| `MAIL_PORT` | `587` | SMTP port (STARTTLS) |
| `MAIL_USERNAME` | `your-email@gmail.com` | SMTP login / Gmail address |
| `MAIL_PASSWORD` | `xxxx xxxx xxxx xxxx` | Gmail App Password (16 chars, not your account password) |
| `MAIL_FROM` | `your-email@gmail.com` | Sender address shown in "From" field |
| `APP_NAME` | `Cicipin` | Application name used in email templates |

### Development (`.env.dev`)

Same variables as `.env`. The compose file additionally injects `SPRING_PROFILES_ACTIVE=dev` for all services.

---

## Dockerfiles

### Production Build (multi-stage)

Both `user-service/Dockerfile` and `api-gateway/Dockerfile` follow the same pattern:

**Stage 1 — Builder** (`eclipse-temurin:25-jdk-alpine`):
1. Copy `pom.xml` files
2. Copy source
3. Run `mvn clean package -DskipTests` to produce a fat JAR

**Stage 2 — Runtime** (`eclipse-temurin:25-jre-alpine`):
1. Copy the fat JAR from the builder stage
2. `EXPOSE` the container port (passed as build arg)
3. `ENTRYPOINT ["java", "-jar", "app.jar"]`

The JRE-only runtime image keeps the final image smaller.

### Development Build (single-stage)

Both `Dockerfile.dev` files use `eclipse-temurin:25-jdk-alpine`:
1. Install Maven via `apk`
2. Copy `pom.xml` and source
3. `CMD ["mvn", "spring-boot:run", ...]`

The compose file mounts the local `src/` directory into the container, so code changes are reflected without rebuilding the image. The user-service dev command also passes `-Dspring-boot.run.addResources=true` for resource hot-reload.

---

## `dev.sh`

A thin shell wrapper around `docker compose -f docker-compose.dev.yml`:

```bash
./dev.sh up --build      # Start dev environment
./dev.sh down            # Stop dev environment
./dev.sh logs -f         # Follow logs
./dev.sh restart user-service  # Restart a single service
```

---

## Application Configuration

### User Service (`application.yml`)

```yaml
server:
  port: ${USER_SERVICE_CONTAINER_PORT}

spring:
  datasource:
    url: jdbc:postgresql://cicipin-user-service-db:${USER_SERVICE_DB_CONTAINER_PORT}/${USER_SERVICE_DB_NAME}
    driver-class-name: org.postgresql.Driver
  flyway:
    enabled: true
    locations: classpath:db/migration
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

management:
  endpoints:
    web:
      exposure:
        include: health
```

### API Gateway (`application.yml`)

```yaml
server:
  port: ${API_GATEWAY_CONTAINER_PORT}

spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: user-service
              uri: http://cicipin-user-service:${USER_SERVICE_CONTAINER_PORT}
              predicates:
                - Path=/user/**
              filters:
                - StripPrefix=0
            - id: user-service-health
              uri: http://cicipin-user-service:${USER_SERVICE_CONTAINER_PORT}
              predicates:
                - Path=/user-service-health
              filters:
                - StripPrefix=0
            - id: email-service-health
              uri: http://cicipin-email-service:${EMAIL_SERVICE_CONTAINER_PORT}
              predicates:
                - Path=/email-service-health
              filters:
                - StripPrefix=0
```

### Email Service (`application.yml`)

```yaml
server:
  port: ${EMAIL_SERVICE_CONTAINER_PORT}

spring:
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    from: ${MAIL_FROM}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true

  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML
    cache: true   # set to false in dev profile if needed

app:
  name: ${APP_NAME:Cicipin}
```
