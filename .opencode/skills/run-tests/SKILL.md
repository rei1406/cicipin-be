---
name: run-tests
description: Run tests for any Cicipin service using Maven inside Docker containers
---

## Run All Tests for a Service

```bash
./dev.sh exec user-service mvn test
./dev.sh exec api-gateway mvn test
./dev.sh exec email-service mvn test
```

## Run a Single Test Class

```bash
./dev.sh exec user-service mvn test -Dtest=UserServiceTest
```

## Run Tests with Specific Profile

```bash
./dev.sh exec user-service mvn test -Dspring.profiles.active=test
```

## Build Without Tests

```bash
./dev.sh exec user-service mvn clean package -DskipTests
```

## Test Framework

- JUnit 5 + Mockito (`spring-boot-starter-test`)
- email-service: tests tagged `gmail` excluded by default (use real SMTP); Mailpit integration tests run by default

## Caching

Maven dependencies are cached in a Docker volume (`cicipin-maven-cache`) so repeated test runs are faster.
