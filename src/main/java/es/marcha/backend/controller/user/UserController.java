package es.marcha.backend.controller.user;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.marcha.backend.dto.response.user.BannedUserResponseDTO;
import es.marcha.backend.dto.response.user.UserResponseDTO;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.model.user.User;
import es.marcha.backend.services.media.MediaService;
import es.marcha.backend.services.user.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    // Attribs
    @Autowired
    private UserService uService;

    @Autowired
    private MediaService mService;

    /**
     * Obtiene todos los usuarios de la base de datos.
     *
     * @return {@link ResponseEntity} con la lista de {@link User} y código HTTP 200
     *         OK.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        List<UserResponseDTO> users = uService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Obtiene un usuario por su ID.
     *
     * @param id El ID del usuario que se desea obtener.
     * @return {@link ResponseEntity} con el {@link User} correspondiente y código
     *         HTTP 200 OK. Si
     *         no existe, devuelve {@code null} en el cuerpo.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable long id) {
        UserResponseDTO user = uService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param user El {@link User} que se desea crear. Debe incluir un {@link Role}
     *             válido.
     * @return {@link ResponseEntity} con el {@link User} guardado y código HTTP 200
     *         OK.
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> saveUser(@RequestBody User user) {
        UserResponseDTO userSaved = uService.saveUser(user);
        return new ResponseEntity<>(userSaved, HttpStatus.OK);
    }

    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * @param updatedUser El {@link User} con los nuevos datos a actualizar. Debe
     *                    incluir un ID
     *                    válido.
     * @return {@link ResponseEntity} con el {@link User} actualizado y código HTTP
     *         200 OK. Si el
     *         usuario no existe, devuelve {@code null} en el cuerpo.
     */
    @PutMapping
    public ResponseEntity<UserResponseDTO> updateUser(@RequestBody User updatedUser) {
        UserResponseDTO user = uService.updateUser(updatedUser);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Marca un usuario como eliminado (soft delete) en la base de datos.
     *
     * @param id El ID del usuario a eliminar.
     * @return {@link ResponseEntity} con un mensaje de éxito o error y código HTTP
     *         200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        String msg = uService.deleteUser(id);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }

    /**
     * Banea a un usuario por su ID, desactivando su cuenta e impidiendo futuros
     * accesos.
     *
     * @param id El ID del usuario a banear.
     * @return {@link ResponseEntity} con el {@link BannedUserResponseDTO} que
     *         refleja
     *         el nuevo estado del usuario, con código HTTP 200 OK.
     */
    @PostMapping("/ban/{id}")
    public ResponseEntity<BannedUserResponseDTO> banUser(@PathVariable long id) {
        BannedUserResponseDTO bannedUser = uService.banUserById(id);
        return new ResponseEntity<>(bannedUser, HttpStatus.OK);
    }

    /**
     * Sube una nueva imagen de perfil para el usuario indicado y actualiza
     * la URL almacenada en su registro.
     *
     * @param id   ID del usuario cuya foto de perfil se desea actualizar.
     * @param file archivo de imagen recibido en el campo {@code file} del
     *             formulario
     *             (multipart/form-data). Solo se aceptan JPEG y PNG.
     * @return {@link ResponseEntity} con la URL pública de la nueva imagen y código
     *         HTTP 200 OK.
     */
    @PostMapping("/upload/{id}")
    public ResponseEntity<String> uploadProfileImage(@PathVariable long id, @RequestParam("file") MultipartFile file) {
        String imageUrl = mService.newPicProfile(file, id);
        return new ResponseEntity<>(imageUrl, HttpStatus.OK);
    }
}
