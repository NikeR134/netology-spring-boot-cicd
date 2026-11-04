FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -q -DskipTests package

FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --uid 10001 --home-dir /app --shell /usr/sbin/nologin appuser
COPY --from=builder --chown=appuser:appuser /workspace/target/todo-app-1.0.0.jar /app/app.jar
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl --fail --silent http://127.0.0.1:8080/tasks || exit 1
ENTRYPOINT ["java", "-XX:InitialRAMPercentage=20", "-XX:MaxRAMPercentage=70", "-jar", "/app/app.jar"]
