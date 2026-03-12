package es.marcha.backend.modules.company.presentation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import es.marcha.backend.core.config.ModuleProperties;
import es.marcha.backend.core.filestorage.application.service.MediaService;
import es.marcha.backend.core.security.jwt.JwtFilter;
import es.marcha.backend.core.security.jwt.VerifiedUserFilter;
import es.marcha.backend.modules.company.application.dto.response.CompanyConfigDTO;
import es.marcha.backend.modules.company.application.service.CompanyService;

/**
 * Tests de capa web para CompanyController.
 *
 * Nota: CompanyController mezcla inyección de dependencias:
 * - {@code @RequiredArgsConstructor} → {@code MediaService mService} (final)
 * - {@code @Autowired} → {@code CompanyService companyService}
 *
 * Verifica:
 * - GET /company → 200 OK con configuración actual
 * - PUT /company → 200 OK con configuración actualizada
 */
@WebMvcTest(value = CompanyController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class
}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = VerifiedUserFilter.class)
})
@DisplayName("CompanyController — configuración de empresa")
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompanyService companyService;

    @MockBean
    private MediaService mService;

    @MockBean
    private ModuleProperties moduleProperties;

    @BeforeEach
    void setUp() {
        when(moduleProperties.isEnabled(anyString())).thenReturn(true);
    }

    // =========================================================================
    // GET /company
    // =========================================================================

    @Nested
    @DisplayName("GET /company")
    class GetCompanyConfigTests {

        @Test
        @DisplayName("devuelve 200 con configuración actual")
        void getCompanyConfig_devuelve200() throws Exception {
            CompanyConfigDTO config = buildConfig("Mi Tienda");
            when(companyService.getConfig()).thenReturn(config);

            mockMvc.perform(get("/company"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("sin registro en BD → 200 con valores de properties")
        void getCompanyConfig_sinRegistroBD_devuelve200ConFallback() throws Exception {
            CompanyConfigDTO fallbackConfig = buildConfig("Tienda por defecto");
            when(companyService.getConfig()).thenReturn(fallbackConfig);

            mockMvc.perform(get("/company"))
                    .andExpect(status().isOk());

            verify(companyService).getConfig();
        }
    }

    // =========================================================================
    // PUT /company
    // =========================================================================

    @Nested
    @DisplayName("PUT /company")
    class UpdateCompanyConfigTests {

        @Test
        @DisplayName("actualiza configuración → 200 OK")
        void updateCompanyConfig_devuelve200() throws Exception {
            CompanyConfigDTO updated = buildConfig("Nueva Tienda");
            when(companyService.updateConfig(any(CompanyConfigDTO.class))).thenReturn(updated);

            String body = """
                    {
                        "name": "Nueva Tienda",
                        "email": "info@tienda.com",
                        "phone": "+34 600 000 000"
                    }
                    """;

            mockMvc.perform(put("/company")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(user("admin")))
                    .andExpect(status().isOk());
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private CompanyConfigDTO buildConfig(String name) {
        CompanyConfigDTO dto = new CompanyConfigDTO();
        dto.setName(name);
        return dto;
    }
}
