# Online Shop Backend

Este proyecto es una plantilla básica de backend desarrollada con [Spring Boot](https://spring.io/projects/spring-boot) y Maven. Incluye ejemplos de entidades JPA, un controlador de salud y está preparado para conectarse a una base de datos MariaDB/MySQL mediante variables de entorno.

---

## 📚 Documentación Rápida

| Guía | Descripción |
|------|-------------|
| **[📖 SETUP_LOCAL.md](./SETUP_LOCAL.md)** | **Guía paso a paso para levantar el proyecto en local** |
| [🌐 NGINX_GUIDE.md](./NGINX_GUIDE.md) | Configuración de Nginx como reverse proxy |
| [🚀 DEPLOY_VPS.md](./DEPLOY_VPS.md) | Deploy completo en VPS con alanmarcha.com |
| [🔧 ENV_GUIDE.md](./ENV_GUIDE.md) | Variables de entorno (desarrollo vs producción) |

**¿Primera vez?** → Lee [SETUP_LOCAL.md](./SETUP_LOCAL.md) para configurar tu entorno de desarrollo.

---

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
| `INVOICES_STORAGE_PATH` | Ruta absoluta donde se guardan los PDF de facturas (por defecto usa `IMAGES_STORAGE_PATH`) | `C:/uploads/invoices` |
| `COMPANY_NAME` | Nombre legal de la empresa emisora | `Mi Tienda S.L.` |
| `COMPANY_NIF` | NIF/CIF de la empresa | `B12345678` |
| `COMPANY_ADDRESS` | Dirección fiscal (escapar `ñ` como `\u00f1`) | `Calle Ejemplo 1, 46900 Valencia, Espa\u00f1a` |
| `COMPANY_EMAIL` | Email de contacto que aparece en la factura | `info@mitienda.com` |
| `COMPANY_PHONE` | Teléfono de contacto | `+34 600 000 000` |
| `COMPANY_IBAN` | IBAN para datos de pago en la factura | `ES91 2100 0418 4502 0005 1332` |
| `COMPANY_PRIMARY_COLOR` | Color principal del PDF (`#RRGGBB`) | `#1a1a2e` |
| `COMPANY_SECONDARY_COLOR` | Color secundario del PDF | `#16213e` |
| `COMPANY_ACCENT_COLOR` | Color de acento (botones, bordes) | `#e94560` |
| `COMPANY_TEXT_COLOR` | Color del texto principal | `#333333` |
| `COMPANY_LOGO_PATH` | Ruta absoluta a la imagen del logo | `C:/uploads/images/company/logo.png` |
| `APP_FRONTEND_URL` | URL base del frontend (usada en los enlaces de los emails) | `http://localhost:4200` |

### Variables de Stripe

El sistema de pagos utiliza [Stripe](https://stripe.com) como pasarela. Añade las siguientes variables a tu `.env`:

```properties
# Stripe
STRIPE_SECRET_KEY=sk_test_...
STRIPE_WEBHOOK_SECRET=whsec_...
STRIPE_CURRENCY=eur
```

| Variable | Descripción | Ejemplo |
|---|---|---|
| `STRIPE_SECRET_KEY` | Clave secreta de Stripe (test o producción) | `sk_test_4eC39Hq...` |
| `STRIPE_WEBHOOK_SECRET` | Secreto para verificar la firma de los webhooks de Stripe | `whsec_abc123...` |
| `STRIPE_CURRENCY` | Moneda por defecto para los PaymentIntents | `eur` |

> Para obtener estas claves, accede a [dashboard.stripe.com/apikeys](https://dashboard.stripe.com/apikeys). Para el webhook secret, crea un endpoint en [dashboard.stripe.com/webhooks](https://dashboard.stripe.com/webhooks) apuntando a `POST /stripe/webhook`.

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


## Sistema de Pagos con Stripe

El proyecto integra [Stripe](https://stripe.com) como pasarela de pagos usando el flujo **PaymentIntent**. La integración sigue el patrón recomendado por Stripe: el backend crea el `PaymentIntent` y devuelve el `clientSecret` al frontend, que lo usa con Stripe.js para confirmar el pago. El resultado se recibe de forma asíncrona mediante **webhooks**.

### Flujo de pago

```
1. Frontend → POST /stripe/payment-intent  { orderId }
2. Backend  → Stripe: crea PaymentIntent por el importe de la Order
3. Backend  → Frontend: devuelve { clientSecret, paymentIntentId, ... }
4. Frontend → Stripe.js: confirmCardPayment(clientSecret)
5. Stripe   → POST /stripe/webhook  (payment_intent.succeeded / failed / ...)
6. Backend  → actualiza Payment + Order en base de datos
```

### Componentes implementados

| Capa | Clase |
|---|---|
| **Configuración** | `StripeConfig` — lee `stripe.*` del `application.properties` y llama a `Stripe.apiKey` al arrancar |
| **Excepción** | `StripePaymentException` — `STRIPE_CANCEL_FAILED`, `STRIPE_REFUND_FAILED`, etc. |
| **Servicio** | `StripeService` — `createPaymentIntent`, `handleWebhookEvent` |
| **Controlador** | `StripeController` — 2 endpoints bajo `/stripe` |
| **Request DTO** | `CreatePaymentIntentRequestDTO` — `orderId` + `currency` opcional |
| **Response DTO** | `StripePaymentIntentResponseDTO` — `paymentIntentId`, `clientSecret`, `localPaymentId`, `orderId`, `amount`, `currency` |

### Eventos de webhook gestionados

| Evento Stripe | Acción en backend |
|---|---|
| `payment_intent.succeeded` | Avanza el Payment a `SUCCESS` → Order a `PAID` |
| `payment_intent.payment_failed` | Avanza el Payment a `FAILED` |
| `payment_intent.canceled` | Cancela el Payment → Order a `CANCELLED` |
| `charge.refunded` | Reembolsa el Payment → Order a `RETURNED` |

### Seguridad del webhook

`POST /stripe/webhook` es el **único endpoint público sin JWT**. La autenticidad de cada llamada se verifica mediante la firma `Stripe-Signature` con el `STRIPE_WEBHOOK_SECRET`. Si la firma no es válida, se rechaza con `400 Bad Request`.

---

## Ciclo de vida de Órdenes y Pagos

### `totalAmount` calculado en el backend

El importe total de una orden **siempre se calcula en el servidor** a partir de los precios reales de cada producto en base de datos. El frontend solo envía `productId` y `quantity`; el backend aplica el precio efectivo:

```
effectivePrice = discountPrice > 0 ? discountPrice : price
totalAmount    = Σ (effectivePrice × quantity)
```

Los `OrderItems` son un **snapshot inmutable** — se copian todos los campos del producto en el momento de creación de la orden, de forma que cambios futuros en el catálogo no afectan a pedidos históricos.

### Transiciones de estado de un Payment

```
CREATED → PENDING → AUTHORIZED → SUCCESS → REFUNDED
                 ↘           ↘
                  FAILED       FAILED
CREATED/PENDING/AUTHORIZED → CANCELLED  (cancelación manual)
```

Los estados `FAILED`, `EXPIRED`, `CANCELLED` y `REFUNDED` son **terminales**: intentar avanzar desde ellos lanza `HTTP 409 PAYMENT_INVALID_STATUS_TRANSITION`.

### Transiciones de estado de una Order

```
CREATED → PAID → PROCESSING → SHIPPED → DELIVERED → RETURNED
       ↘ (cualquier estado no terminal)
        CANCELLED
```

El estado de la orden se recalcula **automáticamente** a partir del estado agregado de sus pagos. No se puede cancelar una orden ya en estado `DELIVERED`, `RETURNED` o `CANCELLED`.

### Componentes del ciclo de vida

| Capa | Clase | Responsabilidad |
|---|---|---|
| **Request DTOs** | `OrderRequestDTO`, `OrderItemRequestDTO` | Reciben `userId` + lista de `{ productId, quantity }` |
| **Servicio** | `OrderService.saveNewOrder` | Calcula `totalAmount`, crea `OrderItems` como snapshot |
| **Servicio** | `OrderService.nextStatus` | Avanza el estado de la orden con guardas de transición |
| **Servicio** | `PaymentService.cancelPayment` | Cancela el pago si está en `CREATED/PENDING/AUTHORIZED` |
| **Servicio** | `PaymentService.refundPayment` | Reembolsa el pago si está en `SUCCESS` |
| **Servicio** | `PaymentService.updateOrderStatusFromPayments` | Sincroniza el estado de la orden con sus pagos |

---

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

## Sistema de Facturas PDF

El proyecto incluye un subsistema completo de generación de facturas en formato PDF, conforme al RD 1619/2012 (Reglamento de facturación español).

### Modelo de datos

```
Order ──── Invoice  (relación 1:1, unique constraint en order_id)
             ├── invoiceNumber  INV-YYYY-NNNNNN  (correlativo anual)
             ├── pdfPath        ruta absoluta en disco
             ├── status         GENERATED | ERROR
             ├── issueDate      LocalDate
             ├── totalAmount    BigDecimal
             └── createdAt      LocalDateTime
```

### Componentes implementados

| Capa | Clases |
|---|---|
| **Entidad** | `Invoice` |
| **Enumerado** | `InvoiceStatus` — `GENERATED`, `ERROR` |
| **Excepción** | `InvoiceException` |
| **Repositorio** | `InvoiceRepository` — consulta con `@Lock(PESSIMISTIC_WRITE)` para numeración correlativa segura |
| **Servicio** | `InvoiceService` — generación, numeración, renderizado PDF, recuperación de bytes |
| **DTOs** | `InvoiceDataDTO`, `InvoiceLineDTO`, `InvoiceCustomerDTO`, `CompanyDTO`, `TaxSummaryDTO` |
| **Configuración** | `CompanyPropertiesConfig` — mapea todas las variables `COMPANY_*` |
| **Template** | `src/main/resources/templates/emails/orders/invoice-default.html` — plantilla Thymeleaf A4 |
| **Controlador** | `InvoiceController` — 4 endpoints bajo `/invoices` |

### Numeración correlativa (RD 1619/2012)

El método `buildInvoiceNumber()` está declarado `synchronized` y usa `@Lock(PESSIMISTIC_WRITE)` en el repositorio para garantizar que no se emitan dos facturas con el mismo número, incluso bajo carga concurrente. El contador se reinicia automáticamente cada año: `INV-2025-000001`, `INV-2025-000002`, …, `INV-2026-000001`.

### Generación PDF (OpenHTMLtoPDF + Jsoup)

1. Thymeleaf renderiza `invoice-default.html` → String HTML
2. Jsoup parsea el HTML y establece charset UTF-8
3. `W3CDom` convierte a `org.w3c.dom.Document`
4. `PdfRendererBuilder.withW3cDocument()` genera el PDF (**necesario para codificar caracteres especiales como `ñ` correctamente**)
5. El PDF se guarda en `INVOICES_STORAGE_PATH/{year}/{invoiceNumber}.pdf`

### Fuentes y caracteres especiales

OpenHTMLtoPDF requiere una fuente con soporte Latin Extended para renderizar `ñ`, tildes, etc. El servicio detecta automáticamente:
- **Windows**: `C:/Windows/Fonts/arial.ttf` + `arialbd.ttf`
- **Linux/Docker**: DejaVu Sans o Liberation Sans (Alpine: `apk add ttf-dejavu`)

La variable `COMPANY_ADDRESS` en `.env` debe escapar la `ñ` como `\u00f1`:
```properties
COMPANY_ADDRESS=Calle Ejemplo 1, 46900 Valencia, Espa\u00f1a
```

### Comportamiento idempotente

`POST /invoices/orders/{orderId}` es idempotente:
- Si ya existe una factura para ese pedido **y el PDF existe en disco**, devuelve la factura existente.
- Si la entidad existe pero el PDF se perdió (reinicio, migración), regenera el PDF automáticamente.
- Solo crea una nueva factura si no existe ningún registro previo.

### Dockerfile — fuente DejaVu (Alpine)

```dockerfile
RUN apk add --no-cache ttf-dejavu
```

---

## Rate Limiting

Los endpoints de autenticación y exportación de datos están protegidos con rate limiting basado en token bucket ([Bucket4j](https://github.com/bucket4j/bucket4j) en memoria).

### Límites configurados

| Endpoint | Límite | Ventana | Clave |
|---|---|---|---|
| `POST /auth/login` | 5 intentos | 15 minutos | IP del cliente |
| `POST /auth/register` | 10 peticiones | 1 hora | IP del cliente |
| `POST /auth/password-reset/request` | 3 intentos | 1 hora | IP del cliente |
| `GET /users/me/data-export` | 1 exportación | 24 horas | Username (JWT) |

### Comportamiento

- Si se supera el límite, la API responde `429 Too Many Requests` con el header `Retry-After: <segundos>`.
- Cuando el login es exitoso, el contador de intentos fallidos se **resetea** para esa IP — el usuario legítimo comienza limpio.
- El contador de `password-reset/request` **nunca se resetea** aunque sea exitoso — protección anti-enumeración de cuentas.
- El límite de exportación de datos se aplica por username, no por IP.

### Componentes

| Clase | Responsabilidad |
|---|---|
| `RateLimitService` | Gestiona los buckets en un `ConcurrentHashMap<String, Bucket>` keyed por `"identifier:ENDPOINT_TYPE"` |
| `RateLimitException` | Extiende `NoHandlerException`; lleva el campo `retryAfterSeconds` |
| `GlobalExceptionHandler` | Captura `RateLimitException` → `429` con header `Retry-After` |

### IP del cliente

El servicio respeta la cabecera `X-Forwarded-For` para entornos detrás de un proxy o balanceador de carga. Si no está presente, usa `request.getRemoteAddr()`.

---

## RGPD — Protección de datos

### Art. 7 — Consentimiento de términos y condiciones

En el momento del registro, el usuario debe aceptar explícitamente los términos y condiciones. La versión vigente se configura en `application.properties`:

```properties
app.terms.current-version=1.0
```

Si `termsAccepted: false` en el body de registro, la API responde `400 Bad Request` con `TERMS_NOT_ACCEPTED`.

#### Columnas añadidas a `users`

| Columna | Tipo | Descripción |
|---|---|---|
| `terms_accepted_at` | `DATETIME` nullable | Fecha y hora de aceptación de los términos |
| `terms_version` | `VARCHAR` nullable | Versión de los términos aceptados |

#### Endpoint

```
GET /users/me/terms
Authorization: Bearer <token>
```

Devuelve `{ "termsVersion": "1.0", "termsAcceptedAt": "2026-03-02T10:00:00" }`.

---

### Art. 17 — Derecho al olvido (eliminación de cuenta)

`DELETE /users/me` anonimiza y desactiva la cuenta del usuario autenticado:

1. Se envía un email de notificación al **email real** antes de anonimizar.
2. Se reemplazan los datos personales por valores neutros.
3. Se desactiva la cuenta (`isActive=false`, `isDeleted=true`, `deletedAt=now()`).
4. Los pedidos e historial se conservan **de forma anónima** (FK al usuario anonimizado intacta).

#### Campos anonimizados

| Campo | Valor después de anonimizar |
|---|---|
| `name` | `"Usuario"` |
| `surname` | `"Eliminado"` |
| `username` | `"deleted_{id}"` |
| `email` | `"deleted_{id}@eliminado.local"` |
| `phone` | `"0000000000"` |
| `profileImageUrl` | `null` |
| `resetToken` / `resetTokenExpiry` | `null` |
| `termsAcceptedAt` / `termsVersion` | `null` |

> Los valores `username` y `email` incluyen el ID de la entidad para garantizar la unicidad en base de datos.

#### Obligación legal

Los pedidos e historial de compras se conservan anonimizados durante **10 años** conforme al Art. 30 del Código de Comercio y la LSSICE.

#### Componentes

| Clase | Responsabilidad |
|---|---|
| `UserDeletionService` | Orquesta anonimización y llama al servicio de email |
| `account-deletion-notification.html` | Plantilla del email de notificación |

---

### Art. 20 — Derecho a la portabilidad de datos

`GET /users/me/data-export` devuelve todos los datos personales del usuario en formato JSON.

#### Rate limit

1 exportación por día por usuario (identificado por el JWT). Si se supera, responde `429 Too Many Requests` con `Retry-After: 86400`.

#### Estructura del JSON

```json
{
  "schemaVersion": "1.0",
  "exportedAt": "2026-03-02T10:15:00",
  "profile": { "id": 1, "name": "...", "email": "...", "termsVersion": "1.0", ... },
  "addresses": [ { "addressLine1": "...", "city": "...", ... } ],
  "orders": [ { "id": 1, "status": "DELIVERED", "totalAmount": 49.99, ... } ],
  "invoices": [ { "invoiceNumber": "INV-2026-000001", "totalAmount": 49.99, ... } ],
  "payments": [ { "id": 1, "orderId": 1, "status": "SUCCESS", "amount": 49.99, ... } ]
}
```

#### Componentes

| Clase | Responsabilidad |
|---|---|
| `DataExportService` | Consulta y mapea todos los datos del usuario |
| `DataExportResponseDTO` | DTO raíz con 5 inner classes: `ProfileExport`, `AddressExport`, `OrderExport`, `InvoiceExport`, `PaymentExport` |

---

## Email asíncrono

Todos los envíos de email se realizan de forma **asíncrona** para no bloquear la respuesta HTTP al cliente. Si un email falla, el error se loguea pero la operación de negocio (pedido creado, contraseña actualizada, cuenta eliminada) no se ve afectada.

### Arquitectura

| Clase | Responsabilidad |
|---|---|
| `AsyncConfig` | Habilita `@EnableAsync` y define el bean `emailTaskExecutor` (ThreadPool: 2 core / 4 max / queue 50, prefijo `email-async-`) |
| `UserEmailNotificationService` | Centraliza los emails de usuario: reset de contraseña, cambio de contraseña, eliminación de cuenta. Cada método lleva `@Async("emailTaskExecutor")` |
| `OrderConfirmationEmailService` | Email de confirmación de pedido con `@Async("emailTaskExecutor")` |

> **Nota importante**: para que `@Async` funcione correctamente, los métodos deben estar en un bean **distinto** al que los invoca. Spring AOP usa proxies y no intercepta auto-llamadas dentro del mismo bean.

---

## Email de confirmación de pedido

Al crear un pedido mediante `POST /orders`, se envía automáticamente un email de confirmación al usuario. El envío es asíncrono — el cliente recibe la respuesta `201 Created` inmediatamente.

### Contenido del email

- Nombre del usuario
- Número y fecha del pedido
- Tabla de ítems con nombre, cantidad y precio efectivo (precio con descuento si aplica)
- Total del pedido
- Dirección de envío (snapshot del pedido)
- Botón `Ver pedido` enlazado a `{APP_FRONTEND_URL}/orders/{orderId}`
- Logo de empresa (si está configurado)

### Componentes

| Clase / Archivo | Responsabilidad |
|---|---|
| `OrderConfirmationEmailService` | Construye el contexto Thymeleaf y delega el envío al `MailService` |
| `order-confirmation.html` | Plantilla del email con tabla de ítems y bloque de dirección condicional |

---

## Restablecimiento de contraseña

El sistema implementa el flujo estándar de recuperación de cuenta mediante email.

### Flujo

```
1. POST /auth/password-reset/request  { email }
   → genera token UUID con validez de 1 hora
   → guarda resetToken + resetTokenExpiry en el usuario
   → envía email con enlace usando la plantilla password-reset.html

2. El usuario hace clic en el enlace del email
   → el frontend redirige a la pantalla de nueva contraseña con el token en la URL

3. POST /auth/password-reset/confirm  { token, newPassword }
   → valida el token y su expiración
   → guarda la contraseña codificada con BCrypt
   → limpia resetToken y resetTokenExpiry
   → envía email de notificación usando password-change-notification.html
```

### Variables de entorno necesarias

```properties
APP_FRONTEND_URL=http://localhost:4200
```

El enlace del email se construye como `{APP_FRONTEND_URL}/reset-password?token={token}`.

### Seguridad

- Ambos endpoints son **públicos** (bajo `/auth/**`, ya excluido del filtro JWT).
- Si el email no existe, `/request` responde **200 OK igualmente** — no revela qué cuentas están registradas.
- El token expira a la hora. Intentar usarlo caducado devuelve **410 Gone** (`RESET_TOKEN_EXPIRED`).
- Token inválido devuelve **400 Bad Request** (`INVALID_RESET_TOKEN`).
- Tras un uso exitoso el token se anula inmediatamente, por lo que **no se puede reutilizar**.

### Columnas añadidas a `users`

| Columna | Tipo | Descripción |
|---|---|---|
| `reset_token` | `VARCHAR` nullable | Token UUID generado al solicitar el reseteo |
| `reset_token_expiry` | `DATETIME` nullable | Fecha/hora de expiración del token |

---

## Logo de empresa

`POST /company/logo` permite subir o reemplazar el logotipo que aparece en las facturas PDF y los emails. El archivo se guarda en `{IMAGES_STORAGE_PATH}/company/logo.{ext}` y la ruta devuelta en la respuesta es la que debe configurarse en `COMPANY_LOGO_PATH`.

### Uso

```
POST /company/logo
Content-Type: multipart/form-data
Authorization: Bearer <token SUPER_ADMIN>

field: file = <imagen .jpg o .png>
```

Respuesta `200 OK`:
```
C:/uploads/images/company/logo.png
```

Actualiza `COMPANY_LOGO_PATH` en `.env` con la ruta recibida y reinicia la aplicación para que las facturas usen el nuevo logo.

### Validaciones

- MIME type: solo `image/jpeg` o `image/png`
- Extensión: `.jpg`, `.jpeg` o `.png`
- Magic bytes: verifica la firma real del archivo para prevenir spoofing de MIME

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
  - `POST /auth/password-reset/request` — solicita restablecimiento (envía email) · **público**
  - `POST /auth/password-reset/confirm` — confirma con token y nueva contraseña · **público**
- Empresa:
  - `POST /company/logo` — sube o reemplaza el logo (`multipart/form-data`, campo `file`) · solo `SUPER_ADMIN`
- Usuarios:
  - `GET /users`
  - `GET /users/{id}`
  - `POST /users`
  - `PUT /users`
  - `DELETE /users/{id}`
  - `POST /users/ban/{id}`
  - `POST /users/upload/{id}` — sube foto de perfil (`multipart/form-data`, campo `file`)
  - `GET /users/me/terms` — versión y fecha de aceptación de los T&C del usuario autenticado · JWT requerido
  - `DELETE /users/me` — anonimiza y elimina la propia cuenta (RGPD Art. 17) · JWT requerido
  - `GET /users/me/data-export` — exporta todos los datos personales en JSON (RGPD Art. 20) · JWT requerido · rate limit 1/día
- Imágenes (recursos estáticos públicos):
  - `GET /images/default/default_pic_profile.jpeg`
  - `GET /images/{userId}/pic-profile/{filename}`
- Direcciones:
  - `GET /address/{id}`
  - `POST /address`
  - `PUT /address`
  - `DELETE /address/{id}`
- Órdenes y pagos:
  - `GET /orders/users/{id}` — lista órdenes de un usuario
  - `GET /orders/{id}` — detalle de una orden
  - `POST /orders` — crea una orden (calcula `totalAmount` en backend) · `201 Created`
  - `POST /orders/next-status?orderId=&cancelled=&returned=` — avanza el estado de una orden
  - `POST /orders/{orderId}/payments` — registra un nuevo pago
  - `GET /orders/{orderId}/payments/last` — último pago válido de una orden
  - `POST /orders/payments/{paymentId}/nextStatus?targetStatus=` — avanza el estado de un pago
  - `POST /orders/payments/{paymentId}/cancel` — cancela un pago (`CREATED/PENDING/AUTHORIZED`) · idempotente
  - `POST /orders/payments/{paymentId}/refund` — reembolsa un pago (`SUCCESS`) · idempotente
- Stripe:
  - `POST /stripe/payment-intent` — crea un `PaymentIntent` en Stripe y devuelve el `clientSecret` · JWT requerido
  - `POST /stripe/webhook` — recibe eventos de Stripe (firma verificada) · **público**
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
- Facturas (PDF):
  - `POST /invoices/orders/{orderId}` — genera (o recupera) la factura PDF de un pedido · `201 Created` · idempotente
  - `GET /invoices/users/{userId}` — lista todas las facturas de un usuario · `200 OK`
  - `GET /invoices/{invoiceNumber}` — metadatos de una factura por su número (ej. `INV-2026-000001`) · `200 OK`
  - `GET /invoices/{invoiceNumber}/pdf` — descarga el archivo PDF · `200 OK` · `Content-Type: application/pdf`
- Mail:
  - `POST /mails/testing/send`
- Emails transaccionales (enviados automáticamente, sin endpoint directo):
  - Confirmación de pedido — `POST /orders` dispara el envío
  - Restablecimiento de contraseña — `POST /auth/password-reset/request`
  - Notificación de cambio de contraseña — `POST /auth/password-reset/confirm`
  - Notificación de eliminación de cuenta — `DELETE /users/me`

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
