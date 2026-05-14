# LLM Context — Cicipin Backend

> This file is the single best starting point for an LLM working on this codebase. Read this first, then read specific files as needed.

---

## What This Project Is

**Cicipin** is a food delivery platform backend. It is a **Java 25 / Spring Boot 4.0.6 microservices** project using Maven multi-module. The project is early-stage: infrastructure is complete, but most business logic is scaffolded with TODO comments.

---

## Repository Layout

```
cicipin/
├── pom.xml                         ← Maven parent (modules: user-service, api-gateway)
├── docker-compose.yml              ← Production Docker Compose
├── docker-compose.dev.yml          ← Dev Docker Compose (hot-reload via volume mounts)
├── dev.sh                          ← Shortcut: docker compose -f docker-compose.dev.yml
├── .env                            ← Production env vars
├── .env.dev                        ← Dev env vars
├── api-gateway/                    ← Spring Cloud Gateway (reactive, WebFlux)
│   ├── pom.xml
│   └── src/main/
│       ├── java/.../ApiGatewayApplication.java   ← Entry point only, no custom code
│       └── resources/application.yml             ← ALL routing config is here
└── user-service/                   ← User & auth management (Spring MVC, JPA)
    ├── pom.xml
    └── src/main/
        ├── java/com/cicipin/userservice/
        │   ├── UserServiceApplication.java
        │   ├── auth/
        │   │   ├── AuthController.java       ← @ApiVersion(1), /api/auth — all TODO
        │   │   ├── AuthService.java          ← Interface — all TODO
        │   │   ├── AuthServiceImpl.java      ← Implementation — all TODO
        │   │   └── dto/                      ← EMPTY — no DTOs yet
        │   ├── user/
        │   │   ├── UserController.java       ← /api/users — all TODO
        │   │   ├── UserService.java          ← Interface — all TODO
        │   │   ├── UserServiceImpl.java      ← Implementation — all TODO
        │   │   └── UserRepository.java       ← JPA repo, 4 custom query methods
        │   └── common/
        │       ├── HealthController.java     ← GET /user-service-health (IMPLEMENTED)
        │       ├── model/
        │       │   ├── User.java             ← JPA entity (IMPLEMENTED)
        │       │   └── UserRole.java         ← Enum: CUSTOMER, ADMIN, DELIVERY_PARTNER, RESTAURANT_OWNER
        │       └── versioning/               ← Custom X-API-Version header versioning (IMPLEMENTED)
        │           ├── ApiVersion.java
        │           ├── ApiVersionCondition.java
        │           ├── VersionRequestMappingHandlerMapping.java
        │           └── WebMvcConfig.java
        └── resources/
            ├── application.yml
            └── db/migration/
                └── V1__create_users_table.sql   ← Creates users table + user_role enum
```

---

## Key Facts for Code Generation

### Tech Stack
- Java 25, Spring Boot 4.0.6, Spring Cloud 2025.1.1
- Spring MVC (not WebFlux) in user-service
- Spring Cloud Gateway WebFlux in api-gateway
- PostgreSQL 18.3, Flyway for migrations, JPA `ddl-auto: validate`
- Lombok (`@Data`, `@Builder`, `@RequiredArgsConstructor`, etc.)
- No Spring Security configured yet

### Dependency Injection Pattern
Always inject the **interface**, not the implementation. Use `@RequiredArgsConstructor` (Lombok) for constructor injection:

```java
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
}
```

### API Versioning
Controllers use `@ApiVersion(n)` annotation. The `X-API-Version` header is read from requests (defaults to 1). Only `AuthController` currently uses it. When adding new controllers, decide whether to add `@ApiVersion`.

```java
@ApiVersion(1)
@RestController
@RequestMapping("/api/auth")
public class AuthController { ... }
```

### User Entity Fields
`id` (UUID), `username` (unique), `name`, `email` (unique), `password`, `role` (UserRole enum), `isVerified`, `isActive`, `photo`, `createdAt`, `updatedAt`

### UserRepository Methods Available
- `findByEmail(String)` → `Optional<User>`
- `findByUsername(String)` → `Optional<User>`
- `existsByEmail(String)` → `boolean`
- `existsByUsername(String)` → `boolean`
- All standard `JpaRepository` methods

### Gateway Routing
The gateway routes `/user/**` to the user-service. So a user-service endpoint at `/api/auth/login` is reachable externally at `/user/api/auth/login`.

---

## What Needs to Be Built (TODO)

### Immediate next steps (in order):

1. **DTOs** — Create request/response classes in `user-service/src/main/java/com/cicipin/userservice/auth/dto/`:
   - `RegisterRequest` (username, name, email, password, role)
   - `LoginRequest` (email/username, password)
   - `LoginResponse` (accessToken, refreshToken, user info)
   - `RegisterResponse`

2. **AuthServiceImpl** — Implement:
   - `register()` — hash password, save user, send verification email
   - `login()` — validate credentials, issue JWT
   - `logout()` — invalidate token
   - `refreshToken()` — issue new access token
   - `verifyEmail()` — mark `isVerified=true`

3. **AuthController** — Wire up the 5 POST endpoints

4. **Spring Security + JWT** — Add `spring-boot-starter-security` and JWT library to `user-service/pom.xml`

5. **UserServiceImpl** — Implement user CRUD methods

6. **UserController** — Wire up the 5 user endpoints

---

## Conventions to Follow

- New service methods go in the interface first, then the `*Impl` class
- New DB columns/tables → new Flyway migration file (`V{n}__{description}.sql`)
- DTOs go in the feature's `dto/` subpackage
- Use `ResponseEntity<T>` as controller return type
- Use `@Valid` on `@RequestBody` parameters
- Use `@RestControllerAdvice` for global exception handling (not yet created)
- Passwords must be hashed (bcrypt) — never store plaintext

---

## Running Locally

```bash
./dev.sh up --build    # start dev environment with hot-reload
```

Health check: `curl http://localhost:8080/user-service-health`

---

## Ports

| Service | Host Port |
|---|---|
| API Gateway | 8080 |
| User Service | 8081 |
| PostgreSQL | 7001 |
