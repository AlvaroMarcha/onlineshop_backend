package es.marcha.backend.services.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.dto.response.BannedUserResponseDTO;
import es.marcha.backend.dto.response.UserResponseDTO;
import es.marcha.backend.exception.UserException;
import es.marcha.backend.mapper.UserMapper;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.user.UserRepository;
import es.marcha.backend.utils.Validations;
import jakarta.transaction.Transactional;

@Service
public class UserService {
    // Attribs
    @Autowired
    private UserRepository uRepository;
    @Autowired
    private RoleService rService;

    public static final String USER_DELETED = "USER WAS DELETED";

    // Methods
    public UserResponseDTO getUserById(long id) {
        return uRepository.findById(id)
                .filter(user -> !user.isDeleted() || !user.isBanned())
                .map(UserMapper::toUserDTO)
                .orElseThrow(() -> new UserException());
    }

    public User getUserByIdForHandler(long id) {
        return uRepository.findById(id)
                .filter(user -> !user.isDeleted() || !user.isBanned())
                .orElseThrow(() -> new UserException());
    }

    public Optional<UserResponseDTO> getUserByUsername(String username) {
        return uRepository.findByUsername(username)
                .map(UserMapper::toUserDTO);
    }


    /**
     * Obtiene un usuario a partir de su username o email.
     * <p>
     * Este método primero determina si el parámetro proporcionado es un email válido o un username.
     * Dependiendo del caso, busca al usuario en la base de datos usando
     * {@code uRepository.findByEmail} o {@code uRepository.findByUsername}. Además, filtra los
     * usuarios que estén marcados como eliminados ({@code isDeleted}) o baneados
     * ({@code isBanned}), de modo que solo se devuelvan usuarios activos. Si no se encuentra ningún
     * usuario válido, se lanza una excepción {@link UserException}.
     * </p>
     *
     * @param usernameOrEmail el username o email del usuario a buscar
     * @return el usuario encontrado y activo
     * @throws UserException si no existe un usuario con ese username/email, o si el usuario está
     *         eliminado o baneado
     */
    public User getUserByUsernameOrEmail(String usernameOrEmail) {
        boolean isEmail = Validations.validateEmail(usernameOrEmail);
        if (isEmail) {
            return uRepository.findByEmail(usernameOrEmail)
                    .filter(user -> !user.isDeleted() || !user.isBanned())
                    .orElseThrow(() -> new UserException());
        } else {
            return uRepository.findByUsername(usernameOrEmail)
                    .filter(user -> !user.isDeleted() || !user.isBanned())
                    .orElseThrow(() -> new UserException());
        }
    }

    public List<UserResponseDTO> getAllUsers() {
        List<User> users = uRepository.findAll();
        if (users.isEmpty()) {
            throw new UserException(UserException.FAILED_FETCH);
        }

        List<User> filteredUsers = users.stream()
                .filter(user -> !user.isDeleted())
                .toList();
        List<UserResponseDTO> usersDTO = filteredUsers.stream()
                .map(UserMapper::toUserDTO)
                .toList();
        return usersDTO;
    }

    public UserResponseDTO saveUser(User user) {
        Role role = rService.getRoleById(user.getRole().getId());
        try {
            user.setRole(role);
            user.setActive(true);
            user.setBanned(false);
            user.setDeleted(false);
            user.setVerified(false);
            return UserMapper.toUserDTO(uRepository.save(user));
        } catch (Exception e) {
            throw new UserException(UserException.FAILED_SAVE, e);
        }
    }

    public User saveUserForHandler(User user) {
        return uRepository.save(user);
    }

    /**
     * Actualiza los datos de un usuario existente en la base de datos. Solo se actualizan ciertos
     * campos, y se marca la fecha de actualización con la hora actual.
     *
     * @param updatedUser El objeto {@link User} que contiene los nuevos datos a actualizar. Debe
     *        incluir un ID válido de un usuario existente.
     * @return El {@link User} actualizado después de guardarlo en la base de datos, o {@code null}
     *         si el usuario no existe.
     */
    @Transactional
    public UserResponseDTO updateUser(User updatedUser) {
        try {
            User user = uRepository.findById(updatedUser.getId())
                    .orElseThrow(() -> new UserException());

            user.setName(updatedUser.getName());
            user.setSurname(updatedUser.getSurname());
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setProfileImageUrl(updatedUser.getProfileImageUrl());

            user.setActive(updatedUser.isActive());
            user.setLastLogin(updatedUser.getLastLogin());
            user.setUpdatedAt(LocalDateTime.now());

            return UserMapper.toUserDTO(uRepository.save(updatedUser));
        } catch (Exception e) {
            throw new UserException(UserException.FAILED_UPDATE, e);
        }
    }

    /**
     * Marca un usuario como eliminado en la base de datos, sin borrarlo físicamente. Se establece
     * la fecha de eliminación y se activa el flag {@code isDeleted}.
     *
     * @param id El ID del usuario que se desea eliminar.
     * @return Un mensaje {@link String} indicando si el usuario fue eliminado correctamente o si no
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
        return bannedUser;
    }


}
