# Multi-stage: Gradle 8 + JDK 21 → Temurin 21 Alpine (runtime).
FROM gradle:8-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle build -x test --no-daemon \
    && JAR=$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -n1) \
    && test -n "$JAR" \
    && cp "$JAR" /workspace/app.jar

FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache curl
ENV SERVER_PORT=9001
WORKDIR /app
COPY --from=build /workspace/app.jar /app/app.jar
EXPOSE 9001
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -sf "http://127.0.0.1:9001/v3/api-docs" > /dev/null || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
