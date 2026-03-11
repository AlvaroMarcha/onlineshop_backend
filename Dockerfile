# =============================================================================
# DOCKERFILE OPTIMIZADO - Caché de dependencias Maven en volumen persistente
# =============================================================================

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar SOLO pom.xml primero (para caché de dependencias)
COPY pom.xml .

# Descargar dependencias (se cachea en volumen maven_cache)
# El volumen se monta desde docker-compose.yml en /root/.m2
RUN mvn dependency:go-offline -B

# Ahora copiar el código fuente
COPY src ./src

# Compilar (dependencias ya están en caché)
RUN mvn clean package -DskipTests

# ---------- Run stage ----------
FROM eclipse-temurin:21-jdk-alpine
RUN apk add --no-cache ttf-dejavu
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENV PORT=8080
# Ejecutar la app usando la variable PORT
CMD ["sh", "-c", "java -Dserver.port=$PORT -jar app.jar"]