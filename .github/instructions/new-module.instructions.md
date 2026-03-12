---
description: Usar cuando se crea un nuevo módulo de negocio o una nueva sección dentro de core/
applyTo: 'src/main/java/es/marcha/backend/**/*.java'
---

# Skill: Crear un módulo nuevo

Sigue esta guía al generar o modificar cualquier módulo de `modules/` o sección de `core/`.

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

- Unit tests del Service con `@ExtendWith(MockitoExtension.class)` + `@InjectMocks`.
- Integration tests del Controller con `@WebMvcTest` + `@MockBean` del Service.
- Ver `testing.instructions.md` para convenciones detalladas.
