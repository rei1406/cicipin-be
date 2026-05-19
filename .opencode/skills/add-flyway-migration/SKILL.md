---
name: add-flyway-migration
description: Add a new Flyway database migration to user-service
---

## Steps

### 1. Create Migration File

Create a file in `user-service/src/main/resources/db/migration/` following the naming convention:

```
V{n}__{description}.sql
```

Where `{n}` is the next sequential number (check existing files) and `{description}` is PascalCase with underscores (e.g. `V2__AddRefreshTokenTable.sql`).

### 2. Write the Migration

Use standard PostgreSQL DDL. Example:

```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    token VARCHAR(512) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

### 3. Update Entity (if adding/changing columns)

Update the corresponding JPA entity in `user-service/src/main/java/com/cicipin/userservice/.../entity/`. Add new fields with `@Column` annotations matching the migration.

### 4. Important

- Spring Boot `ddl-auto` is set to `validate` — schema is managed by Flyway only, never by JPA auto-DDL
- Migrations are applied automatically on startup (Flyway runs before the app is ready)
- After creating the migration, rebuild and restart: `./dev.sh build user-service && ./dev.sh up -d user-service`
