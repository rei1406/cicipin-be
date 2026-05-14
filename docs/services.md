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

### Own Endpoints

- `GET /actuator/health` — gateway's own health status

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
