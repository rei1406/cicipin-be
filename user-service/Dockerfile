FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY user-service/pom.xml user-service/pom.xml
COPY user-service/src user-service/src
RUN apk add --no-cache maven && mvn -pl user-service -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=builder /app/user-service/target/*.jar app.jar

ARG USER_SERVICE_CONTAINER_PORT
EXPOSE ${USER_SERVICE_CONTAINER_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]