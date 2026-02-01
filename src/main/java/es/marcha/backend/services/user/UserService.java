package es.marcha.backend.services.user;

import java.sql.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.exception.UserException;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {
    // Attribs
    @Autowired
    private UserRepository uRepository;

    public static final String USER_DELETED = "USER WAS DELETED";

    // Methods
    public User getUserById(long id) {
        return uRepository.findById(id).filter(user -> !user.isDeleted())
                .orElseThrow(() -> new UserException(UserException.DEFAULT));
    }

    public List<User> getAllUsers() {
        List<User> users = uRepository.findAll();
        if (users.isEmpty()) {
            throw new UserException(UserException.FAILED_FETCH);
        }
        List<User> filteredUsers = users.stream().filter(user -> !user.isDeleted()).toList();
        return filteredUsers;
    }

    public User saveUser(User user) {
        try {
            user.setActive(true);
            user.setBanned(false);
            user.setDeleted(false);
            user.setVerified(false);
            return uRepository.save(user);
        } catch (Exception e) {
            throw new UserException(UserException.FAILED_SAVE, e);
        }
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
    public User updateUser(User updatedUser) {
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
            user.setUpdatedAt(new Date(System.currentTimeMillis()));

            return uRepository.save(updatedUser);
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
        User deletedUser = uRepository.findById(id).orElseThrow(() -> new UserException());
        deletedUser.setDeletedAt(new Date(System.currentTimeMillis()));
        deletedUser.setDeleted(true);
        deletedUser.setActive(false);
        uRepository.save(deletedUser);

        return USER_DELETED;
    }


}
