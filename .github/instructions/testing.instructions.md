---
description: Usar cuando se escriben o revisan tests (JUnit 5, Mockito, MockMvc, @DataJpaTest)
applyTo: 'src/test/**'
---

# Skill: Tests

Convenciones y patrones de testing para este proyecto.

## Tipos de test y cuándo usarlos

| Tipo | Anotación | Cuándo |
|---|---|---|
| **Unit test** | `@ExtendWith(MockitoExtension.class)` | Lógica de negocio en Services (sin Spring context) |
| **Slice test (web)** | `@WebMvcTest(XController.class)` | Controllers REST — carga solo la capa web |
| **Slice test (JPA)** | `@DataJpaTest` | Repositorios JPA — usa H2 en memoria |
| **Integration test** | `@SpringBootTest` + `@AutoConfigureMockMvc` | Flujos E2E completos (usar con moderación) |

Preferir **unit tests** y **slice tests** sobre `@SpringBootTest` completo: son más rápidos y aislados.

## Estructura de carpetas

```
src/test/java/es/marcha/backend/
├── modules/
│   ├── order/
│   │   └── application/service/
│   │       ├── PaymentServiceTest.java
│   │       └── OrderServiceTest.java
│   └── catalog/
│       └── application/service/
│           └── ProductServiceTest.java
└── core/
    └── auth/
        └── presentation/controller/
            └── AuthControllerTest.java
```

Espeja la estructura de `src/main/` para facilitar la navegación.

## Unit test — patrón con Mockito

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService — transiciones de estado")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks                        // inyecta mocks por campo (compatible con @Autowired)
    private PaymentService paymentService;

    @Test
    @DisplayName("CREATED → PENDING: transición válida")
    void givenCreatedPayment_whenAdvanceToPending_thenStatusIsPending() {
        // Arrange
        Payment payment = Payment.builder()
            .id(1L).status(PaymentStatus.CREATED)
            .amount(49.99).createdAt(LocalDateTime.now())
            .transactionId("txn_001").provider("stripe")
            .build();

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(payment));

        // Act
        PaymentStatus result = paymentService.nextPaymentStatus(1L, PaymentStatus.PENDING);

        // Assert
        assertEquals(PaymentStatus.PENDING, result);
        verify(paymentRepository).save(payment);
    }
}
```

## Nomenclatura de tests

Usar el patrón `given_when_then` o `should_when`:

```
givenCreatedPayment_whenAdvanceToPending_thenStatusIsPending()
shouldThrowOrderException_whenPaymentIsTerminal()
givenExistingUser_whenLogin_thenReturnsJwtToken()
```

Siempre añadir `@DisplayName` con descripción en español o inglés legible.

## Web slice test — patrón con @WebMvcTest

**Atención:** este proyecto tiene filtros de seguridad propios (`JwtFilter`, `VerifiedUserFilter`) anotados con `@Component`. Deben excluirse explícitamente en cualquier `@WebMvcTest`, junto con `ModuleProperties` (dependencia de `ModuleFlagInterceptor`):

```java
@WebMvcTest(
        value = AuthController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
        }
)
@DisplayName("AuthController — endpoints públicos")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthService authService;
    @MockBean private ModuleProperties moduleProperties; // requerido por ModuleFlagInterceptor

    @Test
    @DisplayName("POST /auth/password-reset/request — responde 200 aunque el email no exista")
    void givenNonExistentEmail_whenPasswordResetRequest_thenReturns200() throws Exception {
        doNothing().when(authService).requestPasswordReset(any());

        mockMvc.perform(post("/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("email", "noexiste@test.com"))))
            .andExpect(status().isOk());
    }
}
```

## JPA slice test — patrón con @DataJpaTest

```java
@DataJpaTest
@DisplayName("InvoiceRepository — numeración correlativa")
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    @DisplayName("findLastByYear devuelve la factura con número más alto del año")
    void givenTwoInvoicesSameYear_whenFindLastByYear_thenReturnsHighestNumber() {
        // Arrange: persistir dos facturas con @DataJpaTest (H2 en memoria)
        Invoice first  = buildInvoice("INV-2026-000001", 2026);
        Invoice second = buildInvoice("INV-2026-000002", 2026);
        invoiceRepository.saveAll(List.of(first, second));

        // Act
        Optional<Invoice> last = invoiceRepository.findLastByYear(2026);

        // Assert
        assertTrue(last.isPresent());
        assertEquals("INV-2026-000002", last.get().getInvoiceNumber());
    }
}
```

## Reglas de negocio que siempre deben tener tests

| Regla | Tipo de test recomendado |
|---|---|
| Estados terminales de Payment (FAILED, CANCELLED, REFUNDED, EXPIRED) | Unit test de `PaymentService` |
| Transiciones inválidas de Payment | Unit test de `PaymentService` |
| totalAmount calculado en backend (no acepta valor del cliente) | Unit test de `OrderService` |
| Anti-enumeración en password-reset/request | `@WebMvcTest` de `AuthController` |
| Idempotencia de POST /invoices/orders/{orderId} | `@WebMvcTest` de `InvoiceController` |
| Rate limiting (responde 429 al superar límite) | `@SpringBootTest` + MockMvc |

## Convenciones adicionales

- No usar `@SpringBootTest` si un unit test o slice test es suficiente.
- Preparar fixtures con builders de Lombok (`Entity.builder()...build()`), no con setters.
- Usar `assertThrows` para verificar excepciones:
  ```java
  OrderException ex = assertThrows(OrderException.class,
      () -> paymentService.nextPaymentStatus(id, PaymentStatus.PENDING));
  assertTrue(ex.getMessage().contains(OrderException.TERMINAL_STATUS_PAYMENT));
  ```
- Los mocks de email (`MailService`, `UserEmailNotificationService`) deben mockearse en todos los tests que involucren creación de pedidos o autenticación.
- `@Transactional` en tests de `@DataJpaTest` hace rollback automático — no limpiar BBDD manualmente.

## Ejecutar los tests

```bash
mvn test                    # todos los tests
mvn test -pl . -Dtest=PaymentServiceTest   # test específico
mvn verify                  # tests + build completo
```
