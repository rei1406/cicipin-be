# Services

## 1. API Gateway

**Module**: `api-gateway`  
**Artifact**: `com.cicipin:api-gateway:1.0.0`  
**Port**: 8080 (host + container)  
**Entry point**: `com.cicipin.gateway.ApiGatewayApplication`

### Purpose

Single entry point for all client traffic. Routes incoming HTTP requests to the correct downstream microservice based on path predicates. Built on Spring Cloud Gateway (reactive/WebFlux).

### Dependencies

| Dependency | Purpose |
|---|---|
| `spring-cloud-starter-gateway-server-webflux` | Reactive API gateway |
| `spring-boot-starter-actuator` | Health endpoint at `/actuator/health` |

### Routing Rules

| Route ID | Path Predicate | Upstream | Notes |
|---|---|---|---|
| `user-service` | `/user/**` | `http://cicipin-user-service:8081` | Forwards full path (no prefix strip) |
| `user-service-health` | `/user-service-health` | `http://cicipin-user-service:8081` | Direct health passthrough |
| `email-service-health` | `/email-service-health` | `http://cicipin-email-service:8082` | Direct health passthrough |

> ⚠️ The email service's `/api/email/**` endpoints are **not** exposed through the gateway — they are internal only, reachable only by other services inside the Docker network.

### Own Endpoints

- `GET /actuator/health` — gateway's own health status
- `GET /email-service-health` — proxied health passthrough to email-service

### Source Files

```
api-gateway/src/main/java/com/cicipin/gateway/
└── ApiGatewayApplication.java      ← @SpringBootApplication entry point

api-gateway/src/main/resources/
└── application.yml                 ← All routing config lives here
```

---

## 2. User Service

**Module**: `user-service`  
**Artifact**: `com.cicipin:user-service:1.0.0`  
**Port**: 8081 (host + container)  
**Entry point**: `com.cicipin.userservice.UserServiceApplication`

### Purpose

Manages user accounts, authentication, and user profiles. Owns the `users` table in PostgreSQL.

### Dependencies

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-web` | Spring MVC REST controllers |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM |
| `spring-boot-starter-validation` | Bean validation (`@Valid`, `@NotNull`, etc.) |
| `spring-boot-starter-actuator` | Health endpoint |
| `postgresql` | JDBC driver |
| `flyway-core` + `flyway-database-postgresql` | Database migrations |
| `spring-boot-flyway` | Flyway Spring Boot integration |
| `lombok` | Boilerplate reduction (compile-time only) |

### Package Structure

```
com.cicipin.userservice/
├── UserServiceApplication.java         ← Entry point
├── auth/
│   ├── AuthController.java             ← REST controller for /api/auth
│   ├── AuthService.java                ← Interface (methods all TODO)
│   ├── AuthServiceImpl.java            ← Implementation (methods all TODO)
│   └── dto/                            ← Empty — DTOs not yet created
├── user/
│   ├── UserController.java             ← REST controller for /api/users
│   ├── UserService.java                ← Interface (methods all TODO)
│   ├── UserServiceImpl.java            ← Implementation (methods all TODO)
│   └── UserRepository.java             ← JPA repository
└── common/
    ├── HealthController.java           ← GET /user-service-health
    ├── model/
    │   ├── User.java                   ← JPA entity
    │   └── UserRole.java               ← Enum: CUSTOMER, ADMIN, etc.
    └── versioning/
        ├── ApiVersion.java             ← @ApiVersion annotation
        ├── ApiVersionCondition.java    ← Matching logic (X-API-Version header)
        ├── VersionRequestMappingHandlerMapping.java ← Plugs into Spring MVC
        └── WebMvcConfig.java           ← Registers custom handler mapping
```

### Database

- **Engine**: PostgreSQL 18.3-alpine
- **Container name**: `cicipin-user-service-db`
- **Internal port**: 7001 (non-standard, set via `PGPORT`)
- **Host port**: 7001
- **Database name**: `user_db`
- **Schema managed by**: Flyway (migrations in `src/main/resources/db/migration/`)
- **JPA DDL mode**: `validate` (Flyway owns the schema, Hibernate only validates)

### Own Endpoints

- `GET /user-service-health` — returns `{ "status": "UP", "service": "user-service", "message": "Service is running" }`
- `GET /actuator/health` — Spring Actuator health
- `POST /api/auth/register` — 🚧 TODO
- `POST /api/auth/login` — 🚧 TODO
- `POST /api/auth/logout` — 🚧 TODO
- `POST /api/auth/refresh-token` — 🚧 TODO
- `POST /api/auth/verify-email` — 🚧 TODO
- `GET /api/users/me` — 🚧 TODO
- `PUT /api/users/me` — 🚧 TODO
- `DELETE /api/users/me` — 🚧 TODO
- `GET /api/users/{id}` — 🚧 TODO (admin)
- `GET /api/users/` — 🚧 TODO (admin)

---

## 3. Email Service

**Module**: `email-service`  
**Artifact**: `com.cicipin:email-service:1.0.0`  
**Port**: 8082 (host + container)  
**Entry point**: `com.cicipin.emailservice.EmailServiceApplication`

### Purpose

Handles all outbound email delivery for the platform. Other services call this service over HTTP (internal Docker network only). Renders HTML emails using Thymeleaf templates and delivers them via SMTP (Gmail by default).

> This service is **not** exposed through the API Gateway. It is an internal service only.

### Dependencies

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-web` | Spring MVC REST controllers |
| `spring-boot-starter-mail` | JavaMailSender / SMTP integration |
| `spring-boot-starter-thymeleaf` | HTML email template rendering |
| `spring-boot-starter-validation` | Bean validation on request DTOs |
| `spring-boot-starter-test` | JUnit 5 + Mockito (test scope) |
| `lombok` | Boilerplate reduction (compile-time only) |

