# API Reference

All requests go through the API Gateway at `http://localhost:8080`.

## API Versioning

The user service uses a custom header-based versioning scheme. Include the header on every request:

```
X-API-Version: 1
```

If the header is absent or invalid, the server defaults to version `1`. See [api-versioning.md](./api-versioning.md) for full details.

---

## Gateway Endpoints

### Gateway Health

```
GET /actuator/health
```

Returns the gateway's own Spring Actuator health status.

**Response** `200 OK`
```json
{
  "status": "UP"
}
```

---

## User Service — Health

Accessible via gateway at `/user-service-health` or directly at `http://localhost:8081/user-service-health`.

### Service Health Check

```
GET /user-service-health
```

**Response** `200 OK`
```json
{
  "status": "UP",
  "service": "user-service",
  "message": "Service is running"
}
```

---

## Auth Endpoints (`/api/auth`)

> **Note**: All auth endpoints are scaffolded but not yet implemented. The controller exists at `AuthController.java` with `@ApiVersion(1)`.

Base path through gateway: `/user/api/auth`  
Base path direct: `http://localhost:8081/api/auth`

### Register

```
POST /api/auth/register
X-API-Version: 1
```

> 🚧 **TODO** — Not implemented. DTOs not yet created.

Planned behavior: Create a new user account. Should hash the password, set `isActive=false`, `isVerified=false`, and send a verification email.

---

### Login

```
POST /api/auth/login
X-API-Version: 1
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Validate credentials, return a JWT access token and refresh token.

---

### Logout

```
POST /api/auth/logout
X-API-Version: 1
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Invalidate the current session/token.

---

### Refresh Token

```
POST /api/auth/refresh-token
X-API-Version: 1
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Exchange a valid refresh token for a new access token.

---

### Verify Email

```
POST /api/auth/verify-email
X-API-Version: 1
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Verify a user's email address using a token sent via email. Sets `isVerified=true`.

---

## User Endpoints (`/api/users`)

> **Note**: All user endpoints are scaffolded but not yet implemented.

Base path through gateway: `/user/api/users`  
Base path direct: `http://localhost:8081/api/users`

### Get Current User Profile

```
GET /api/users/me
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Return the authenticated user's profile.

---

### Update Current User Profile

```
PUT /api/users/me
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Update the authenticated user's profile fields (name, photo, etc.).

---

### Deactivate Current User Account

```
DELETE /api/users/me
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Soft-delete by setting `isActive=false`.

---

### Get User by ID (Admin)

```
GET /api/users/{id}
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Admin-only. Return any user's profile by UUID.

---

### List All Users (Admin)

```
GET /api/users/
```

> 🚧 **TODO** — Not implemented.

Planned behavior: Admin-only. Return a paginated list of all users.

---

## Port Reference

| Service | Host Port | Container Port |
|---|---|---|
| API Gateway | 8080 | 8080 |
| User Service | 8081 | 8081 |
| User Service DB | 7001 | 7001 |
