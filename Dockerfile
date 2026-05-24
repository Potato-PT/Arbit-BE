FROM eclipse-temurin:17-jdk AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=default
ENV SERVER_PORT=8080
ENV STORAGE_LOCAL_UPLOAD_DIR=/app/uploads

RUN mkdir -p /app/uploads

COPY --from=builder /workspace/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
