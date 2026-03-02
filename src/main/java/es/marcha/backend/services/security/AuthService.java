package es.marcha.backend.services.security;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.security.crypto.password.PasswordEncoder;

import es.marcha.backend.dto.request.security.LoginRequestDTO;
import es.marcha.backend.dto.request.security.RegisterRequestDTO;
import es.marcha.backend.dto.response.security.AuthResponseDTO;
import es.marcha.backend.dto.response.user.LogoutResponseDTO;
import es.marcha.backend.dto.response.user.UserResponseDTO;
import es.marcha.backend.exception.UserException;
import es.marcha.backend.model.enums.RoleName;
import es.marcha.backend.model.security.RefreshToken;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.model.user.User;
import es.marcha.backend.security.JwtUtil;
import es.marcha.backend.services.mail.UserEmailNotificationService;
import es.marcha.backend.services.user.RoleService;
import es.marcha.backend.services.user.UserService;
import es.marcha.backend.utils.Validations;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AuthService {
    // Attribs
    @Autowired
    private UserService uService;
    @Autowired
    private RoleService rService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private RefreshTokenService refreshTokenService;
    @Autowired
    private UserEmailNotificationService emailService;

    @Value("${app.terms.current-version}")
    private String currentTermsVersion;

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
     *                    (username/email y password)
     * @return AuthResponseDTO que incluye el usuario autenticado y el token JWT
     *         generado
     * @throws UserException si el usuario no existe o la contraseña es incorrecta
     */
    public AuthResponseDTO login(LoginRequestDTO credentials) {
        User user = uService.getUserByUsernameOrEmail(credentials.getUsernameOrEmail());
        user.setActive(true);
        user.setSessionCount(user.getSessionCount() + 1);

        if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
            throw new UserException(UserException.FAILED_LOGIN);
        }

        User savedUser = uService.saveUserForHandler(user);
        UserResponseDTO savedUserDTO = uService.mapUserToDTO(savedUser);
        String accessToken = JwtUtil.generateToken(user.getUsername(), user.getRole().getName());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);
        return AuthResponseDTO.builder()
                .user(savedUserDTO)
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
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
     *                 usuario (nombre,
     *                 apellido, username, email, contraseña, teléfono)
     * @return AuthResponseDTO que incluye el usuario registrado y el token JWT
     *         generado
     * @throws UserException si ya existe un usuario con el mismo username o si el
     *                       email no es
     *                       válido
     */
    public AuthResponseDTO register(RegisterRequestDTO userData) {
        Optional<UserResponseDTO> existUser = uService.getUserByUsername(userData.getUsername());
        Role role = rService.getRoleByName(RoleName.ROLE_USER.name());
        boolean isValidEmail = Validations.validateEmail(userData.getEmail());
        if (existUser.isPresent()) {
            throw new UserException(UserException.FAILED_CREATE_USER);
        }
        if (!isValidEmail) {
            throw new UserException(UserException.FAILED_REGISTER);
        }
        if (!userData.isTermsAccepted()) {
            throw new UserException(UserException.TERMS_NOT_ACCEPTED);
        }

        User user = User.builder()
                .name(userData.getName())
                .surname(userData.getSurname())
                .username(userData.getUsername())
                .email(userData.getEmail())
                .password(passwordEncoder.encode(userData.getPassword()))
                .phone(userData.getPhone())
                .role(role)
                .isActive(true)
                .isVerified(false).isBanned(false)
                .isDeleted(false)
                .lastLogin(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .deletedAt(null)
                .sessionCount(0)
                .termsAcceptedAt(LocalDateTime.now())
                .termsVersion(currentTermsVersion)
                .build();

        UserResponseDTO savedUser = uService.saveUser(user);
        String accessToken = JwtUtil.generateToken(savedUser.getUsername(), savedUser.getRoleName());
        User persistedUser = uService.getUserByIdForHandler(savedUser.getId());

        // Generar token de verificación de email con expiración de 24h
        String verificationToken = UUID.randomUUID().toString();
        persistedUser.setVerificationToken(verificationToken);
        persistedUser.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        uService.saveUserForHandler(persistedUser);

        // Enviar email de verificación de forma asíncrona
        emailService.sendVerificationEmail(persistedUser.getName(), persistedUser.getEmail(), verificationToken);

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(persistedUser);
        return AuthResponseDTO.builder()
                .user(savedUser)
                .token(accessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    /**
     * Cierra la sesión del usuario indicado marcándolo como inactivo y registrando
     * el momento del último acceso.
     * <p>
     * Si el usuario ya está inactivo (sesión ya cerrada), lanza una excepción.
     * </p>
     *
     * @param userId ID del usuario cuya sesión se desea cerrar.
     * @return {@link LogoutResponseDTO} con el estado actualizado del usuario.
     * @throws UserException si el usuario ya tiene la sesión cerrada
     *                       ({@link UserException#USER_LOGGEDOUT}).
     */
    public LogoutResponseDTO logout(long userId) {
        User user = uService.getUserByIdForHandler(userId);
        if (!user.isActive()) {
            throw new UserException(UserException.USER_LOGGEDOUT);
        }
        user.setActive(false);
        user.setLastLogin(LocalDateTime.now());
        uService.saveUserForHandler(user);
        // Revocar todos los refresh tokens activos del usuario
        refreshTokenService.revokeAllTokensByUser(user);
        return LogoutResponseDTO.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .isActive(user.isActive())
                .lastLogin(user.getLastLogin())
                .build();
    }

    /**
     * Renueva el access token a partir de un refresh token válido.
     * <p>
     * Valida que el refresh token exista, no haya expirado y no haya sido revocado.
     * Si es válido, genera un nuevo access token JWT para el usuario asociado.
     * El refresh token sigue siendo el mismo — no se rota.
     * </p>
     *
     * @param refreshTokenValue UUID del refresh token emitido en el login
     * @return {@link AuthResponseDTO} con el nuevo access token y el mismo refresh
     *         token
     * @throws UserException con código {@code REFRESH_TOKEN_INVALID} si no existe
     * @throws UserException con código {@code REFRESH_TOKEN_EXPIRED} si ha caducado
     *                       o revocado
     */
    public AuthResponseDTO refreshAccessToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(refreshTokenValue);
        User user = refreshToken.getUser();
        String newAccessToken = JwtUtil.generateToken(user.getUsername(), user.getRole().getName());
        return AuthResponseDTO.builder()
                .user(uService.mapUserToDTO(user))
                .token(newAccessToken)
                .refreshToken(refreshToken.getToken())
                .build();
    }

    /**
     * Verifica el email del usuario usando el token recibido por correo.
     * <p>
     * Comprueba que el token existe, que no ha expirado y que el usuario
     * no está ya verificado. Si todo es correcto, establece {@code isVerified=true}
     * y limpia el token de verificación de la base de datos.
     * </p>
     *
     * @param token UUID de verificación enviado al email del usuario
     * @throws UserException con {@code VERIFICATION_TOKEN_INVALID} si el token no
     *                       existe
     * @throws UserException con {@code VERIFICATION_TOKEN_EXPIRED} si el token ha
     *                       caducado
     * @throws UserException con {@code EMAIL_ALREADY_VERIFIED} si el usuario ya
     *                       estaba verificado
     */
    public void verifyEmail(String token) {
        User user = uService.getUserByVerificationToken(token);

        if (user.isVerified()) {
            throw new UserException(UserException.EMAIL_ALREADY_VERIFIED);
        }
        if (user.getVerificationTokenExpiry() == null ||
                LocalDateTime.now().isAfter(user.getVerificationTokenExpiry())) {
            throw new UserException(UserException.VERIFICATION_TOKEN_EXPIRED);
        }

        // Marcar como verificado y limpiar el token
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        uService.saveUserForHandler(user);
    }

    /**
     * Reenvía el email de verificación al usuario.
     * <p>
     * Genera un nuevo token con expiración de 24h y lo envía al email registrado.
     * Lanza excepción si el usuario ya está verificado.
     * </p>
     *
     * @param usernameOrEmail username o email del usuario
     * @throws UserException con {@code EMAIL_ALREADY_VERIFIED} si ya está
     *                       verificado
     */
    public void resendVerification(String usernameOrEmail) {
        User user = uService.getUserByUsernameOrEmail(usernameOrEmail);

        if (user.isVerified()) {
            throw new UserException(UserException.EMAIL_ALREADY_VERIFIED);
        }

        // Regenerar token con nueva expiración de 24h
        String newToken = UUID.randomUUID().toString();
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
        uService.saveUserForHandler(user);

        emailService.sendVerificationEmail(user.getName(), user.getEmail(), newToken);
    }

}
