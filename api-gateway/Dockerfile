FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /app
COPY pom.xml .
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY api-gateway/src api-gateway/src
RUN apk add --no-cache maven && mvn -pl api-gateway -am clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=builder /app/api-gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]