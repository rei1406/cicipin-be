# Cicipin Microservices

## Services
- **api-gateway**: Port 8080
- **user-service**: Port 8081

## Setup
```bash
# Build and start all services
docker-compose up --build

# Stop all services
docker-compose down
```

## Endpoints
- Gateway health: `GET http://localhost:8080/actuator/health`
- User service health: `GET http://localhost:8080/health`
- Direct user service: `GET http://localhost:8081/health`

## Database
- PostgreSQL 18.3-alpine on port 5433 (external), 5432 (internal)
- Database: `user_service`
- User: `cicipin`
- Password: `cicipin123`