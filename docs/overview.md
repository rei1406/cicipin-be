# Project Overview

## What is Cicipin?

Cicipin is a food delivery / restaurant platform backend built as a **Spring Boot microservices** system. The name suggests a tasting/sampling concept (Indonesian: "cicipin" = to taste).

## Architecture

```
Client
  │
  ▼
┌─────────────────────┐
│    API Gateway       │  :8080  (Spring Cloud Gateway, WebFlux)
│  cicipin-api-gateway │
└──────────┬──────────┘
           │  HTTP routing
           ▼
┌─────────────────────┐
│    User Service      │  :8081  (Spring Boot MVC)
│ cicipin-user-service │
└──────────┬──────────┘
           │  JDBC / JPA
           ▼
┌─────────────────────┐
│  PostgreSQL DB       │  :7001
│ cicipin-user-svc-db  │
└─────────────────────┘
```

All client requests go through the API Gateway. The gateway routes them to the appropriate downstream service based on path predicates. There is no service-to-service communication yet.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 25 |
| Framework | Spring Boot 4.0.6 |
| Cloud | Spring Cloud 2025.1.1 |
| Gateway | Spring Cloud Gateway (WebFlux / reactive) |
| Web (services) | Spring MVC (servlet) |
| Persistence | Spring Data JPA + Hibernate |
| Database | PostgreSQL 18.3-alpine |
| Migrations | Flyway |
| Build | Maven (multi-module) |
| Containerization | Docker + Docker Compose |
| Boilerplate reduction | Lombok |

## Module Structure

```
cicipin/                    ← Maven parent (pom.xml)
├── api-gateway/            ← Spring Cloud Gateway module
├── user-service/           ← User & auth management module
├── docker-compose.yml      ← Production compose
├── docker-compose.dev.yml  ← Development compose (hot-reload)
├── dev.sh                  ← Shortcut: runs docker-compose.dev.yml
├── .env                    ← Production environment variables
├── .env.dev                ← Development environment variables
└── docs/                   ← This documentation folder
```

## User Roles

The platform supports four user roles:

| Role | Description |
|---|---|
| `CUSTOMER` | End users who order food |
| `RESTAURANT_OWNER` | Manages restaurant listings and menus |
| `DELIVERY_PARTNER` | Handles order deliveries |
| `ADMIN` | Platform administrators |

## Implementation Status

| Feature | Status |
|---|---|
| Project scaffolding & Maven multi-module | ✅ Complete |
| Docker Compose (prod + dev with hot-reload) | ✅ Complete |
| API Gateway routing | ✅ Complete |
| Database schema (Flyway migration) | ✅ Complete |
| User entity & JPA repository | ✅ Complete |
| Custom API versioning infrastructure | ✅ Complete |
| Health check endpoints | ✅ Complete |
| Auth endpoints (register, login, etc.) | 🚧 Scaffolded — TODO |
| User CRUD endpoints | 🚧 Scaffolded — TODO |
| JWT / Spring Security | ❌ Not started |
| Request/Response DTOs | ❌ Not started |
| Other microservices (restaurant, order, etc.) | ❌ Not started |