### Package Structure

```
com.cicipin.emailservice/
├── EmailServiceApplication.java
├── controller/
│   ├── EmailController.java         ← POST /api/email/send
│   ├── HealthController.java        ← GET /email-service-health
│   └── GlobalExceptionHandler.java  ← Validation & runtime error handling
├── service/
│   ├── EmailService.java            ← Interface
│   └── EmailServiceImpl.java        ← Thymeleaf rendering + JavaMailSender
└── dto/
    ├── EmailType.java               ← Enum: OTP_VERIFICATION, WELCOME, PASSWORD_RESET, GENERIC
    ├── SendEmailRequest.java        ← Polymorphic base (discriminated by "type" field)
    ├── OtpEmailRequest.java         ← name, otpCode, expiryMinutes
    ├── WelcomeEmailRequest.java     ← name
    ├── PasswordResetEmailRequest.java ← name, resetCode, expiryMinutes
    ├── GenericEmailRequest.java     ← subject, body (plain text)
    └── SendEmailResponse.java       ← { success, message }
```

### Email Templates

Located in `src/main/resources/templates/email/`:

| Template file | Email type | Description |
|---|---|---|
| `otp-verification.html` | `OTP_VERIFICATION` | 6-digit OTP code for email verification |
| `welcome.html` | `WELCOME` | Sent after successful email verification |
| `password-reset.html` | `PASSWORD_RESET` | OTP code for password reset flow |

Generic emails (`GENERIC` type) are sent as plain text without a template.

### API

#### `POST /api/email/send`

Internal endpoint. The `type` field is the discriminator — it determines which subclass is deserialized and which template is used.

**OTP Verification:**
```json
{
  "type": "OTP_VERIFICATION",
  "to": "user@example.com",
  "name": "John Doe",
  "otpCode": "482910",
  "expiryMinutes": 5
}
```

**Welcome:**
```json
{
  "type": "WELCOME",
  "to": "user@example.com",
  "name": "John Doe"
}
```

**Password Reset:**
```json
{
  "type": "PASSWORD_RESET",
  "to": "user@example.com",
  "name": "John Doe",
  "resetCode": "654321",
  "expiryMinutes": 15
}
```

**Generic:**
```json
{
  "type": "GENERIC",
  "to": "user@example.com",
  "subject": "Your order is confirmed",
  "body": "Plain text body here."
}
```

**Response:**
```json
{ "success": true, "message": "Email sent successfully" }
```

#### `GET /email-service-health`

Returns `{ "status": "UP", "service": "email-service", "message": "Service is running" }`.  
Also proxied through the gateway at `GET /email-service-health`.

### SMTP Configuration

The service uses Gmail SMTP by default (`smtp.gmail.com:587`, STARTTLS). Configure via environment variables — see `.env.example` for all required keys.

For Gmail, `MAIL_PASSWORD` must be a **16-character App Password** (not your regular account password). Generate one at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) after enabling 2-Step Verification.

### Tests

```
src/test/java/com/cicipin/emailservice/service/
├── EmailServiceImplTest.java        ← Unit tests (Mockito, no SMTP)
├── EmailServiceMailpitTest.java     ← Integration tests against Mailpit mail catcher (runs by default)
└── EmailServiceIntegrationTest.java ← Integration tests against real Gmail SMTP (excluded by default, tagged "gmail")
```

#### Mailpit tests (default)

Mailpit is a mail catcher included in `docker-compose.dev.yml`. It accepts SMTP on port 1025 and exposes a REST API on port 8025 that the tests use to assert on captured messages — subject, recipient, body content, etc.

```bash
# Run all default tests (includes Mailpit, excludes Gmail)
./dev.sh exec email-service mvn test

# Run Mailpit tests specifically
./dev.sh exec email-service mvn test -Dtest=EmailServiceMailpitTest
```

Open `http://localhost:8025` in your browser to inspect captured emails visually while tests run.

#### Gmail SMTP tests (opt-in)

These tests send real emails via Gmail SMTP. They are tagged `gmail` and excluded from the default `mvn test` run. To run them, pass both `-Dsurefire.excludedGroups=` (to clear the exclusion) and `-Dsurefire.groups=gmail`:

```bash
./dev.sh exec email-service mvn test -Dsurefire.excludedGroups= -Dsurefire.groups=gmail
```

Requires `MAIL_USERNAME` and `MAIL_PASSWORD` to be set in `.env.dev`. The tests are also guarded by `@EnabledIfEnvironmentVariable` so they skip automatically if credentials are absent.

#### Unit tests

```bash
./dev.sh exec email-service mvn test -Dtest=EmailServiceImplTest
```
