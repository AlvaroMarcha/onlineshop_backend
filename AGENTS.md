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
| Crear módulo nuevo | `new-module.instructions.md` | Al crear un módulo o sección de `core` desde cero (**3 fases**: modelo → lógica → presentación) |
| Catálogo | `catalog.instructions.md` | Productos, categorías, variantes, reseñas |
| Pedidos y pagos | `orders-payments.instructions.md` | Order/Payment lifecycle, cupones |
| Stripe | `stripe.instructions.md` | PaymentIntent, webhooks, firma Stripe |
| Facturas | `invoice.instructions.md` | PDF, numeración correlativa, RGPD |
| Tests | `testing.instructions.md` | JUnit 5, Mockito, MockMvc, @DataJpaTest |
| Git y CI/CD | `git.instructions.md` | Flujo de ramas, conventional commits, workflows |

### 📋 Flujo para crear módulo nuevo (3 fases):
1. **FASE 1 - Modelo**: Entidades + DTOs + Mappers (+ tests)
2. **FASE 2 - Lógica**: Exceptions + Repository + Service (+ tests)
3. **FASE 3 - Presentación**: Utils + Controllers + Config (+ tests)

**Cada fase en PR separada** (< 900 líneas ideal). Tests OBLIGATORIOS en cada fase o PR final dedicada.

---

## Git y CI/CD

### 🔴 REGLAS CRÍTICAS ANTES DE CREAR PR
1. **Tests primero**: SIEMPRE ejecutar `mvn clean test` localmente - deben pasar al 100%
2. **PRs pequeños**: < 900 líneas (ideal), máximo 1000 líneas. Si es mayor, dividir en PRs más pequeños
3. **PRs autocontenidos**: Cada PR debe funcionar independientemente y pasar tests por sí solo
4. **❌ NUNCA crear PRs interdependientes** que necesiten mergearse entre sí para pasar tests
5. **❌ NUNCA hacer push con tests rotos** esperando "arreglarlo después"

### Flujo de trabajo (Automatizado)
- **develop**: rama principal de trabajo
- **main**: rama de producción con releases automáticas
- **Flujo en cascada**:
  1. Crear branch: `feature/`, `bugfix/`, `refactor/`, `hotfix/` + descripción-corta
  2. **ANTES del PR**: `mvn clean test` debe pasar al 100%
  3. PR a develop → requiere 1 aprobación manual del propietario + tests pasando
  4. develop → main automático cuando hay cambios aprobados
  5. main → release + deploy automático

### Conventional Commits (Obligatorio)
- **Formato**: `tipo(scope): descripción`
- **Tipos**: `feat`, `fix`, `refactor`, `perf`, `test`, `docs`, `style`, `chore`
- **Ejemplos**: 
  - `feat(catalog): añadir filtro por categoría`
  - `fix: resolver NPE en PaymentService`
- Commits y PRs en **español**, código en **inglés**

### CI/CD Pipeline
- **Tests automáticos**: ejecutados en cada PR
- **Aprobación manual requerida**: NUNCA mergear PRs sin aprobación explícita del propietario
- **Auto-merge**: solo después de aprobación manual + tests pasando
- **Releases**: semantic-release en main (versionado automático)
  - `feat:` → minor, `fix:` → patch, `BREAKING CHANGE` → major
- **Deploy**: automático a VPS vía Docker (ghcr.io)
- **CHANGELOG.md**: generado automáticamente
- **Branch cleanup**: branches mergeadas se borran automáticamente

### Límites PR
- **Warning**: > 900 líneas → label `size/large`
- **Bloqueado**: > 1000 líneas → merge rechazado

### Reglas para Agentes IA
- ✅ **ANTES de cualquier push**: Ejecutar `mvn clean test` - debe pasar al 100%
- ✅ Crear PRs pequeños (< 900 líneas ideal, < 1000 máximo)
- ✅ Cada PR debe ser autocontenido y pasar tests independientemente
- ✅ Dividir PRs grandes (> 900 líneas) automáticamente en PRs más pequeños y secuenciales
- ✅ **Al crear módulo nuevo**: seguir flujo de 3 fases (modelo → lógica → presentación), una PR por fase
- ✅ **Tests OBLIGATORIOS**: incluir tests en cada PR o crear PR final dedicada a tests
- ✅ Ejecutar tests locales antes de push
- ✅ Reportar estado de las PRs creadas
- ❌ **NUNCA crear PRs interdependientes** que requieran mergearse entre sí para pasar tests
- ❌ **NUNCA hacer push con tests rotos**
- ❌ **NUNCA crear módulo o feature sin tests** - los tests no son opcionales
- ❌ **NUNCA mergear PRs sin aprobación manual explícita del usuario**
- ❌ **NUNCA usar `gh pr merge` sin que el usuario lo solicite**
