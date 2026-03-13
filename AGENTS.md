# AGENTS.md

> Guía de referencia rápida para agentes de IA. Para instrucciones detalladas por dominio, consulta los archivos en `.github/instructions/`.

---

## ¿Qué es este proyecto?

Backend de una tienda online — **Java 21 + Spring Boot 3.x + Maven**.  
API REST consumida por un frontend Angular. Gestiona usuarios, catálogo, pedidos, pagos (Stripe), facturas PDF, emails y RGPD.

---

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.x |
| DB | MariaDB / MySQL + Spring Data JPA |
| Seguridad | Spring Security + JWT |
| Pagos | Stripe (PaymentIntent + Webhooks) |
| Email | Gmail SMTP + OAuth2 (XOAUTH2) |
| PDF | OpenHTMLtoPDF + Thymeleaf |
| Rate limiting | Bucket4j |
| Tests | JUnit 5 + Mockito + MockMvc |

---

## Arquitectura

```
src/main/java/es/marcha/backend/
├── core/      ← auth · config · error · filestorage · notification · security · user
└── modules/   ← cart · catalog · company · coupon · invoice · notification · order · wishlist
```

Cada módulo tiene **4 capas fijas**: `domain/` → `application/` → `infrastructure/` → `presentation/`

**Regla absoluta**: Controller → Service → Repository. Nunca saltarse capas ni acceder a repositorios de otro módulo directamente.

---

## Convenciones esenciales

- **Inyección siempre por constructor** — nunca `@Autowired` en campo.
- Código en **inglés**; comentarios de lógica compleja en **español**.
- DTOs con sufijos `RequestDTO` / `ResponseDTO`. Mappers con sufijo `Mapper`.
- Excepciones: una clase por módulo (ej. `OrderException`) con constantes. Capturadas en `GlobalExceptionHandler`.
- Emails: siempre `@Async("emailTaskExecutor")` en un bean **diferente** al invocador.

---

## Reglas de negocio no negociables

- `totalAmount` se calcula en backend a partir de precios reales. El frontend solo envía `productId + quantity`.
- `Payment`: estados `FAILED`, `CANCELLED`, `EXPIRED`, `REFUNDED` son **terminales** → HTTP 409 si se intenta avanzar.
- `Order`: el estado se recalcula automáticamente desde el estado agregado de sus pagos (`PaymentService.updateOrderStatusFromPayments`). No modificar manualmente.
- `POST /stripe/webhook` es el **único** endpoint público fuera de `/auth/**` e `/images/**`.
- Facturas: numeración `INV-YYYY-NNNNNN` con `@Lock(PESSIMISTIC_WRITE)`. Endpoint idempotente.
- `/auth/password-reset/request` responde **200 OK aunque no exista el email** (anti-enumeración).
- `DELETE /users/me` **anonimiza** la cuenta, no la borra (RGPD / Cód. Comercio 10 años).

---

## Skills disponibles

| Skill | Archivo | Cuándo usarla |
|---|---|---|
| Crear módulo nuevo | `new-module.instructions.md` | Al crear un módulo o sección de `core` desde cero |
| Catálogo | `catalog.instructions.md` | Productos, categorías, variantes, reseñas |
| Pedidos y pagos | `orders-payments.instructions.md` | Order/Payment lifecycle, cupones |
| Stripe | `stripe.instructions.md` | PaymentIntent, webhooks, firma Stripe |
| Facturas | `invoice.instructions.md` | PDF, numeración correlativa, RGPD |
| Tests | `testing.instructions.md` | JUnit 5, Mockito, MockMvc, @DataJpaTest |
| Git y CI/CD | `git.instructions.md` | Flujo de ramas, conventional commits, workflows |

---

## Git y CI/CD

### Flujo de trabajo (Automatizado)
- **develop**: rama principal de trabajo
- **main**: rama de producción con releases automáticas
- **Flujo en cascada**:
  1. Crear branch: `feature/`, `bugfix/`, `refactor/`, `hotfix/` + descripción-corta
  2. PR a develop → auto-merge si tests pasan + 1 aprobación
  3. develop → main automático cuando hay cambios aprobados
  4. main → release + deploy automático

### Conventional Commits (Obligatorio)
- **Formato**: `tipo(scope): descripción`
- **Tipos**: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `style`, `chore`
- **Ejemplos**: 
  - `feat(catalog): añadir filtro por categoría`
  - `fix: resolver NPE en PaymentService`
- Commits y PRs en **español**, código en **inglés**

### CI/CD Pipeline
- **Tests automáticos**: ejecutados en cada PR
- **Auto-merge**: cuando tests pasan + 1 aprobación
- **Releases**: semantic-release en main (versionado automático)
  - `feat:` → minor, `fix:` → patch, `BREAKING CHANGE` → major
- **Deploy**: automático a VPS vía Docker (ghcr.io)
- **CHANGELOG.md**: generado automáticamente
- **Branch cleanup**: branches mergeadas se borran automáticamente

### Límites PR
- **Warning**: > 500 líneas → label `size/large`
- **Bloqueado**: > 1000 líneas → merge rechazado
