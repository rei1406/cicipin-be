FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY email-service/pom.xml email-service/pom.xml
COPY email-service/src email-service/src
RUN apk add --no-cache maven && mvn -pl email-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=builder /app/email-service/target/*.jar app.jar

ARG EMAIL_SERVICE_CONTAINER_PORT
EXPOSE ${EMAIL_SERVICE_CONTAINER_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]
