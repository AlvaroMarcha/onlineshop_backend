package es.marcha.backend.services;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.model.User;
import es.marcha.backend.repository.UserRepository;
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
