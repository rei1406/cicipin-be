# Cicipin Backend — Agent Guide

## Stack & Versions
- Java 25, Spring Boot 4.0.6, Spring Cloud 2025.1.1
- PostgreSQL 18.3, Flyway (`ddl-auto: validate`), Redis 7.4, Kafka (KRaft, no ZK)
- Lombok everywhere (`@Data`, `@Builder`, `@RequiredArgsConstructor`)
- JWT via jjwt 0.12.6, BCrypt via `spring-security-crypto`

## Services (3 independent Maven modules, no root pom)

| Service | Framework | Entrypoint | Port |
|---|---|---|---|
| `api-gateway/` | Spring Cloud Gateway (WebFlux/Netty) | `com.cicipin.gateway.ApiGatewayApplication` | 8080 |
| `user-service/` | Spring MVC (Tomcat/JPA) | `com.cicipin.userservice.UserServiceApplication` | 8081 |
| `email-service/` | Spring MVC (Tomcat/Thymeleaf) | `com.cicipin.emailservice.EmailServiceApplication` | 8082 |

Each service has its own `pom.xml`, `Dockerfile`, and `Dockerfile.dev`. Build independently:
```
./dev.sh up --build   # docker compose -f docker-compose.dev.yml up --build
```

## Dev / Test Environment
**Everything runs inside Docker.** No local Maven or JDK needed.

```bash
cp .env.example .env.dev   # fill in secrets
./dev.sh up --build         # start all services with hot-reload
```

- Mailpit at `localhost:8025` (Web UI) / `localhost:1025` (SMTP) for email testing
- Hot-reload via volume-mounted `mvn spring-boot:run` inside each dev container
- Health: `curl localhost:8080/user-service-health`

Run commands inside a service container:
```bash
./dev.sh exec user-service mvn test              # run tests
./dev.sh exec user-service mvn clean package -DskipTests   # build JAR
```

## Gateway Routing (api-gateway/src/main/resources/application.yml)
- `/api/auth/**` → user-service:8081 (permitAll)
- `/api/users/**` → user-service:8081 (requires auth, admin for listing/role mgmt)
- `/user/**` → user-service:8081 (legacy)
- Forwarded headers: `X-User-Id`, `X-User-Role` injected by `JwtHeaderFilter`

## Gateway Security
- Reactive Spring Security (`@EnableWebFluxSecurity`)
- Bearer JWT extracted by `JwtSecurityContextRepository`, validated by `JwtAuthenticationManager` → `JwtProvider`
- Auth routes `permitAll()`, user routes authenticated, admin routes require `ROLE_ADMIN`

## Auth Flow (user-service)
7 POST endpoints under `/api/auth`: `register`, `login`, `verify-email`, `resend-otp`, `forgot-password`, `verify-otp`, `reset-password`
- OTP stored in Redis with TTL; rate-limited (3 attempts → 10min block)
- Login rate-limited per email (3 failed → 10min block)
- On register/resend-otp/forgot-password: Kafka event → email-service sends email

## Kafka Event Flow (4 topics)
| Topic | Producer (user-service) | Consumer (email-service) | Email |
|---|---|---|---|
| `user-registered` | `UserEventProducer` | `UserRegisteredEventConsumer` | OTP_VERIFICATION |
| `user-resend-otp` | `UserEventProducer` | `UserResendOtpEventConsumer` | OTP_VERIFICATION |
| `user-forgot-password` | `UserEventProducer` | `UserForgotPasswordEventConsumer` | PASSWORD_RESET |
| `user-verified` | `UserEventProducer` | `UserVerifiedEventConsumer` | WELCOME |

## API Versioning (user-service)
Custom `@ApiVersion(n)` annotation reads `X-API-Version` header (defaults to 1). See `common/versioning/`.

## Environment Configuration
All ports and secrets come from env files — never hardcode them:
- `.env.example` — template with dummy values (the source of truth for variable names)
- `.env` — production values (git-ignored)
- `.env.dev` — dev values (git-ignored)
- **All three must stay synced** — same set of variable names in each

## Testing
```bash
./dev.sh exec user-service mvn test
./dev.sh exec api-gateway mvn test
./dev.sh exec email-service mvn test
```
- email-service: tests tagged `gmail` excluded by default (use real SMTP); Mailpit integration tests run by default
- Test framework: JUnit 5 + Mockito (`spring-boot-starter-test`)
- No linter/typechecker or CI configured

## Architecture & Conventions

### Spring Opinionated Stack
This is a Spring Boot project — follow standard Java/Spring ecosystem conventions. Do not treat it as an unopinionated or free-form framework. Use idiomatic Spring patterns: dependency injection via constructor (`@RequiredArgsConstructor`), `@RestController` for API endpoints, `@Service` for business logic, `@Repository` for data access, and `ResponseEntity<T>` as return type.

### Layered Architecture
Every service follows a layered structure. The exact subpackages may vary by service (feature-based like `auth/`, `user/` or layer-based like `controller/`, `service/`), but these layers must always be present:
- **controller** — request handling (`@RestController`, `@RequestMapping`)
- **service** — business logic (interface + `*Impl`)
- **repository** — data access (JPA `@Repository`, DAO, or Kafka consumer)
- **dto** — request/response classes
- **model / entity** — domain models, JPA entities, enums
- **config** — bean definitions (`@Configuration`, `@Bean`)

### API Response Standard
All services **must** use the same `ApiResponse<T>` wrapper for every API response:

```java
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private int status;
    private T data;
    private Object errors;
}
```
Three static factories are available: `ApiResponse.success(HttpStatus, T, String)`, `ApiResponse.error(HttpStatus, String)`, and `ApiResponse.validationError(Map<String, String>)`. Copy this class into each new service's `common/dto/` package.

### Other Conventions
- Service interfaces → `*Impl` classes; inject interface with `@RequiredArgsConstructor`
- `@Valid` on all `@RequestBody` params
- New DB columns → Flyway migration `V{n}__{description}.sql` in `db/migration/`
- Global exception handling via `@RestControllerAdvice` (`GlobalExceptionHandler`)
- User entity fields: `id` (UUID), `username`, `name`, `email`, `password`, `role` (enum), `isVerified`, `isActive`, `photo`, timestamps

## Important Gotchas
- **`docs/` is stale** — auth, JWT, Spring Security, Kafka, Redis OTP are all implemented, not TODO
- No root `pom.xml` (despite docs claiming multi-module); each service is standalone
- Docker internal hostnames: `cicipin-user-service`, `cicipin-email-service`, `cicipin-kafka`, `cicipin-redis`
- DB port externally is `7001` (not 5432); README shows different values than `.env.example`
- `.env` and `.env.dev` are git-ignored; use `.env.example` as template
- JWT secret must be base64-encoded
