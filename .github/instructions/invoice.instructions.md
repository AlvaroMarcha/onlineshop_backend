---
description: Usar cuando se trabaja con facturas PDF, numeración correlativa o el módulo invoice
applyTo: 'src/main/java/es/marcha/backend/modules/invoice/**'
---

# Skill: Facturas PDF

Módulo ubicado en `modules/invoice/`. Genera facturas conforme al **RD 1619/2012** (Reglamento de facturación español).

## Modelo de datos

```
Order ──── Invoice  (relación 1:1, unique constraint en order_id)
             ├── invoiceNumber  INV-YYYY-NNNNNN  (correlativo anual)
             ├── pdfPath        ruta absoluta en disco
             ├── status         GENERATED | ERROR
             ├── issueDate      LocalDate
             ├── totalAmount    BigDecimal
             └── createdAt      LocalDateTime
```

## Numeración correlativa (RD 1619/2012)

El método `buildInvoiceNumber()` es `synchronized` y usa `@Lock(PESSIMISTIC_WRITE)` en el repositorio.

```java
// InvoiceRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT i FROM Invoice i WHERE YEAR(i.issueDate) = :year ORDER BY i.invoiceNumber DESC")
Optional<Invoice> findLastByYear(@Param("year") int year);
```

- Formato: `INV-2026-000001`, `INV-2026-000002`, …, `INV-2027-000001` (reset anual automático).
- **No cambiar la estrategia de bloqueo** sin evaluar concurrencia — eliminarla causaría duplicados bajo carga.
- **No modificar el formato del número** — está regulado legalmente.

## Comportamiento idempotente de POST /invoices/orders/{orderId}

```
1. ¿Existe entidad Invoice para esta Order?
   NO  → crear nueva Invoice + generar PDF → 201 Created
   SÍ  → ¿Existe el archivo PDF en disco?
         SÍ  → devolver Invoice existente → 200 OK  (idempotente)
         NO  → regenerar PDF (recovery tras reinicio/migración) → 200 OK
```

## Pipeline de generación PDF

```
1. Thymeleaf renderiza invoice-default.html → String HTML
2. Jsoup parsea el HTML → establece charset UTF-8
3. W3CDom convierte a org.w3c.dom.Document
4. PdfRendererBuilder.withW3cDocument() → genera PDF
5. PDF se guarda en INVOICES_STORAGE_PATH/{year}/{invoiceNumber}.pdf
```

Usar siempre `PdfRendererBuilder.withW3cDocument()` (no `withHtmlContent()`): es necesario para codificar correctamente caracteres como `ñ` y tildes.

## Fuentes — caracteres especiales

OpenHTMLtoPDF necesita fuente con soporte Latin Extended. El servicio detecta automáticamente:
- **Windows**: `C:/Windows/Fonts/arial.ttf` + `arialbd.ttf`
- **Linux/Docker**: DejaVu Sans o Liberation Sans

En Docker (Alpine):
```dockerfile
RUN apk add --no-cache ttf-dejavu
```

En `.env`, escapar la `ñ` en la dirección de empresa:
```env
COMPANY_ADDRESS=Calle Ejemplo 1, 46900 Valencia, Espa\u00f1a
```

## Variables de empresa (CompanyPropertiesConfig)

Todas las variables `COMPANY_*` se mapean a `CompanyPropertiesConfig`:

| Variable | Descripción |
|---|---|
| `COMPANY_NAME` | Nombre legal del emisor |
| `COMPANY_NIF` | NIF/CIF |
| `COMPANY_ADDRESS` | Dirección fiscal |
| `COMPANY_EMAIL` | Email de contacto |
| `COMPANY_PHONE` | Teléfono |
| `COMPANY_IBAN` | IBAN para datos de pago |
| `COMPANY_PRIMARY_COLOR` | Color principal del PDF |
| `COMPANY_LOGO_PATH` | Ruta absoluta al logo |

## Endpoints

| Método | Path | Descripción |
|---|---|---|
| POST | `/invoices/orders/{orderId}` | Genera (o recupera) factura — idempotente |
| GET | `/invoices/users/{userId}` | Lista facturas de un usuario |
| GET | `/invoices/{invoiceNumber}` | Metadatos de una factura (ej. `INV-2026-000001`) |
| GET | `/invoices/{invoiceNumber}/pdf` | Descarga el PDF (`Content-Type: application/pdf`) |

## Excepciones

```java
InvoiceException.DEFAULT             // INVOICE_NOT_FOUND
InvoiceException.PDF_NOT_FOUND       // PDF_FILE_NOT_FOUND
InvoiceException.GENERATION_ERROR    // INVOICE_GENERATION_ERROR
```

## Tests del módulo

- Para la numeración correlativa: test de integración con `@DataJpaTest` + H2, verificar que dos llamadas concurrentes no generan el mismo número.
- Para la generación PDF: test unitario que mockea `InvoiceRepository` y verifica que se llama a `PdfRendererBuilder` con los parámetros correctos.
- Para el endpoint idempotente: test con `@WebMvcTest` que verifica 201 en primera llamada y 200 en segunda con el mismo `orderId`.
