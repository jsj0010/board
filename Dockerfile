# ---- Build stage ----
FROM gradle:8.12.0-jdk21 AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle --no-daemon clean bootJar -x test

RUN set -e; \
    JAR_PATH="$(ls -1 build/libs/*.jar | grep -v plain | head -n 1)"; \
    cp "$JAR_PATH" /app/app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r -g 10001 spring \
 && useradd  -r -u 10001 -g spring spring

COPY --from=builder --chown=spring:spring /app/app.jar /app/app.jar

RUN apt-get update && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
