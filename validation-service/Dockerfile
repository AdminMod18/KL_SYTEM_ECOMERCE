# syntax=docker/dockerfile:1
FROM gradle:8.10.2-jdk21 AS build
WORKDIR /workspace
COPY build.gradle settings.gradle* ./
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle dependencies --no-daemon -q || true
COPY src ./src
RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
ENV SERVER_PORT=9004
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build --chown=app:app /workspace/build/libs/*.jar app.jar
USER app
EXPOSE 9004
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]
