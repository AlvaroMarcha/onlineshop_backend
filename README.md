# Online Shop Backend

Este proyecto es una plantilla básica de backend desarrollada con [Spring Boot](https://spring.io/projects/spring-boot) y Maven. Incluye ejemplos de entidades JPA, un controlador de salud y está preparado para conectarse a una base de datos MariaDB/MySQL mediante variables de entorno.

## Requisitos

- Java 21
- Maven 3.5+
- Docker (opcional para desarrollo)
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

## Levantar BBDD y proyecto por separado para HotReload

1. **Base de datos** (Docker)
   - Levantar: 
     ```bash
     ./dev.sh db-start
     ```
   - Detener:
     ```bash
     ./dev.sh db-stop
     ```

2. **Spring Boot**
   - Construir y levantar:
     ```bash
     ./dev.sh app
     ```
   - Hot reload:
     ```bash
     ./dev.sh app-hotreload
     ```

3. **Todo a la vez**
   - Levanta DB y Spring Boot (por defecto):
     ```bash
     ./dev.sh all
     ```

4. **Ayuda**
   - Mostrar menú de ayuda:
     ```bash
     ./dev.sh help
     ```

### Menú completo de comandos

```bash
    echo "Uso: ./dev.sh [db-start|db-stop|app|app-hotreload|all|help]"
    echo "  db-start       : Levanta solo la base de datos"
    echo "  db-stop        : Detiene solo la base de datos"
    echo "  app            : Construye y levanta Spring Boot"
    echo "  app-hotreload  : Ejecuta Spring Boot en modo hot-reload"
    echo "  all            : Levanta DB y Spring Boot (por defecto)"
    echo "  help           : Muestra este menú de ayuda"
``` 

### Tabla de comandos

| Comando             | Descripción                                      |
|--------------------|-------------------------------------------------|
| `./dev.sh db-start` | Levanta solo la base de datos (Docker)         |
| `./dev.sh db-stop`  | Detiene solo la base de datos                   |
| `./dev.sh app`      | Construye y levanta Spring Boot                |
| `./dev.sh app-hotreload` | Ejecuta Spring Boot en modo hot-reload   |
| `./dev.sh all`      | Levanta DB y Spring Boot (por defecto)         |
| `./dev.sh help`     | Muestra el menú de ayuda   


## Entidades de ejemplo

El proyecto incluye modelos JPA con relaciones entre usuarios, direcciones,
órdenes y pagos:

- `User`
- `Role`
- `Address`
- `Order`
- `Payment`

## Endpoints principales

Estos son los endpoints más relevantes que expone la API:

- Salud:
  - `GET /health/status`
- Autenticación:
  - `POST /auth/login`
  - `POST /auth/register`
  - `POST /auth/logout`
- Usuarios:
  - `GET /users`
  - `GET /users/{id}`
  - `POST /users`
  - `PUT /users`
  - `DELETE /users/{id}`
  - `POST /users/ban/{id}`
- Direcciones:
  - `GET /address/{id}`
  - `POST /address`
  - `PUT /address`
  - `DELETE /address/{id}`
- Órdenes y pagos:
  - `GET /orders/users/{id}`
  - `GET /orders/{id}`
  - `POST /orders/nextStatus`
  - `POST /orders/{orderId}/payments`
  - `GET /orders/{orderId}/payments/last`
  - `POST /orders/payments/{paymentId}/nextStatus`
  - Categorías:
  - `GET /categories`
  - `GET /categories/{id}`
  - `POST /categories`
  - `PUT /categories`
  - `DELETE /categories/{id}`

## Pruebas

Para ejecutar la suite de pruebas:

```bash
mvn test
```
