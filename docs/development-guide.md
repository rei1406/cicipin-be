# Development Guide

## Prerequisites

- Docker Desktop (with Compose v2)
- Java 25 (for local builds outside Docker)
- Maven 3.x (for local builds outside Docker)

---

## Running the Project

### Development Mode (recommended)

Uses `docker-compose.dev.yml`. Source directories are mounted as volumes so code changes are picked up without rebuilding the image.

```bash
# First run — build images and start all services
./dev.sh up --build

# Subsequent runs (no code changes to Dockerfile)
./dev.sh up

# Stop everything
./dev.sh down

# Follow logs for all services
./dev.sh logs -f

# Follow logs for a specific service
./dev.sh logs -f user-service
```

The `dev.sh` script is just a shortcut for `docker compose -f docker-compose.dev.yml`.

### Production Mode

```bash
# Build and start
docker compose up --build

# Stop
docker compose down

# Stop and remove volumes (wipes the database)
docker compose down -v
```

---

## Environment Setup

Copy the example env file and adjust values if needed:

```bash
# For development
cp .env .env.dev
```

The `.env.dev` file is used by `docker-compose.dev.yml`. The `.env` file is used by `docker-compose.yml`.

Key variables to check:

```bash
USER_SERVICE_DB_USER=cicipin
USER_SERVICE_DB_PASSWORD=c1c1p1n123#
USER_SERVICE_DB_NAME=user_db
USER_SERVICE_DB_HOST_PORT=7001
USER_SERVICE_DB_CONTAINER_PORT=7001
USER_SERVICE_HOST_PORT=8081
USER_SERVICE_CONTAINER_PORT=8081
API_GATEWAY_HOST_PORT=8080
API_GATEWAY_CONTAINER_PORT=8080
```

---

## Verifying the Setup

After starting, check these endpoints:

```bash
# Gateway health
curl http://localhost:8080/actuator/health

# User service health (via gateway)
curl http://localhost:8080/user-service-health

# User service health (direct)
curl http://localhost:8081/user-service-health
```

Expected response for health:
```json
{ "status": "UP", "service": "user-service", "message": "Service is running" }
```

---

## Adding a New Endpoint

1. **Define the method in the service interface** (`AuthService.java` or `UserService.java`)
2. **Implement it in the `*Impl` class** (`AuthServiceImpl.java` or `UserServiceImpl.java`)
3. **Add the route in the controller** (`AuthController.java` or `UserController.java`)
4. **Create DTOs** in the `auth/dto/` package (request and response classes)

Example pattern for a new auth endpoint:

```java
// 1. Interface
public interface AuthService {
    LoginResponse login(LoginRequest request);
}

// 2. Implementation
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;

    @Override
    public LoginResponse login(LoginRequest request) {
        // implementation
    }
}

// 3. Controller
@ApiVersion(1)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
```

---

## Adding a Database Migration

Create a new SQL file in `user-service/src/main/resources/db/migration/`:

```
V{next_version}__{snake_case_description}.sql
```

Example: `V2__add_phone_column_to_users.sql`

```sql
ALTER TABLE users ADD COLUMN phone VARCHAR(20);
```

Flyway applies migrations automatically on startup. **Never edit an existing migration file** — always create a new version.

---

## Adding a New Microservice

1. Create a new Maven module directory (e.g., `restaurant-service/`)
2. Add a `pom.xml` with parent `com.cicipin:cicipin:1.0.0`
3. Register the module in the root `pom.xml` `<modules>` section
4. Add a `Dockerfile` and `Dockerfile.dev`
5. Add the service to `docker-compose.yml` and `docker-compose.dev.yml`
6. Add a route in `api-gateway/src/main/resources/application.yml`

---

## Building Without Docker

```bash
# Build all modules
./mvnw clean package -DskipTests

# Build a specific module
./mvnw -pl user-service -am clean package -DskipTests

# Run user-service locally (requires a running PostgreSQL instance)
./mvnw -pl user-service spring-boot:run
```

You'll need to set the environment variables that `application.yml` references, or override them:

```bash
USER_SERVICE_CONTAINER_PORT=8081 \
USER_SERVICE_DB_CONTAINER_PORT=5432 \
USER_SERVICE_DB_NAME=user_db \
USER_SERVICE_DB_USER=cicipin \
USER_SERVICE_DB_PASSWORD=cicipin123 \
./mvnw -pl user-service spring-boot:run
```

---

## Project Conventions

- **Package structure**: `com.cicipin.{service-name}.{feature}/` — each feature (auth, user) is a self-contained package with its own controller, service interface, implementation, and DTOs.
- **Service layer**: Always program to the interface (`AuthService`, not `AuthServiceImpl`). Controllers inject the interface.
- **Lombok**: Use `@RequiredArgsConstructor` for constructor injection. Use `@Data`, `@Builder` on entities and DTOs.
- **API versioning**: Annotate controllers with `@ApiVersion(n)`. See [api-versioning.md](./api-versioning.md).
- **Database**: All schema changes go through Flyway migrations. Never use `ddl-auto: create` or `update`.
