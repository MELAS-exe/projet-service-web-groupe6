# STAGE 1 — Build
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests -B

# STAGE 2 — Runtime
FROM eclipse-temurin:21-jdk-alpine
LABEL maintainer="voom-team"
LABEL version="1.0"
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]