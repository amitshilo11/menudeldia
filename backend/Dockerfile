# Multi-stage build: Gradle -> JRE 21 slim runtime.

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
# Copy minimal Gradle scaffolding from repo root for layer caching.
# TODO: actual build context is the repo root; adjust `docker compose build` accordingly,
#       or switch to a multi-module aware buildx setup.
COPY . .
RUN ./gradlew :backend:bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
COPY --from=build /workspace/backend/build/libs/*.jar /app/app.jar
RUN mkdir -p /data/photos && chown -R app:app /data/photos /app
USER app
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
