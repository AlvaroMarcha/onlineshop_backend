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

### Variables de Mail y Google OAuth2

El sistema de envío de correos utiliza Gmail SMTP con autenticación OAuth2 (XOAUTH2), más seguro que las contraseñas de aplicación. Añade las siguientes variables a tu `.env`:

```properties
# Mail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=465
MAIL_USERNAME=tu_cuenta@gmail.com

# Google OAuth2 (para Gmail SMTP via XOAUTH2)
GOOGLE_CLIENT_ID=tu_client_id
GOOGLE_CLIENT_SECRET=tu_client_secret
GOOGLE_TOKEN_URI=https://oauth2.googleapis.com/token
GOOGLE_REFRESH_TOKEN=tu_refresh_token
```

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
- Mail:
  - `POST /mails/testing/send`

## Configuración Google OAuth2 — Obtener el Refresh Token

El `GOOGLE_REFRESH_TOKEN` se genera **una sola vez** mediante el flujo Authorization Code de Google.

### Opción 1 — OAuth 2.0 Playground (recomendado)

1. Ve a [developers.google.com/oauthplayground](https://developers.google.com/oauthplayground)
2. Haz clic en ⚙️ (Settings) → activa **"Use your own OAuth credentials"**
3. Introduce tu `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`
4. En el listado de scopes selecciona:
   ```
   https://mail.google.com/
   ```
   > ⚠️ Este scope exacto es obligatorio para SMTP XOAUTH2. `gmail.send` no es válido para SMTP.
5. Haz clic en **"Authorize APIs"** y autoriza con tu cuenta de Gmail
6. En el **Paso 2**, haz clic en **"Exchange authorization code for tokens"**
7. Copia el `refresh_token` de la respuesta y añádelo al `.env`

### Opción 2 — Curl manual

Abre este enlace en el navegador (sustituye tu `CLIENT_ID`):

```
https://accounts.google.com/o/oauth2/v2/auth
  ?client_id=TU_CLIENT_ID
  &redirect_uri=http://localhost:8080/oauth2/callback
  &response_type=code
  &scope=https://mail.google.com/
  &access_type=offline
  &prompt=consent
```

Luego intercambia el `code` recibido:

```bash
curl -X POST https://oauth2.googleapis.com/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=TU_CLIENT_ID" \
  -d "client_secret=TU_CLIENT_SECRET" \
  -d "code=AUTHORIZATION_CODE" \
  -d "grant_type=authorization_code" \
  -d "redirect_uri=http://localhost:8080/oauth2/callback"
```

> `access_type=offline` y `prompt=consent` son obligatorios para que Google devuelva el `refresh_token`.

### Nota sobre el entorno de la app en Google Cloud

Si la OAuth consent screen está en modo **Testing**, los refresh tokens caducan a los **7 días**.
Para que sean permanentes, cambia el estado a **In production** en:
[Google Cloud Console → APIs & Services → OAuth consent screen](https://console.cloud.google.com/apis/credentials/consent)

## Pruebas

Para ejecutar la suite de pruebas:

```bash
mvn test
```
