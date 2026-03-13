---
description: Usar cuando se crea un nuevo módulo de negocio o una nueva sección dentro de core/
applyTo: 'src/main/java/es/marcha/backend/**/*.java'
---

# Skill: Crear un módulo nuevo

Sigue esta guía al generar o modificar cualquier módulo de `modules/` o sección de `core/`.

## 🔴 FLUJO DE TRABAJO OBLIGATORIO

Crear módulos en **3 FASES secuenciales**, cada una en PR separadas pequeñas:

### **FASE 1: Modelo de Datos**
Archivos a crear:
- ✅ Entidades JPA (`domain/model/`)
- ✅ DTOs Request/Response (`application/dto/`)
- ✅ Mappers (`application/mapper/`)
- ✅ Enums si aplica (`domain/enums/`)

**Regla de división**: Si esta fase supera **900 líneas**, dividir en PRs separadas:
- PR 1a: Solo entidades
- PR 1b: DTOs + Mappers

### **FASE 2: Lógica de Negocio**
Archivos a crear:
- ✅ Excepciones del módulo (`domain/exception/`)
- ✅ Repositorios (`infrastructure/persistence/`)
- ✅ Servicios (`application/service/`)

**Regla de división**: Si esta fase supera **900 líneas**, dividir en PRs separadas:
- PR 2a: Exceptions + Repository
- PR 2b: Services

### **FASE 3: Presentación y Utilidades**
Archivos a crear:
- ✅ Utilidades si aplica (`application/util/` o raíz del módulo)
- ✅ Controladores REST (`presentation/controller/`)
- ✅ Configuración del módulo si aplica (`<Nombre>ModuleConfig.java`)
- ✅ Documentación (README.md del módulo si es necesario)

**Regla de división**: Si esta fase supera **900 líneas**, dividir en PRs separadas:
- PR 3a: Utils + Config
- PR 3b: Controllers

### 🧪 **TESTS - OBLIGATORIO**
**REGLA ABSOLUTA**: Cada fase DEBE incluir sus tests correspondientes, o en su defecto, crear una PR final exclusiva de tests.

- **Opción recomendada**: Añadir tests en la misma PR de cada fase
  - Fase 1 → Tests de Mappers
  - Fase 2 → Tests de Service (unit tests)
  - Fase 3 → Tests de Controller (integration tests)

- **Opción alternativa**: Si las PRs quedan muy grandes, crear PR final:
  - PR Final: Tests completos del módulo (unit + integration)

**⚠️ NUNCA crear un módulo sin tests**. Los tests son tan importantes como el código de producción.

---

## Estructura de carpetas obligatoria

```
src/main/java/es/marcha/backend/modules/<nombre>/
├── domain/
│   ├── model/          ← Entidades JPA (@Entity) y value objects
│   └── enums/          ← Enumerados propios del dominio
├── application/
│   ├── service/        ← Lógica de negocio (@Service)
│   ├── dto/
│   │   ├── request/    ← *RequestDTO con Bean Validation
│   │   └── response/   ← *ResponseDTO
│   └── mapper/         ← Clases *Mapper (conversión entity ↔ DTO)
├── infrastructure/
│   └── persistence/    ← Interfaces *Repository extends JpaRepository
└── presentation/
    └── controller/     ← @RestController con endpoints REST
```

Si el módulo necesita configuración propia, añadir `<Nombre>ModuleConfig.java` en la raíz del módulo.

## Convenciones de nombre

| Elemento | Patrón | Ejemplo |
|---|---|---|
| Entidad | PascalCase | `Coupon` |
| Servicio | `<Nombre>Service` | `CouponService` |
| Repositorio | `<Nombre>Repository` | `CouponRepository` |
| Controlador | `<Nombre>Controller` | `CouponController` |
| Request DTO | `<Nombre>RequestDTO` | `CouponRequestDTO` |
| Response DTO | `<Nombre>ResponseDTO` | `CouponResponseDTO` |
| Mapper | `<Nombre>Mapper` | `CouponMapper` |
| Excepción | `<Nombre>Exception` | `CouponException` |

## Excepción del módulo

Crear siempre una clase de excepción propia que extienda `NoHandlerException`:

```java
package es.marcha.backend.modules.<nombre>.domain.exception; // o core.error.exception

public class CouponException extends NoHandlerException {
    public static final String DEFAULT        = "COUPON_NOT_FOUND";
    public static final String ALREADY_USED   = "COUPON_ALREADY_USED";
    public static final String EXPIRED        = "COUPON_EXPIRED";

    public CouponException() { this(DEFAULT); }
    public CouponException(String msg) { super(msg); }
}
```

El `GlobalExceptionHandler` en `core/error/` la capturará automáticamente si extiende `NoHandlerException`.

## Inyección de dependencias

**Siempre por constructor.** Nunca `@Autowired` en campo.

```java
// ✅ Correcto
@Service
public class CouponService {
    private final CouponRepository couponRepository;

    public CouponService(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }
}

// ❌ Incorrecto
@Service
public class CouponService {
    @Autowired
    private CouponRepository couponRepository;
}
```

## Controlador REST

