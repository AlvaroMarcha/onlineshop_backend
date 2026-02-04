# Online Shop Backend

Este proyecto es una plantilla básica de backend desarrollada con [Spring Boot](https://spring.io/projects/spring-boot) y Maven. Incluye ejemplos de entidades JPA, un controlador de salud y está preparado para conectarse a una base de datos MariaDB/MySQL mediante variables de entorno.

## Requisitos

- Java 21
- Maven 3.5+
- Base de datos compatible con MariaDB/MySQL

## Configuración

La aplicación lee su configuración de conexión a base de datos y otros parámetros desde variables de entorno definidas en `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.datasource.driver-class-name=${DB_DRIVER}
spring.jpa.database-platform=${DB_DIALECT}
spring.jpa.show-sql=${DB_SHOW_SQL}
spring.jpa.hibernate.ddl-auto=${DB_HBM2DDL}
server.port=${SERVER_PORT:8080}
app.images.storage-path=${IMAGES_STORAGE_PATH}
```

Asegúrate de definir estas variables antes de ejecutar la aplicación. 
Recuerda definirlas en tu archivo .env, con los nombres correspondientes. Ejem: ${DB_URL}

## Ejecución

```bash
mvn spring-boot:run
```

La aplicación expone un endpoint de salud en `/health/status` que devuelve el estado de la API:

```bash
curl http://localhost:8080/health/status
```

## Entidades de ejemplo

El proyecto incluye modelos JPA de ejemplo con relaciones uno a uno:

- `User`
- `Role`
- `Client`

## Pruebas

Para ejecutar la suite de pruebas:

```bash
mvn test
```

