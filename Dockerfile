# ---- Build stage ----
FROM gradle:8.12.0-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./

# ✅ (권장) dependencies 단계 제거: toolchain 이슈/네트워크 이슈 줄임

COPY src ./src
RUN gradle --no-daemon clean bootJar -x test

# ✅ bootJar(plain 제외)만 집기
RUN set -e; \
    JAR_PATH="$(ls -1 build/libs/*.jar | grep -v plain | head -n 1)"; \
    cp "$JAR_PATH" /app/app.jar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN useradd -r -u 10001 -g root spring
COPY --from=build /app/app.jar /app/app.jar

RUN mkdir -p /app/logs && chown -R spring:root /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

USER spring
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -fsS http://localhost:8080/actuator/health || exit 1

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
