package es.marcha.backend.services.security;

import java.util.Optional;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.request.LoginRequestDTO;
import es.marcha.backend.dto.request.RegisterRequestDTO;
import es.marcha.backend.dto.response.AuthResponseDTO;
import es.marcha.backend.dto.response.LogoutResponseDTO;
import es.marcha.backend.dto.response.UserResponseDTO;
import es.marcha.backend.exception.UserException;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.model.user.User;
import es.marcha.backend.security.JwtUtil;
import es.marcha.backend.services.user.RoleService;
import es.marcha.backend.services.user.UserService;
import es.marcha.backend.utils.Validations;

@Service
public class AuthService {
    // Attribs
    @Autowired
    private UserService uService;
    @Autowired
    private RoleService rService;

    // Methods
    // !! REVISAR MAS ADELANTE, ESTO DEBE IR CON USERDETAILSSERVICE
    // public AuthResponseDTO login(LoginRequestDTO credentials) {
    // Authentication authentication =
    // authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
    // credentials.getUsernameOrEmail(), credentials.getPassword()));

    // UserDetails userDetails = (UserDetails) authentication.getPrincipal();
    // User user =
    // uService.getUserByUsernameOrEmail(credentials.getUsernameOrEmail());
    // String token = JwtUtil.generateToken(userDetails.getUsername());

    // return new AuthResponseDTO(user, token);
    // }

    /**
     * Realiza la autenticación de un usuario mediante username o email y
     * contraseña.
     * <p>
     * El método busca al usuario por el valor proporcionado en
     * {@code usernameOrEmail}, compara la
     * contraseña ingresada con la almacenada en la base de datos y, si es correcta,
     * genera un token
     * JWT que será usado para autenticación en futuras solicitudes.
     * </p>
     *
     * @param credentials DTO que contiene las credenciales del usuario
     *        (username/email y password)
     * @return AuthResponseDTO que incluye el usuario autenticado y el token JWT
     *         generado
     * @throws UserException si el usuario no existe o la contraseña es incorrecta
     */
    public AuthResponseDTO login(LoginRequestDTO credentials) {
        User user = uService.getUserByUsernameOrEmail(credentials.getUsernameOrEmail());
        user.setActive(true);

        if (!Validations.comparePasswords(credentials.getPassword(), user.getPassword())) {
            throw new UserException(UserException.FAILED_LOGIN);
        }

        String token = JwtUtil.generateToken(user.getUsername());
        return new AuthResponseDTO(uService.saveUser(user), token);
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * <p>
     * Este método realiza las siguientes operaciones:
     * <ol>
     * <li>Verifica si ya existe un usuario con el mismo username y lanza una
     * excepción si es
     * así.</li>
     * <li>Valida el formato del email proporcionado y lanza una excepción si no es
     * válido.</li>
     * <li>Asigna un rol predeterminado al usuario (por ID en este caso).</li>
     * <li>Crea un objeto User con los datos proporcionados y algunos valores por
     * defecto (activo,
     * no verificado, no baneado, etc.).</li>
     * <li>Guarda el usuario en la base de datos mediante
     * {@code uService.saveUser}.</li>
     * <li>Genera un token JWT para el usuario recién creado.</li>
     * </ol>
     * </p>
     *
     * @param userData DTO que contiene los datos necesarios para registrar un
     *        usuario (nombre,
     *        apellido, username, email, contraseña, teléfono)
     * @return AuthResponseDTO que incluye el usuario registrado y el token JWT
     *         generado
     * @throws UserException si ya existe un usuario con el mismo username o si el
     *         email no es
     *         válido
     */
    public AuthResponseDTO register(RegisterRequestDTO userData) {
        Optional<UserResponseDTO> existUser = uService.getUserByUsername(userData.getUsername());
        Role role = rService.getRoleById(2);
        boolean isValidEmail = Validations.validateEmail(userData.getEmail());
        if (existUser.isPresent()) {
            throw new UserException(UserException.FAILED_CREATE_USER);
        }
        if (!isValidEmail) {
            throw new UserException(UserException.FAILED_REGISTER);
        }

        User user = User.builder()
                .name(userData.getName())
                .surname(userData.getSurname())
                .username(userData.getUsername())
                .email(userData.getEmail())
                .password(userData.getPassword())
                .phone(userData.getPhone())
                .role(role)
                .isActive(true)
                .isVerified(false).isBanned(false)
                .isDeleted(false)
                .profileImageUrl("")
                .lastLogin(null)
                .createdAt(LocalDateTime().now())
                .updatedAt(null)
                .deletedAt(null)
                .build();

        UserResponseDTO savedUser = uService.saveUser(user);
        String token = JwtUtil.generateToken(savedUser.getUsername());
        return new AuthResponseDTO(savedUser, token);
    }

    public LogoutResponseDTO logout(long userId) {
        User user = uService.getUserByIdForHandler(userId);
        if (!user.isActive()) {
            throw new UserException(UserException.USER_LOGGEDOUT);
        }
        user.setActive(false);
        user.setLastLogin(LocalDateTime().now());
        uService.saveUserForHandler(user);
        return LogoutResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .isActive(user.isActive())
                .lastLogin(user.getLastLogin())
                .build();
    }

}
