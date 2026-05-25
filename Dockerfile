FROM eclipse-temurin:17-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=default
ENV SERVER_PORT=8080
ENV STORAGE_LOCAL_UPLOAD_DIR=/app/uploads

RUN mkdir -p /app/uploads

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
