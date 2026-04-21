# ════════════════════════════════════════════════════════════════════
# STAGE 1 — Build
# ════════════════════════════════════════════════════════════════════
FROM maven:3.9.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ════════════════════════════════════════════════════════════════════
# STAGE 2 — Runtime (image légère ~200MB au lieu de ~600MB)
# ════════════════════════════════════════════════════════════════════
FROM --platform=linux/amd64 eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd --system voom && useradd --system --gid voom voom

COPY --from=build /app/target/*.jar app.jar
RUN chown voom:voom app.jar

USER voom

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]