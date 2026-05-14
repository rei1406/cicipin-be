# Cicipin Backend — Documentation Index

This folder contains all technical documentation for the Cicipin backend project.

## Documents

| File | Description |
|---|---|
| [overview.md](./overview.md) | High-level architecture, tech stack, and project goals |
| [services.md](./services.md) | Per-service breakdown: purpose, ports, dependencies |
| [api-reference.md](./api-reference.md) | All API endpoints, request/response shapes, versioning |
| [data-model.md](./data-model.md) | Database schema, entities, enums, and Flyway migrations |
| [infrastructure.md](./infrastructure.md) | Docker Compose, Dockerfiles, environment variables |
| [api-versioning.md](./api-versioning.md) | How the custom `X-API-Version` header versioning works |
| [development-guide.md](./development-guide.md) | How to run, build, and extend the project locally |
| [llm-context.md](./llm-context.md) | Compact context file for LLMs — start here when working on this codebase |

## Quick Start

```bash
# Development (hot-reload)
./dev.sh up --build

# Production
docker compose up --build
```

All traffic enters through the API Gateway on port **8080**.
