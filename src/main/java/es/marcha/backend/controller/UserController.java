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

import es.marcha.backend.model.Role;
import es.marcha.backend.model.User;
import es.marcha.backend.services.RoleService;
import es.marcha.backend.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    // Attribs
    @Autowired
    private UserService uService;

    @Autowired
    private RoleService rService;

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = uService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        User user = uService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        Role role = rService.getRoleById(user.getRole().getId());
        User userSaved = uService.saveUser(user);
        userSaved.setRole(role);
        return new ResponseEntity<>(userSaved, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@RequestBody User updatedUser) {
        User user = uService.updateUser(updatedUser);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        String msg = uService.deleteUser(id);
        return new ResponseEntity<>(msg, HttpStatus.OK);
    }



}
