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
app.base-url=${APP_BASE_URL}
app.images.public-path=${APP_IMAGES_PUBLIC_PATH}
```

Asegúrate de definir estas variables antes de ejecutar la aplicación. 
Recuerda definirlas en tu archivo .env, con los nombres correspondientes. Ejem: ${DB_URL}

| Variable | Descripción | Ejemplo |
|---|---|---|
| `IMAGES_STORAGE_PATH` | Ruta absoluta del disco donde se guardan las imágenes | `C:/uploads/images` |
| `APP_BASE_URL` | URL pública base del servidor | `http://localhost:8080` |
| `APP_IMAGES_PUBLIC_PATH` | Ruta pública bajo la que se sirven las imágenes | `/images` |

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

### Variables de almacenamiento de imágenes

```properties
# Ruta absoluta en disco donde se almacenan las imágenes
IMAGES_STORAGE_PATH=C:/uploads/images

# URL base pública del servidor
APP_BASE_URL=http://localhost:8080

# Prefijo de ruta pública bajo el que se sirven las imágenes
APP_IMAGES_PUBLIC_PATH=/images
```

Estructura de carpetas generada automáticamente al arrancar:

```
C:/uploads/images/
  default/
    default_pic_profile.jpeg   ← copiada desde el classpath al arranque
  {userId}/
    pic-profile/
      {username}_profile.jpg   ← subida por el usuario
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
órdenes, pagos, productos, reseñas, atributos y variantes:

- `User` / `Role`
- `Address`
- `Order` / `Payment`
- `Product` / `Category` / `Subcategory`
- `ProductReview`
- `ProductAttrib` / `ProductAttribValue`
- `ProductVariant` / `ProductVariantAttrib`

## Sistema de atributos y variantes de producto

El proyecto implementa un sistema escalable para definir atributos configurables (talla, color, material, etc.) y variantes de producto que combinan esos atributos.

### Modelo de datos

```
ProductAttrib          → define el tipo de atributo (ej. "Color", "Talla")
  └── ProductAttribValue  → valores posibles (ej. "Rojo", "Azul", "XL")

Product
  ├── ManyToMany → ProductAttrib   (tabla: product_attrib)
  └── OneToMany  → ProductVariant  (cascada completa)
        └── OneToMany → ProductVariantAttrib → ProductAttribValue
```

Una variante es la combinación concreta de valores de atributos de un producto (ej. "Camiseta · Color: Azul · Talla: M"), con su propio SKU, precio y stock.

### Componentes implementados

| Capa | Clases |
|---|---|
| **Entidades** | `ProductAttrib`, `ProductAttribValue`, `ProductVariant`, `ProductVariantAttrib` |
| **Excepción** | `ProductAttribException` — 21 constantes agrupadas por dominio |
| **Repositorios** | `ProductAttribRepository`, `ProductAttribValueRepository`, `ProductVariantRepository`, `ProductVariantAttribRepository` |
| **Servicios** | `ProductAttribService` (10 métodos), `ProductVariantService` (9 métodos) |
| **Request DTOs** | `ProductAttribRequestDTO`, `ProductAttribValueRequestDTO`, `ProductVariantRequestDTO` |
| **Response DTOs** | `ProductAttribResponseDTO`, `ProductAttribValueResponseDTO`, `ProductVariantResponseDTO`, `ProductVariantAttribResponseDTO` |
| **Mappers** | `ProductAttribMapper`, `ProductAttribValueMapper`, `ProductVariantMapper`, `ProductVariantAttribMapper` |
| **Controller** | `ProductAttribController` — todos los endpoints bajo `/products` |

### Comportamientos destacados

- **Auto-slug**: si no se proporciona `slug` al crear un atributo, se genera automáticamente desde el `name` vía `ProductUtils.createSlug`.
- **Variante por defecto**: al crear una variante con `isDefault: true`, el servicio retira el flag de la variante que lo tuviera previamente.
- **Validaciones en variante**: el servicio comprueba que el SKU sea único, que los `attribValueIds` pertenezcan a atributos asignados al producto y que no haya dos valores del mismo tipo de atributo en la misma variante.
- **Respuesta enriquecida**: `ProductResponseDTO` devuelve la lista completa de `attribs` y `variants` al consultar un producto.

## Seguridad

La seguridad está gestionada con **Spring Security + JWT**. Se aplica una estrategia de triple cadena de filtros según el origen de la petición:

| Orden | Origen | Comportamiento |
|---|---|---|
| `@Order(1)` | `localhost:5500` / `127.0.0.1:5500` (Live Server) | Sin restricciones — solo para pruebas con frontend estático |
| `@Order(2)` | Sin cabecera `Origin` (Postman, curl, backend) | Sin restricciones — solo para pruebas con herramientas de API |
| `@Order(3)` | `localhost:4200` y cualquier otro origen | JWT obligatorio en todos los endpoints excepto `/auth/**` e `/images/**` |

> ⚠️ Para pasar a producción, eliminar `devFilterChain` y `noTokenFilterChain`, o restringir los orígenes de prueba.

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
  - `POST /users/upload/{id}` — sube foto de perfil (`multipart/form-data`, campo `file`)
- Imágenes (recursos estáticos públicos):
  - `GET /images/default/default_pic_profile.jpeg`
  - `GET /images/{userId}/pic-profile/{filename}`
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
- Subcategorías:
  - `GET /subcategories`
  - `GET /subcategories/{id}`
  - `POST /subcategories`
  - `PUT /subcategories`
  - `DELETE /subcategories/{id}`
- Productos:
  - `GET /products`
  - `GET /products/{id}`
  - `POST /products`
  - `PUT /products`
  - `DELETE /products/{id}`
- Atributos de producto:
  - `GET /products/attribs` — lista todos los atributos
  - `GET /products/attribs/{id}` — detalle de un atributo
  - `POST /products/attribs` — crea un atributo (slug auto-generado si se omite)
  - `PUT /products/attribs/{id}` — actualiza un atributo
  - `DELETE /products/attribs/{id}` — elimina un atributo y sus valores en cascada
- Valores de atributo:
  - `GET /products/attribs/{attribId}/values` — valores de un atributo
  - `POST /products/attribs/{attribId}/values` — crea un valor para un atributo
  - `PUT /products/attribs/values/{id}` — actualiza un valor
  - `DELETE /products/attribs/values/{id}` — elimina un valor
- Variantes de producto:
  - `GET /products/{productId}/variants` — variantes de un producto
  - `GET /products/variants/{id}` — detalle de una variante
  - `POST /products/{productId}/variants` — crea una variante con sus atributos
  - `PUT /products/variants/{id}` — actualiza campos escalares de una variante
  - `DELETE /products/variants/{id}` — elimina una variante y sus atributos en cascada
- Atributos de variante:
  - `POST /products/variants/{variantId}/attribs/{attribValueId}` — añade un valor de atributo a una variante
  - `DELETE /products/variants/{variantId}/attribs/{variantAttribId}` — elimina un valor de atributo de una variante
- Reseñas de producto:
  - `GET /reviews/product/{productId}`
  - `POST /reviews`
  - `DELETE /reviews/{id}`
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
