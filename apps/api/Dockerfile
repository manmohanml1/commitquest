FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace
COPY apps/api ./apps/api
COPY docs/api ./docs/api

WORKDIR /workspace/apps/api
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S commitquest && adduser -S commitquest -G commitquest
WORKDIR /app
COPY --from=build /workspace/apps/api/build/libs/*.jar /app/commitquest-api.jar

ENV PORT=10000
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC -Djava.security.egd=file:/dev/urandom"
EXPOSE 10000

USER commitquest
ENTRYPOINT ["java", "-jar", "/app/commitquest-api.jar"]
