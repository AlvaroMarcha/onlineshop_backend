package es.marcha.backend.services.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.request.user.UpdateUserRequestDTO;
import es.marcha.backend.dto.response.user.AdminUserResponseDTO;
import es.marcha.backend.dto.response.user.BannedUserResponseDTO;
import es.marcha.backend.dto.response.user.TermsResponseDTO;
import es.marcha.backend.dto.response.user.UserResponseDTO;
import es.marcha.backend.exception.UserException;
import es.marcha.backend.mapper.user.UserMapper;
import es.marcha.backend.model.enums.RoleName;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.user.UserRepository;
import es.marcha.backend.services.media.MediaService;
import es.marcha.backend.services.security.TokenInvalidationService;
import es.marcha.backend.utils.Validations;
import jakarta.transaction.Transactional;

@Service
public class UserService {
    // Attribs
    @Autowired
    private UserRepository uRepository;
    @Autowired
    private RoleService rService;
    @Autowired
    private MediaService mService;
    @Autowired
    private TokenInvalidationService tokenInvalidationService;

    public static final String USER_DELETED = "USER WAS DELETED";

    // Methods
    /**
     * Obtiene un usuario activo y no baneado por su ID, mapeado a DTO.
     *
     * @param id El ID del usuario a buscar.
     * @return {@link UserResponseDTO} con los datos del usuario.
     * @throws UserException si el usuario no existe, está eliminado o baneado.
     */
    public UserResponseDTO getUserById(long id) {
        return uRepository.findById(id)
                .filter(user -> !user.isDeleted() && !user.isBanned())
                .map(UserMapper::toUserDTO)
                .orElseThrow(() -> new UserException());
    }

    /**
     * Obtiene un usuario activo y no baneado por su ID como entidad, para uso
     * interno de otros servicios.
     * A diferencia de {@link #getUserById}, devuelve la entidad completa sin mapear
     * a DTO.
     *
     * @param id El ID del usuario a buscar.
     * @return La entidad {@link User} correspondiente.
     * @throws UserException si el usuario no existe, está eliminado o baneado.
     */
    public User getUserByIdForHandler(long id) {
        return uRepository.findById(id)
                .filter(user -> !user.isDeleted() && !user.isBanned())
                .orElseThrow(() -> new UserException());
    }

    /**
     * Busca un usuario por su username y lo devuelve como {@link Optional} de DTO.
     * No lanza excepción si no existe, por lo que es seguro para comprobaciones de
     * existencia.
     *
     * @param username El nombre de usuario a buscar.
     * @return {@link Optional} con el {@link UserResponseDTO} si existe, o vacío si
     *         no.
     */
    public Optional<UserResponseDTO> getUserByUsername(String username) {
        return uRepository.findByUsername(username)
                .map(UserMapper::toUserDTO);
    }

    /**
     * Devuelve la versión y fecha de aceptación de los términos y condiciones del
     * usuario autenticado, identificado por su username extraído del JWT.
     *
     * @param username El username del usuario autenticado.
     * @return {@link TermsResponseDTO} con la versión y la fecha de aceptación.
     * @throws UserException si el usuario no existe.
     */
    public TermsResponseDTO getTermsByUsername(String username) {
        User user = uRepository.findByUsername(username)
                .orElseThrow(() -> new UserException());
        return TermsResponseDTO.builder()
                .termsVersion(user.getTermsVersion())
                .termsAcceptedAt(user.getTermsAcceptedAt())
                .build();
    }

