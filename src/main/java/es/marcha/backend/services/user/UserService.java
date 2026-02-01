package es.marcha.backend.services.user;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.marcha.backend.model.user.User;
import es.marcha.backend.repository.user.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {
    // Attribs
    @Autowired
    private UserRepository uRepository;

    // Methods
    public User getUserById(long id) {
        return uRepository.findById(id).orElse(null);
    }

    public List<User> getAllUsers() {
        return uRepository.findAll();
    }

    public User saveUser(User user) {
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
    public User updateUser(User updatedUser) {
        Optional<User> existUser = uRepository.findById(updatedUser.getId());
        if (!existUser.isPresent())
            return null;
        User user = existUser.get();
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
        Optional<User> existUser = uRepository.findById(id);
        if (!existUser.isPresent()) {
            return "User was not found";
        }
        User deletedUser = existUser.get();
        deletedUser.setDeletedAt(new Date(System.currentTimeMillis()));
        deletedUser.setDeleted(true);
        uRepository.save(deletedUser);

        return "User was deleted";
    }


}
