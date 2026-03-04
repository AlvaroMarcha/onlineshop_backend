package es.marcha.backend.core.user.presentation.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.marcha.backend.core.user.domain.model.Role;
import es.marcha.backend.core.user.application.service.RoleService;

@RestController
@RequestMapping("/roles")
public class RoleController {

    @Autowired
    private RoleService rService;

    /**
     * Obtiene todos los roles existentes en el sistema.
     *
     * @return {@link ResponseEntity} con la lista de {@link Role} y código HTTP 200
     *         OK.
     */
    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        List<Role> roles = rService.getAllRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    /**
     * Obtiene un rol por su ID.
     *
     * @param id El ID del rol a obtener.
     * @return {@link ResponseEntity} con el {@link Role} correspondiente y código
     *         HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable long id) {
        Role role = rService.getRoleById(id);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    /**
     * Crea y persiste un nuevo rol en el sistema.
     *
     * @param role El {@link Role} a guardar.
     * @return {@link ResponseEntity} con el {@link Role} creado y código HTTP 200
     *         OK.
     */
    @PostMapping
    public ResponseEntity<Role> saveRole(@RequestBody Role role) {
        Role newRole = rService.saveRole(role);
        return new ResponseEntity<>(newRole, HttpStatus.OK);
    }

    /**
     * Actualiza un rol existente con los nuevos datos proporcionados.
     *
     * @param role El {@link Role} con los datos actualizados. Debe incluir un ID
     *             válido.
     * @return {@link ResponseEntity} con el {@link Role} actualizado y código HTTP
     *         200 OK.
     */
    @PutMapping
    public ResponseEntity<Role> updateRole(@RequestBody Role role) {
        Role existRole = rService.updateRole(role);
        return new ResponseEntity<>(existRole, HttpStatus.OK);
    }

    /**
     * Elimina un rol por su ID. No se puede eliminar el rol {@code ADMIN}.
     *
     * @param id El ID del rol a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP
     *         200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRole(@PathVariable long id) {
        return new ResponseEntity<>(rService.deleteRole(id), HttpStatus.OK);
    }
}
