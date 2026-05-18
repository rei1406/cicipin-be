FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY src src
RUN apk add --no-cache maven && mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

ARG API_GATEWAY_CONTAINER_PORT
EXPOSE ${API_GATEWAY_CONTAINER_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