```java
@RestController
@RequestMapping("/coupons")
public class CouponController {
    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponResponseDTO> getById(@PathVariable long id) {
        return ResponseEntity.ok(couponService.getById(id));
    }

    @PostMapping
    public ResponseEntity<CouponResponseDTO> create(@Valid @RequestBody CouponRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(couponService.create(dto));
    }
}
```

## DTOs — validaciones

Validar en el RequestDTO con Bean Validation. La lógica de negocio va en el Service, no en el Controller.

```java
public class CouponRequestDTO {
    @NotBlank
    private String code;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal discountPercent;

    @NotNull
    @FutureOrPresent
    private LocalDate expiresAt;
}
```

## Entidad JPA — checklist

- `@Entity` + `@Table(name = "...")` en snake_case.
- `@Id` + `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- Lombok: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder`.
- Relaciones bidireccionales: usar `@JsonManagedReference` / `@JsonBackReference` para evitar ciclos.
- Auditoría: añadir `createdAt` con `LocalDateTime` si necesita trazabilidad.

## Emails desde el módulo

Si el módulo envía emails, delegarlos a un bean separado anotado con `@Async("emailTaskExecutor")`:

```java
@Service
public class CouponEmailService {
    private final MailService mailService;

    public CouponEmailService(MailService mailService) {
        this.mailService = mailService;
    }

    @Async("emailTaskExecutor")
    public void sendCouponEmail(String to, String code) {
        // Un fallo aquí NO debe propagar excepción al llamador
        try {
            mailService.send(...);
        } catch (Exception e) {
            log.error("Error enviando email de cupón a {}: {}", to, e.getMessage());
        }
    }
}
```

## Tests del módulo

**🔴 REGLA CRÍTICA**: Los tests son OBLIGATORIOS para cada módulo. No es opcional.

### Estrategias de testing por fase:

**FASE 1 - Tests de Modelo**:
- `@Test` para Mappers: verificar conversión Entity ↔ DTO correcta
- Validar que las validaciones de DTOs funcionan (`@Valid`)

**FASE 2 - Tests de Lógica**:
- `@ExtendWith(MockitoExtension.class)` para Service
  - Mock de Repository y dependencias externas
  - `@InjectMocks` en el Service bajo test
  - Verificar lógica de negocio, excepciones, edge cases

**FASE 3 - Tests de Presentación**:
- `@WebMvcTest(XController.class)` para Controllers
  - `@MockBean` del Service
  - `MockMvc` para simular requests HTTP
  - Verificar status codes, response bodies, validaciones

### Cobertura mínima esperada:
- ✅ **Happy paths**: flujos principales funcionan
- ✅ **Excepciones**: lanzamiento correcto de excepciones custom
- ✅ **Validaciones**: DTOs rechazan datos inválidos
- ✅ **Edge cases**: valores nulos, listas vacías, etc.

### Opciones de implementación:

**Opción A - Recomendada**: Tests en la misma PR de cada fase
```
PR: feat(module): añadir entidades y DTOs
  ├── src/main/java/.../domain/model/Entity.java
  ├── src/main/java/.../dto/EntityRequestDTO.java
  └── src/test/java/.../mapper/EntityMapperTest.java    ← Tests incluidos
```

**Opción B - Alternativa**: PR final dedicada a tests
```
PR: test(module): añadir cobertura completa del módulo
  ├── src/test/java/.../service/ServiceTest.java
  ├── src/test/java/.../controller/ControllerTest.java
  └── src/test/java/.../mapper/MapperTest.java
```

**⚠️ IMPORTANTE**: 
- Ejecutar `mvn clean test` antes de cada PR
- Todos los tests existentes + nuevos deben pasar al 100%
- Ver `testing.instructions.md` para patrones detallados y convenciones

---

## Checklist completo para crear un módulo

Usa esta lista antes de dar por terminado un módulo nuevo:

### Código de producción:
- [ ] Fase 1: Entidades JPA con Lombok
- [ ] Fase 1: DTOs Request con `@Valid` y Response
- [ ] Fase 1: Mappers Entity ↔ DTO
- [ ] Fase 2: Exception personalizada extendiendo `NoHandlerException`
- [ ] Fase 2: Repository interface extendiendo `JpaRepository`
- [ ] Fase 2: Service con lógica de negocio
- [ ] Fase 3: Controller REST con endpoints
- [ ] Fase 3: Inyección por constructor en todos los beans
- [ ] Config del módulo si es necesario

### Tests:
- [ ] Tests de Mappers (conversión entity ↔ DTO)
- [ ] Unit tests de Service con Mockito
- [ ] Integration tests de Controller con `@WebMvcTest`
- [ ] Todos los tests pasan: `mvn clean test` ✅

### Git:
- [ ] PRs pequeñas (< 900 líneas ideal, < 1000 máximo)
- [ ] Conventional commits en español
- [ ] Cada PR pasa tests independientemente
- [ ] Sin dependencias entre PRs

**⚠️ RECORDATORIO FINAL**: Un módulo sin tests NO está completo. Los tests son parte integral del desarrollo, no un extra opcional.
