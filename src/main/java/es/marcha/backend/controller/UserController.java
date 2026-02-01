package es.marcha.backend.controller;

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
import es.marcha.backend.model.user.Role;
import es.marcha.backend.model.user.User;
import es.marcha.backend.services.user.RoleService;
import es.marcha.backend.services.user.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    // Attribs
    @Autowired
    private UserService uService;

    @Autowired
    private RoleService rService;

    /**
     * Obtiene todos los usuarios de la base de datos.
     *
     * @return {@link ResponseEntity} con la lista de {@link User} y código HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = uService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id El ID del usuario que se desea obtener.
     * @return {@link ResponseEntity} con el {@link User} correspondiente y código HTTP 200 OK. Si
     *         no existe, devuelve {@code null} en el cuerpo.
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        User user = uService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param user El {@link User} que se desea crear. Debe incluir un {@link Role} válido.
     * @return {@link ResponseEntity} con el {@link User} guardado y código HTTP 200 OK.
     */
    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        Role role = rService.getRoleById(user.getRole().getId());
        User userSaved = uService.saveUser(user);
        userSaved.setRole(role);
        return new ResponseEntity<>(userSaved, HttpStatus.OK);
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * @param updatedUser El {@link User} con los nuevos datos a actualizar. Debe incluir un ID
     *        válido.
     * @return {@link ResponseEntity} con el {@link User} actualizado y código HTTP 200 OK. Si el
     *         usuario no existe, devuelve {@code null} en el cuerpo.
     */
    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User updatedUser) {
        User user = uService.updateUser(updatedUser);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Marca un usuario como eliminado (soft delete) en la base de datos.
     *
     * @param id El ID del usuario a eliminar.
     * @return {@link ResponseEntity} con un mensaje de éxito o error y código HTTP 200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        String msg = uService.deleteUser(id);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }



}
