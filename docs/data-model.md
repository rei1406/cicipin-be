# Data Model

## Database Overview

| Property | Value |
|---|---|
| Engine | PostgreSQL 18.3-alpine |
| Migration tool | Flyway |
| JPA DDL mode | `validate` (Flyway owns the schema) |
| Migration location | `user-service/src/main/resources/db/migration/` |

Flyway runs automatically on service startup. The JPA `ddl-auto: validate` setting means Hibernate will verify the schema matches the entities but will never modify it.

---

## Migrations

### V1 — Create Users Table

File: `db/migration/V1__create_users_table.sql`

```sql
CREATE TYPE user_role AS ENUM (
    'CUSTOMER',
    'ADMIN',
    'DELIVERY_PARTNER',
    'RESTAURANT_OWNER'
);

CREATE TABLE users (
    id          UUID         NOT NULL DEFAULT gen_random_uuid(),
    username    VARCHAR(50)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        user_role    NOT NULL,
    is_verified BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active   BOOLEAN      NOT NULL DEFAULT FALSE,
    photo       VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);
```

---

## Entities

### `User`

Java class: `com.cicipin.userservice.common.model.User`  
Table: `users`  
Lombok: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

| Field | Java Type | Column | Constraints | Notes |
|---|---|---|---|---|
| `id` | `UUID` | `id` | PK, NOT NULL | Auto-generated via `GenerationType.UUID` |
| `username` | `String` | `username` | NOT NULL, UNIQUE, max 50 chars | |
| `name` | `String` | `name` | NOT NULL, max 100 chars | Display name |
| `email` | `String` | `email` | NOT NULL, UNIQUE, max 255 chars | |
| `password` | `String` | `password` | NOT NULL, max 255 chars | Should be stored hashed (bcrypt) |
| `role` | `UserRole` | `role` | NOT NULL | Stored as PostgreSQL `user_role` enum |
| `isVerified` | `boolean` | `is_verified` | NOT NULL, default `false` | Set to `true` after email verification |
| `isActive` | `boolean` | `is_active` | NOT NULL, default `false` | Set to `true` after account activation |
| `photo` | `String` | `photo` | nullable, max 500 chars | URL to profile photo |
| `createdAt` | `LocalDateTime` | `created_at` | NOT NULL, immutable | Auto-set by `@CreationTimestamp` |
| `updatedAt` | `LocalDateTime` | `updated_at` | NOT NULL | Auto-updated by `@UpdateTimestamp` |

---

## Enums

### `UserRole`

Java class: `com.cicipin.userservice.common.model.UserRole`  
PostgreSQL type: `user_role` (native ENUM)

| Value | Description |
|---|---|
| `CUSTOMER` | End user who places food orders |
| `RESTAURANT_OWNER` | Manages restaurant listings and menus |
| `DELIVERY_PARTNER` | Handles order deliveries |
| `ADMIN` | Platform administrator with elevated access |

---

## Repository

### `UserRepository`

Java class: `com.cicipin.userservice.user.UserRepository`  
Extends: `JpaRepository<User, UUID>`

Custom query methods:

| Method | Return Type | Description |
|---|---|---|
| `findByEmail(String email)` | `Optional<User>` | Look up user by email address |
| `findByUsername(String username)` | `Optional<User>` | Look up user by username |
| `existsByEmail(String email)` | `boolean` | Check if email is already taken |
| `existsByUsername(String username)` | `boolean` | Check if username is already taken |

All standard `JpaRepository` methods are also available (`findById`, `save`, `delete`, `findAll`, etc.).

---

## Adding New Migrations

Follow Flyway's versioned naming convention:

```
V{version}__{description}.sql
```

Examples:
- `V2__add_phone_to_users.sql`
- `V3__create_restaurants_table.sql`

Place files in `user-service/src/main/resources/db/migration/`. Flyway applies them in version order on startup. **Never modify an already-applied migration** — always create a new version.
