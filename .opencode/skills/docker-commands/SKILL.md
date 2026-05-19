---
name: docker-commands
description: Build, run, restart, exec, test, and debug the Cicipin backend services in Docker
---

## Quick Reference

All commands use `./dev.sh` (alias for `docker compose -f docker-compose.dev.yml`).

## Common Commands

| Action | Command |
|---|---|
| Start everything with rebuild | `./dev.sh up --build` |
| Start without rebuild | `./dev.sh up` |
| Start a single service | `./dev.sh up -d <service>` (e.g. `user-service`, `api-gateway`, `email-service`, `kafka`, `redis`) |
| Stop all | `./dev.sh down` |
| Restart a service | `./dev.sh restart <service>` |
| Rebuild a service | `./dev.sh build <service>` |
| Rebuild and restart | `./dev.sh build <service> && ./dev.sh up -d <service>` |
| View logs (all) | `./dev.sh logs -f` |
| View logs (one service) | `./dev.sh logs -f <service>` |
| Health check | `curl localhost:8080/user-service-health` |

## Running Commands Inside a Container

```bash
./dev.sh exec user-service <command>   # e.g. ./dev.sh exec user-service mvn test
./dev.sh exec api-gateway <command>
./dev.sh exec email-service <command>
```

## Running Tests

```bash
./dev.sh exec user-service mvn test
./dev.sh exec api-gateway mvn test
./dev.sh exec email-service mvn test
```

## Building JAR (without running)

```bash
./dev.sh exec user-service mvn clean package -DskipTests
```

## Environment Setup

```bash
cp .env.example .env.dev   # fill in secrets first
```

## Service Ports

| Service | External Port |
|---|---|
| api-gateway | 8080 |
| user-service | 8081 |
| email-service | 8082 |
| Mailpit Web UI | 8025 |
| Mailpit SMTP | 1025 |
| PostgreSQL (host) | 7001 |

## Internal Docker Hostnames

- `cicipin-user-service`
- `cicipin-email-service`
- `cicipin-kafka`
- `cicipin-redis`
- `cicipin-mailpit`

## Rebuilding After Dependency Changes

If you modify `pom.xml` (add a dependency, change version), you must rebuild:

```bash
# Rebuild the specific service image (invalidates Maven cache for that service)
./dev.sh build user-service
# Then restart
./dev.sh up -d user-service
```
