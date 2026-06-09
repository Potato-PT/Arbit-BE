FROM eclipse-temurin:17-jre

WORKDIR /app

RUN mkdir -p /app/uploads

COPY docker-entrypoint.sh /app/docker-entrypoint.sh
COPY build/libs/arbit-0.0.1-SNAPSHOT.jar app.jar
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/app/docker-entrypoint.sh"]
