# Dockerfile
# El JAR se compila localmente antes de ejecutar docker compose (ver dev.sh)
FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache ttf-dejavu
WORKDIR /app
COPY target/backend_template_web-1.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
