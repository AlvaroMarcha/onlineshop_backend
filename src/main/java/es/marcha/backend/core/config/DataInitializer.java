package es.marcha.backend.core.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import es.marcha.backend.core.shared.domain.enums.RoleName;
import es.marcha.backend.core.user.domain.model.Role;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.user.infrastructure.persistence.RoleRepository;
import es.marcha.backend.core.user.infrastructure.persistence.UserRepository;
import es.marcha.backend.modules.catalog.application.service.CategoryService;
import es.marcha.backend.modules.catalog.application.service.ProductService;
import es.marcha.backend.modules.catalog.application.service.SubcategoryService;
import es.marcha.backend.modules.catalog.domain.model.Category;
import es.marcha.backend.modules.catalog.domain.model.Subcategory;
import es.marcha.backend.modules.catalog.domain.model.product.Product;
import es.marcha.backend.modules.catalog.infrastructure.persistence.CategoryRepository;
import es.marcha.backend.modules.catalog.infrastructure.persistence.SubcategoryRepository;
import lombok.RequiredArgsConstructor;

/**
 * Inicializador de datos esenciales del sistema.
 *
 * <p>
 * Se ejecuta una vez al arranque de la aplicación (tras el contexto listo)
 * y garantiza que los roles, usuarios administradores, categorías y productos
 * de demostración existan en la base de datos.
 * </p>
 *
 * <p>
 * Idempotente: si los datos ya existen, no realiza ninguna operación.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final CategoryService categoryService;
    private final SubcategoryService subcategoryService;
    private final ProductService productService;

    // Configuración de usuarios administradores desde .env
    @Value("${app.seed.admin.username}")
    private String adminUsername;
    @Value("${app.seed.admin.email}")
    private String adminEmail;
    @Value("${app.seed.admin.password}")
    private String adminPassword;
    @Value("${app.seed.admin.phone}")
    private String adminPhone;

    @Value("${app.seed.superadmin.username}")
    private String superAdminUsername;
    @Value("${app.seed.superadmin.email}")
    private String superAdminEmail;
    @Value("${app.seed.superadmin.password}")
    private String superAdminPassword;
    @Value("${app.seed.superadmin.phone}")
    private String superAdminPhone;

    @Value("${app.terms.current-version}")
    private String termsVersion;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDatabase() {
        seedRoles();
        seedAdminUsers();
        seedCategories();
        seedSubcategories();
        seedDemoProducts();
    }

    /**
     * Crea los 7 roles del sistema si no existen.
     */
    private void seedRoles() {
        List<String> existingRoles = roleRepository.findAll()
                .stream()
                .map(Role::getName)
                .toList();

        List<String> requiredRoles = List.of(
                RoleName.ROLE_SUPER_ADMIN.name(),
                RoleName.ROLE_ADMIN.name(),
                RoleName.ROLE_ORDERS.name(),
                RoleName.ROLE_CUSTOMERS_INVOICES.name(),
                RoleName.ROLE_SUPPORT.name(),
                RoleName.ROLE_STORE.name(),
                RoleName.ROLE_USER.name());

        boolean anyCreated = false;
        for (String roleName : requiredRoles) {
            if (!existingRoles.contains(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description(getDescription(roleName))
                        .createdAt(LocalDateTime.now())
                        .build();
                roleRepository.save(role);
                log.info("[DataInitializer] Rol creado: {}", roleName);
                anyCreated = true;
            }
        }

        if (!anyCreated) {
            log.info("[DataInitializer] Roles del sistema ya presentes.");
        }
    }

    /**
     * Crea dos usuarios administradores: Admin y Super Admin.
     * Requiere que los roles ya existan.
     */
    private void seedAdminUsers() {
        // Super Admin
        if (userRepository.findByUsername(superAdminUsername).isEmpty()) {
            Role superAdminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN.name())
                    .orElseThrow(() -> new RuntimeException("ROLE_SUPER_ADMIN no existe — ejecutar seedRoles primero"));

            User superAdmin = User.builder()
                    .name("Super")
                    .surname("Admin")
                    .username(superAdminUsername)
                    .email(superAdminEmail)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .phone(superAdminPhone)
                    .role(superAdminRole)
                    .isActive(true)
                    .isVerified(true)
                    .isBanned(false)
                    .isDeleted(false)
                    .createdAt(LocalDateTime.now())
                    .termsAcceptedAt(LocalDateTime.now())
                    .termsVersion(termsVersion)
                    .build();

            userRepository.save(superAdmin);
            log.info("[DataInitializer] Usuario creado: {} (ROLE_SUPER_ADMIN)", superAdminUsername);
        } else {
            log.info("[DataInitializer] Usuario '{}' ya existe.", superAdminUsername);
        }

        // Admin
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN.name())
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN no existe — ejecutar seedRoles primero"));

            User admin = User.builder()
                    .name("Admin")
                    .surname("Sistema")
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .phone(adminPhone)
                    .role(adminRole)
                    .isActive(true)
                    .isVerified(true)
                    .isBanned(false)
                    .isDeleted(false)
                    .createdAt(LocalDateTime.now())
                    .termsAcceptedAt(LocalDateTime.now())
                    .termsVersion(termsVersion)
                    .build();

            userRepository.save(admin);
            log.info("[DataInitializer] Usuario creado: {} (ROLE_ADMIN)", adminUsername);
        } else {
            log.info("[DataInitializer] Usuario '{}' ya existe.", adminUsername);
        }
    }

    /**
     * Crea una categoría de prueba.
     * Se usa para asociarla a la subcategoría y productos de demostración.
     */
    private void seedCategories() {
        // Verificar si ya existe una categoría activa
        if (categoryRepository.findAll().isEmpty()) {
            Category category = Category.builder()
                    .name("Electrónica")
                    .description("Productos tecnológicos y electrónicos de última generación")
                    .createdBy("system")
                    .build();

            categoryService.saveCategory(category);
            log.info("[DataInitializer] Categoría creada: Electrónica");
        } else {
            log.info("[DataInitializer] Ya existen categorías en la base de datos.");
        }
    }

    /**
     * Crea una subcategoría de prueba asociada a la categoría "Electrónica".
     */
    private void seedSubcategories() {
        // Solo crear si no hay subcategorías en la base de datos
        if (subcategoryRepository.count() > 0) {
            log.info("[DataInitializer] Ya existen subcategorías en la base de datos.");
            return;
        }

        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            log.warn("[DataInitializer] No hay categorías — omitiendo creación de subcategorías.");
            return;
        }

        // Usar la primera categoría disponible (Electrónica si fue creada por
        // seedCategories)
        Category category = categories.get(0);

        Subcategory subcategory = Subcategory.builder()
                .name("Portátiles")
                .description("Ordenadores portátiles y laptops")
                .category(category)
                .createdBy("system")
                .build();

        subcategoryService.createSubcategory(subcategory);
        log.info("[DataInitializer] Subcategoría creada: Portátiles (categoría: {})", category.getName());
    }

    /**
     * Crea 2 productos de demostración asociados a la subcategoría "Portátiles".
     * Los productos se crean con stock inicial de 10 unidades.
     */
    private void seedDemoProducts() {
        // Verificar si ya hay productos
        if (!productService.getAllProducts().isEmpty()) {
            log.info("[DataInitializer] Ya existen productos en la base de datos.");
            return;
        }

        List<Subcategory> subcategories = subcategoryRepository.findAll();
        if (subcategories.isEmpty()) {
            log.warn("[DataInitializer] No hay subcategorías — omitiendo creación de productos.");
            return;
        }

        Subcategory subcategory = subcategories.get(0);

        // Producto 1: Laptop HP 15
        Product product1 = Product.builder()
                .name("Laptop HP 15 Ryzen 5")
                .description("Portátil HP 15 con procesador AMD Ryzen 5, 8GB RAM, SSD 256GB. " +
                        "Pantalla Full HD de 15.6 pulgadas. Ideal para trabajo y estudios.")
                .price(new BigDecimal("599.99"))
                .discountPrice(new BigDecimal("549.99"))
                .taxRate(new BigDecimal("21.00"))
                .stock(10)
                .lowStockThreshold(5)
                .weight(1.8)
                .isDigital(false)
                .isFeatured(true)
                .subcategories(List.of(subcategory))
                .createdBy("system")
                .images(new ArrayList<>())
                .build();

        productService.createProduct(product1);
        log.info("[DataInitializer] Producto creado: Laptop HP 15 Ryzen 5");

        // Producto 2: MacBook Air M2
        Product product2 = Product.builder()
                .name("MacBook Air M2 256GB")
                .description("Apple MacBook Air con chip M2, 8GB de memoria unificada, SSD 256GB. " +
                        "Pantalla Liquid Retina de 13.6 pulgadas. Diseño ultradelgado y ligero.")
                .price(new BigDecimal("1299.99"))
                .discountPrice(new BigDecimal("1199.99"))
                .taxRate(new BigDecimal("21.00"))
                .stock(10)
                .lowStockThreshold(5)
                .weight(1.24)
                .isDigital(false)
                .isFeatured(true)
                .subcategories(List.of(subcategory))
                .createdBy("system")
                .images(new ArrayList<>())
                .build();

        productService.createProduct(product2);
        log.info("[DataInitializer] Producto creado: MacBook Air M2 256GB");
    }

    /**
     * Retorna la descripción asociada a cada rol del sistema.
     */
    private String getDescription(String roleName) {
        return switch (roleName) {
            case "ROLE_SUPER_ADMIN" -> "Super administrador con acceso total al sistema";
            case "ROLE_ADMIN" -> "Administrador con permisos generales";
            case "ROLE_ORDERS" -> "Gestión de pedidos";
            case "ROLE_CUSTOMERS_INVOICES" -> "Gestión de clientes y facturas";
            case "ROLE_SUPPORT" -> "Atención al cliente (solo lectura)";
            case "ROLE_STORE" -> "Gestión de productos, categorías y variantes";
            case "ROLE_USER" -> "Cliente registrado";
            default -> "";
        };
    }
}
