package es.marcha.backend.core.presentation.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.core.config.ModuleProperties;

/**
 * Controlador de administración para activar/desactivar módulos en caliente.
 *
 * <p>
 * No está protegido por seguridad en esta implementación de ejemplo, pero en
 * un escenario real debería requerir autenticación/roles de administrador.
 */
@RestController
@RequestMapping("/admin/modules")
public class ModuleAdminController {

    private final ModuleProperties moduleProps;

    @Autowired
    public ModuleAdminController(ModuleProperties moduleProps) {
        this.moduleProps = moduleProps;
    }

    /**
     * Lista el estado actual de todos los módulos.
     */
    @GetMapping
    public ResponseEntity<Map<String, Boolean>> list() {
        return new ResponseEntity<>(moduleProps.asMap(), HttpStatus.OK);
    }

    /**
     * Cambia el estado de un módulo concreto.
     *
     * <p>
     * Ejemplo: <code>POST /admin/modules/cart/true</code> habilita "cart".
     */
    @PostMapping("/{module}/{enabled}")
    public ResponseEntity<Map<String, Boolean>> set(@PathVariable String module, @PathVariable boolean enabled) {
        moduleProps.setEnabled(module, enabled);
        return new ResponseEntity<>(moduleProps.asMap(), HttpStatus.OK);
    }
}
