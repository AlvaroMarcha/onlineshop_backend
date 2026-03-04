package es.marcha.backend.core.user.presentation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import es.marcha.backend.core.user.application.dto.request.ChangeRoleRequestDTO;
import es.marcha.backend.core.user.application.dto.request.UpdateUserRequestDTO;
import es.marcha.backend.core.user.application.dto.response.AdminUserResponseDTO;
import es.marcha.backend.core.user.application.dto.response.BannedUserResponseDTO;
import es.marcha.backend.core.user.application.dto.response.DataExportResponseDTO;
import es.marcha.backend.core.user.application.dto.response.TermsResponseDTO;
import es.marcha.backend.core.user.application.dto.response.UserResponseDTO;
import es.marcha.backend.core.user.domain.model.User;
import es.marcha.backend.core.filestorage.application.service.MediaService;
import es.marcha.backend.core.auth.application.service.RateLimitService;
import es.marcha.backend.core.auth.application.service.RateLimitService.EndpointType;
import es.marcha.backend.core.user.application.service.DataExportService;
import es.marcha.backend.core.user.application.service.UserDeletionService;
import es.marcha.backend.core.user.application.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    // Attribs
    @Autowired
    private UserService uService;

    @Autowired
    private MediaService mService;

    @Autowired
    private UserDeletionService userDeletionService;

    @Autowired
    private DataExportService dataExportService;

    @Autowired
    private RateLimitService rateLimitService;

    /**
     * Obtiene todos los usuarios de la base de datos con paginación (panel admin).
     * Incluye usuarios baneados y eliminados. Solo accesible para ADMIN /
     * SUPER_ADMIN.
     *
     * @param page número de página (0-based, por defecto 0).
     * @param size tamaño de la página (por defecto 20).
     * @return {@link ResponseEntity} con {@link Page} de
     *         {@link AdminUserResponseDTO}
     *         y código HTTP 200 OK.
     */
    @GetMapping
    public ResponseEntity<Page<AdminUserResponseDTO>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AdminUserResponseDTO> users = uService.getAllUsersForAdmin(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * Devuelve el perfil del usuario autenticado.
     *
     * @return {@link ResponseEntity} con el {@link UserResponseDTO} del usuario
     *         autenticado
     *         y código HTTP 200 OK.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserResponseDTO user = uService.getUserByUsername(auth.getName())
                .orElseThrow(() -> new es.marcha.backend.core.error.exception.UserException());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Devuelve la versión y fecha de aceptación de los términos y condiciones
     * del usuario autenticado.
     *
     * @return {@link ResponseEntity} con {@link TermsResponseDTO} y código HTTP 200
     *         OK.
     */
    @GetMapping("/me/terms")
    public ResponseEntity<TermsResponseDTO> getMyTerms() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        TermsResponseDTO terms = uService.getTermsByUsername(auth.getName());
        return new ResponseEntity<>(terms, HttpStatus.OK);
    }

    /**
     * Exporta todos los datos personales del usuario autenticado (Art. 20 RGPD).
     * <p>
     * El rate limit es de 1 exportación por día por usuario.
     *
     * @param request la petición HTTP entrante, usada para derivar el identificador
     *                de rate limiting.
     * @return {@link ResponseEntity} con {@link DataExportResponseDTO} y código
     *         HTTP 200 OK.
     */
    @GetMapping("/me/data-export")
    public ResponseEntity<DataExportResponseDTO> exportMyData(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        rateLimitService.checkRateLimit(auth.getName(), EndpointType.DATA_EXPORT);
        DataExportResponseDTO export = dataExportService.export(auth.getName());
        return new ResponseEntity<>(export, HttpStatus.OK);
    }

    /**
     * Anonimiza y elimina la cuenta del usuario autenticado (Art. 17 RGPD).
     * <p>
     * Envía un email de notificación, anonimiza los datos de carácter personal
     * y desactiva la cuenta. Los pedidos e historial de compras se conservan
     * de forma anonimizada durante 10 años por obligación legal.
     *
     * @return {@link ResponseEntity} vacío con código HTTP 200 OK.
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        userDeletionService.anonymizeAndDelete(auth.getName());
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene el perfil completo de un usuario por su ID (panel admin).
     * No filtra usuarios baneados ni eliminados.
     *
     * @param id El ID del usuario que se desea obtener.
     * @return {@link ResponseEntity} con {@link AdminUserResponseDTO} y código
     *         HTTP 200 OK.
     */
    @GetMapping("/{id}")
    public ResponseEntity<AdminUserResponseDTO> getUserById(@PathVariable long id) {
        AdminUserResponseDTO user = uService.getUserByIdForAdmin(id);
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
     * Actualiza los datos del usuario autenticado.
     * Solo se permiten cambios en nombre, apellido, email y teléfono.
     *
     * @param dto datos a actualizar (todos opcionales)
     * @return {@link ResponseEntity} con {@link UserResponseDTO} actualizado y
     *         código HTTP 200 OK.
     */
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateUser(@Valid @RequestBody UpdateUserRequestDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserResponseDTO user = uService.updateUser(auth.getName(), dto);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    /**
     * Elimina físicamente un usuario de la base de datos (hard delete).
     * Solo accesible para SUPER_ADMIN. Invalida el JWT del usuario antes de
     * eliminarlo.
     *
     * @param id El ID del usuario a eliminar.
     * @return {@link ResponseEntity} con un mensaje de confirmación y código HTTP
     *         200 OK.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        String msg = uService.hardDeleteUser(id);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }

    /**
     * Banea a un usuario por su ID, desactivando su cuenta e impidiendo futuros
     * accesos. Invalida el JWT del usuario de forma inmediata.
     *
     * @param id El ID del usuario a banear.
     * @return {@link ResponseEntity} con el {@link BannedUserResponseDTO} que
     *         refleja el nuevo estado del usuario, con código HTTP 200 OK.
     */
    @PutMapping("/{id}/ban")
    public ResponseEntity<BannedUserResponseDTO> banUser(@PathVariable long id) {
        BannedUserResponseDTO bannedUser = uService.banUserById(id);
        return new ResponseEntity<>(bannedUser, HttpStatus.OK);
    }

    /**
     * Desbanea a un usuario por su ID, restaurando su acceso al sistema.
     *
     * @param id El ID del usuario a desbanear.
     * @return {@link ResponseEntity} con el {@link BannedUserResponseDTO} que
     *         refleja el nuevo estado del usuario, con código HTTP 200 OK.
     */
    @PutMapping("/{id}/unban")
    public ResponseEntity<BannedUserResponseDTO> unbanUser(@PathVariable long id) {
        BannedUserResponseDTO result = uService.unbanUser(id);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Cambia el rol de un usuario e invalida su JWT de forma inmediata.
     * Solo accesible para SUPER_ADMIN.
     *
     * @param id  El ID del usuario cuyo rol se desea cambiar.
     * @param dto cuerpo de la petición con el nombre del nuevo rol.
     * @return {@link ResponseEntity} con {@link AdminUserResponseDTO} actualizado
     *         y código HTTP 200 OK.
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<AdminUserResponseDTO> changeUserRole(
            @PathVariable long id,
            @Valid @RequestBody ChangeRoleRequestDTO dto) {
        AdminUserResponseDTO updated = uService.changeUserRole(id, dto.getRoleName());
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Obtiene el rol actual del usuario indicado.
     *
     * @param id El ID del usuario.
     * @return {@link ResponseEntity} con la entidad
     *         {@link es.marcha.backend.model.user.Role}
     *         y código HTTP 200 OK.
     */
    @GetMapping("/{id}/role")
    public ResponseEntity<?> getUserRole(@PathVariable long id) {
        return new ResponseEntity<>(uService.getUserRole(id), HttpStatus.OK);
    }

    /**
     * Asigna un rol a un usuario por el ID del rol. Invalida el JWT del usuario
     * de forma inmediata. Solo accesible para SUPER_ADMIN.
     *
     * @param id     El ID del usuario.
     * @param roleId El ID del nuevo rol.
     * @return {@link ResponseEntity} con {@link AdminUserResponseDTO} actualizado
     *         y código HTTP 200 OK.
     */
    @PutMapping("/{id}/role/{roleId}")
    public ResponseEntity<AdminUserResponseDTO> assignRoleById(
            @PathVariable long id,
            @PathVariable long roleId) {
        AdminUserResponseDTO updated = uService.assignRoleById(id, roleId);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    /**
     * Revoca el rol actual del usuario y le asigna {@code ROLE_USER} por defecto.
     * Invalida el JWT del usuario de forma inmediata. Solo accesible para
     * SUPER_ADMIN.
     *
     * @param id El ID del usuario.
     * @return {@link ResponseEntity} con {@link AdminUserResponseDTO} actualizado
     *         y código HTTP 200 OK.
     */
    @DeleteMapping("/{id}/role")
    public ResponseEntity<AdminUserResponseDTO> revokeUserRole(@PathVariable long id) {
        AdminUserResponseDTO updated = uService.revokeUserRole(id);
        return new ResponseEntity<>(updated, HttpStatus.OK);
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