    /**
     * Obtiene un usuario a partir de su username o email.
     * <p>
     * Este método primero determina si el parámetro proporcionado es un email
     * válido o un username.
     * Dependiendo del caso, busca al usuario en la base de datos usando
     * {@code uRepository.findByEmail} o {@code uRepository.findByUsername}. Además,
     * filtra los
     * usuarios que estén marcados como eliminados ({@code isDeleted}) o baneados
     * ({@code isBanned}), de modo que solo se devuelvan usuarios activos. Si no se
     * encuentra ningún
     * usuario válido, se lanza una excepción {@link UserException}.
     * </p>
     *
     * @param usernameOrEmail el username o email del usuario a buscar
     * @return el usuario encontrado y activo
     * @throws UserException si no existe un usuario con ese username/email, o si el
     *                       usuario está
     *                       eliminado o baneado
     */
    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        boolean isEmail = Validations.validateEmail(usernameOrEmail);
        if (isEmail) {
            return uRepository.findByEmail(usernameOrEmail)
                    .filter(user -> !user.isDeleted() && !user.isBanned())
                    .orElseThrow(() -> new UserException());
        } else {
            return uRepository.findByUsername(usernameOrEmail)
                    .filter(user -> !user.isDeleted() && !user.isBanned())
                    .orElseThrow(() -> new UserException());
        }
    }

    /**
     * Obtiene todos los usuarios activos y no baneados del sistema.
     *
     * @return Lista de {@link UserResponseDTO} con los usuarios activos.
     * @throws UserException si no hay ningún usuario en la base de datos.
     */
    public List<UserResponseDTO> getAllUsers() {
        List<User> users = uRepository.findAll();
        if (users.isEmpty()) {
            throw new UserException(UserException.FAILED_FETCH);
        }

        List<User> filteredUsers = users.stream()
                .filter(user -> !user.isDeleted() && !user.isBanned())
                .toList();
        List<UserResponseDTO> usersDTO = filteredUsers.stream()
                .map(UserMapper::toUserDTO)
                .toList();
        return usersDTO;
    }

    /**
     * Guarda o actualiza un usuario en la base de datos, resolviendo su rol
     * e inicializando los campos de estado por defecto.
     *
     * @param user El {@link User} a guardar. Debe incluir un {@link Role} con ID
     *             válido.
     * @return {@link UserResponseDTO} con los datos del usuario persistido.
     */
    public UserResponseDTO saveUser(User user) {
        Role role = rService.getRoleById(user.getRole().getId());

        user.setRole(role);
        user.setAddresses(new ArrayList<>());

        // Solo inicializar la URL si el usuario aún no tiene una asignada
        if (user.getProfileImageUrl() == null || user.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(mService.getDefaultProfileImageUrl());
        }

        user.setActive(true);
        user.setBanned(false);
        user.setDeleted(false);
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        return UserMapper.toUserDTO(uRepository.save(user));
    }

    /**
     * Guarda directamente un usuario en la base de datos sin inicializar campos por
     * defecto.
     * Destinado a uso interno de servicios que necesitan persistir cambios
     * parciales sobre la entidad.
     *
     * @param user El {@link User} a persistir.
     * @return La entidad {@link User} guardada.
     */
    public User saveUserForHandler(User user) {
        return uRepository.save(user);
    }

    /**
     * Mapea una entidad {@link User} a su DTO de respuesta.
     *
     * @param user La entidad a mapear.
     * @return {@link UserResponseDTO} con los datos del usuario.
     */
    public UserResponseDTO mapUserToDTO(User user) {
        return UserMapper.toUserDTO(user);
    }

    /**
     * Busca un usuario por su token de verificación de email.
     * Usado por el flujo de verificación para validar el token recibido.
     *
     * @param token el UUID de verificación enviado al correo del usuario
     * @return la entidad {@link User} asociada al token
     * @throws UserException si el token no existe en la base de datos
     */
    public User getUserByVerificationToken(String token) {
        return uRepository.findByVerificationToken(token)
                .orElseThrow(() -> new UserException(UserException.VERIFICATION_TOKEN_INVALID));
    }

    /**
     * Actualiza los datos del usuario autenticado. Solo se permiten cambios en
     * nombre, apellido, email y teléfono. Los campos {@code null} conservan su
     * valor actual. Si se cambia el email se verifica que no esté ya en uso.
     *
     * @param currentEmail email del usuario autenticado (extraído del JWT)
     * @param dto          DTO con los campos a actualizar
     * @return {@link UserResponseDTO} con los datos actualizados
     * @throws UserException si el usuario no existe o el email nuevo ya está en uso
     */
    @Transactional
    public UserResponseDTO updateUser(String currentUsername, UpdateUserRequestDTO dto) {
        User user = uRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserException());

        if (dto.getEmail() != null && !dto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (uRepository.existsByEmail(dto.getEmail())) {
                throw new UserException(UserException.EMAIL_ALREADY_IN_USE);
            }
            user.setEmail(dto.getEmail());
        }
        if (dto.getName() != null)
            user.setName(dto.getName());
        if (dto.getSurname() != null)
            user.setSurname(dto.getSurname());
        if (dto.getPhone() != null)
            user.setPhone(dto.getPhone());

        user.setUpdatedAt(LocalDateTime.now());
        return UserMapper.toUserDTO(uRepository.save(user));
    }

    /**
     * Marca un usuario como eliminado en la base de datos, sin borrarlo
     * físicamente. Se establece
     * la fecha de eliminación y se activa el flag {@code isDeleted}.
     *
     * @param id El ID del usuario que se desea eliminar.
     * @return Un mensaje {@link String} indicando si el usuario fue eliminado
     *         correctamente o si no
     *         se encontró.
     */
    @Transactional
    public String deleteUser(long id) {
        User deletedUser = uRepository.findById(id)
                .orElseThrow(() -> new UserException());
        deletedUser.setDeletedAt(LocalDateTime.now());
        deletedUser.setDeleted(true);
        deletedUser.setActive(false);
        uRepository.save(deletedUser);

        return USER_DELETED;
    }

    /**
     * Banea a un usuario identificado por su ID.
     *
     * <p>
     * Este método localiza al usuario en el sistema y genera una respuesta que
     * representa su estado tras ser baneado. El baneo implica:
     * <ul>
     * <li>Marcar al usuario como baneado.</li>
     * <li>Desactivar su cuenta para impedir cualquier interacción.</li>
     * </ul>
     * </p>
     *
     * <p>
     * Si el usuario no existe, se lanza una excepción {@link UserException}.
     * </p>
     *
     * @param id identificador único del usuario a banear
     * @return {@link BannedUserResponseDTO} con la información básica del usuario
     *         y su nuevo estado (baneado e inactivo)
     * @throws UserException si el usuario no existe
     */
    public BannedUserResponseDTO banUserById(long id) {
        User user = uRepository.findById(id).orElseThrow(() -> new UserException());
        user.setBanned(true);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        BannedUserResponseDTO bannedUser = BannedUserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isBanned(user.isBanned())
                .isActive(user.isActive())
                .build();
        uRepository.save(user);
        // Invalidar el JWT del usuario baneado de forma inmediata
        tokenInvalidationService.invalidate(user.getUsername());
        return bannedUser;
    }

    // ─── Métodos de administración ────────────────────────────────────────────

    /**
     * Lista todos los usuarios del sistema con paginación para el panel de
     * administración. A diferencia de {@link #getAllUsers()}, no filtra usuarios
     * baneados ni eliminados.
     *
     * @param pageable parámetros de paginación y ordenación
     * @return {@link Page} de {@link AdminUserResponseDTO} con todos los usuarios
     */
    @Transactional
    public Page<AdminUserResponseDTO> getAllUsersForAdmin(Pageable pageable) {
        return uRepository.findAll(pageable).map(UserMapper::toAdminUserDTO);
    }

    /**
     * Obtiene el perfil completo de cualquier usuario por su ID para el panel de
     * administración, incluyendo usuarios baneados o eliminados.
     *
     * @param id El ID del usuario a buscar.
     * @return {@link AdminUserResponseDTO} con todos los datos del usuario.
     * @throws UserException si el usuario no existe.
     */
    @Transactional
    public AdminUserResponseDTO getUserByIdForAdmin(long id) {
        return uRepository.findById(id)
                .map(UserMapper::toAdminUserDTO)
                .orElseThrow(() -> new UserException());
    }

    /**
     * Desbanea a un usuario, restableciendo su acceso al sistema.
     *
     * @param id El ID del usuario a desbanear.
     * @return {@link BannedUserResponseDTO} con el nuevo estado del usuario.
     * @throws UserException si el usuario no existe.
     */
    @Transactional
    public BannedUserResponseDTO unbanUser(long id) {
        User user = uRepository.findById(id).orElseThrow(() -> new UserException());
        user.setBanned(false);
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        uRepository.save(user);
        return BannedUserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .isBanned(user.isBanned())
                .isActive(user.isActive())
                .build();
    }

    /**
     * Cambia el rol de un usuario e invalida su JWT de forma inmediata.
     * El usuario afectado recibirá un 401 en su próxima petición y deberá
     * volver a autenticarse para obtener un token con el nuevo rol.
     *
     * @param id       El ID del usuario cuyo rol se va a cambiar.
     * @param roleName El nombre del nuevo rol (p. ej. {@code ROLE_ADMIN}).
     * @return {@link AdminUserResponseDTO} con el nuevo estado del usuario.
     * @throws UserException si el usuario no existe.
     */
    @Transactional
    public AdminUserResponseDTO changeUserRole(long id, String roleName) {
        User user = uRepository.findById(id).orElseThrow(() -> new UserException());
        Role newRole = rService.getRoleByName(roleName);
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        User saved = uRepository.save(user);
        // Invalidar JWT actual para que el cambio de rol tenga efecto inmediato
        tokenInvalidationService.invalidate(user.getUsername());
        return UserMapper.toAdminUserDTO(saved);
    }

    /**
     * Obtiene el rol actual de un usuario.
     *
     * @param id El ID del usuario.
     * @return La entidad {@link Role} asignada actualmente al usuario.
     * @throws UserException si el usuario no existe.
     */
    @Transactional
    public Role getUserRole(long id) {
        return uRepository.findById(id)
                .map(User::getRole)
                .orElseThrow(() -> new UserException());
    }

    /**
     * Asigna un nuevo rol a un usuario por el ID del rol e invalida su JWT.
     *
     * @param userId El ID del usuario.
     * @param roleId El ID del nuevo rol.
     * @return {@link AdminUserResponseDTO} con el estado actualizado del usuario.
     * @throws UserException            si el usuario no existe.
     * @throws RolePermissionsException si el rol no existe.
     */
    @Transactional
    public AdminUserResponseDTO assignRoleById(long userId, long roleId) {
        User user = uRepository.findById(userId).orElseThrow(() -> new UserException());
        Role newRole = rService.getRoleById(roleId);
        user.setRole(newRole);
        user.setUpdatedAt(LocalDateTime.now());
        User saved = uRepository.save(user);
        tokenInvalidationService.invalidate(saved.getUsername());
        return UserMapper.toAdminUserDTO(saved);
    }

    /**
     * Revoca el rol actual de un usuario y le asigna el rol por defecto
     * ({@code ROLE_USER}). Invalida el JWT del usuario de forma inmediata.
     *
     * @param userId El ID del usuario.
     * @return {@link AdminUserResponseDTO} con el estado actualizado del usuario.
     * @throws UserException si el usuario no existe.
     */
    @Transactional
    public AdminUserResponseDTO revokeUserRole(long userId) {
        User user = uRepository.findById(userId).orElseThrow(() -> new UserException());
        Role defaultRole = rService.getRoleByName(RoleName.ROLE_USER.name());
        user.setRole(defaultRole);
        user.setUpdatedAt(LocalDateTime.now());
        User saved = uRepository.save(user);
        tokenInvalidationService.invalidate(saved.getUsername());
        return UserMapper.toAdminUserDTO(saved);
    }

    /**
     * Elimina físicamente un usuario de la base de datos (solo SUPER_ADMIN).
     * Opera como hard delete: el registro desaparece de la BD.
     * Para eliminaciones con cumplimiento RGPD usar
     * {@code UserDeletionService.anonymizeAndDelete}.
     *
     * @param id El ID del usuario a eliminar.
     * @return Mensaje de confirmación.
     * @throws UserException si el usuario no existe.
     */
    @Transactional
    public String hardDeleteUser(long id) {
        User user = uRepository.findById(id).orElseThrow(() -> new UserException());
        // Invalidar JWT antes de borrar para evitar peticiones en tránsito
        tokenInvalidationService.invalidate(user.getUsername());
        uRepository.delete(user);
        return USER_DELETED;
    }

}
