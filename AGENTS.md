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

## Seeding (user-service)
Profile-based `CommandLineRunner` in `common/seed/SeedRunner.java` — seeds an admin user (`admin` / `admin123`) if none exists, then exits.
```bash
./dev.sh seed
```
This runs a one-off container with `SPRING_PROFILES_ACTIVE=seed`. Idempotent — skips if admin already exists.

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

## i18n / Internationalization

Every service implements its own independent i18n setup — no shared/common i18n module.

### Supported Locales
- English (`en`, default) — `messages.properties`
- Indonesian (`id`) — `messages_id.properties`

### Locale Resolution
The `X-Locale` request header drives locale selection. If absent, falls back to `Accept-Language` header, then to `Locale.ENGLISH`.

Each service configures a custom `AcceptHeaderLocaleResolver` (via `WebMvcConfig` or `I18nConfig`):

```java
AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver() {
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String lang = request.getHeader("X-Locale");
        if (lang != null && !lang.isEmpty()) {
            return Locale.forLanguageTag(lang);
        }
        return super.resolveLocale(request);
    }
};
resolver.setSupportedLocales(List.of(Locale.of("en"), Locale.of("id")));
resolver.setDefaultLocale(Locale.ENGLISH);
```

The resolved locale is stored in `LocaleContextHolder` and accessed anywhere via `LocaleContextHolder.getLocale()`.

### MessageSource Configuration
- `MessageSource` bean with basename `"messages"`, UTF-8 encoding, no system locale fallback
- `LocalValidatorFactoryBean` registered using this `MessageSource` for `@Valid` validation messages
- Validation error codes in properties files match Spring's auto-generated codes (e.g. `auth.register.username.required` for `@NotBlank` on `RegisterRequest.username`)

### Message Properties Files
Each service has its own properties files under `src/main/resources/`:

| File | Content |
|---|---|
| `messages.properties` | Default locale (English) |
| `messages_id.properties` | Indonesian translations |

Message key naming conventions:
- `auth.*` — auth flow messages
- `user.*` — user CRUD messages
- `error.*` — error messages
- `success.*` — success messages
- `email.*` — email-related messages
- Placeholders use `{0}`, `{1}` for argument substitution

### How to Access Localized Messages

**Controllers** — inject `MessageSource` and use a private `resolve()` helper:

```java
@RequiredArgsConstructor
@RestController
public class SomeController {
    private final MessageSource messageSource;

    private String resolve(String code) {
        try {
            return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return code;
        }
    }

    public ResponseEntity<ApiResponse<?>> someEndpoint() {
        return ApiResponse.success(HttpStatus.OK, data, resolve("success.some.code"));
    }
}
```

**GlobalExceptionHandler** — same injection pattern, with an overloaded `resolve` for args:

```java
private String resolve(String code, Object[] args, String fallback) {
    try {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    } catch (NoSuchMessageException e) {
        return fallback;
    }
}
```

**Service layer** — does **not** use `MessageSource`. Services throw custom exceptions carrying a `messageCode` + optional `args`:

```java
throw new ResourceNotFoundException("User not found with email: " + email,
    "error.auth.user.not.found", email);
// The code "error.auth.user.not.found=User not found with email: {0}"
// gets the email substituted for {0} in the GlobalExceptionHandler
```

Custom exceptions: `BadRequestException`, `DuplicateResourceException`, `ResourceNotFoundException`, `UnauthorizedException`, `ForbiddenException`. Each has a `getMessageCode()` and `getArgs()` method.

### Adding a New Locale
1. Create `messages_{locale}.properties` (e.g. `messages_ja.properties`) with all keys translated
2. Add the locale to `setSupportedLocales(...)` in the locale resolver config
3. Repeat for each service that needs the new locale

### Files Implementing i18n (reference)

| File | Purpose |
|---|---|
| `{service}/.../config/I18nConfig.java` | MessageSource + validator bean |
| `{service}/.../config/WebMvcConfig.java` (or equivalent) | LocaleResolver (reads X-Locale) |
| `{service}/.../controller/*.java` | Controllers using resolve() pattern |
| `{service}/.../exception/GlobalExceptionHandler.java` | Resolves message codes from exceptions |
| `{service}/src/main/resources/messages.properties` | English translations |
| `{service}/src/main/resources/messages_id.properties` | Indonesian translations |

**Note:** `api-gateway` is a reactive WebFlux app — it has no i18n. It passes `X-Locale` through to downstream services.

## Important Gotchas
- **`docs/` is stale** — auth, JWT, Spring Security, Kafka, Redis OTP are all implemented, not TODO
- No root `pom.xml` (despite docs claiming multi-module); each service is standalone
- Docker internal hostnames: `cicipin-user-service`, `cicipin-email-service`, `cicipin-kafka`, `cicipin-redis`
- DB port externally is `7001` (not 5432); README shows different values than `.env.example`
- `.env` and `.env.dev` are git-ignored; use `.env.example` as template
- JWT secret must be base64-encoded
