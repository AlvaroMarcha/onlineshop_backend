package es.marcha.backend.services.user;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.marcha.backend.exception.RolePermissionsException;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.repository.user.RoleRepository;
import jakarta.transaction.Transactional;

@Service
public class RoleService {
    // Attribs
    @Autowired
    private RoleRepository rRepository;

    private final static String ROLE_DELETED = "ROLE_wAS_DELETED";

    // Methods
    public Role getRoleById(long id) {
        return rRepository.findById(id).orElseThrow(
                () -> new RolePermissionsException(RolePermissionsException.NOT_EXIST));
    }

    public List<Role> getAllRoles() {
        List<Role> roles = rRepository.findAll();
        if (roles.isEmpty()) {
            throw new RolePermissionsException(RolePermissionsException.FAILED_FETCH);
        }
        return roles;
    }

    @Transactional
    public Role saveRole(Role role) {
        if (rRepository.existsByName(role.getName()))
            throw new RolePermissionsException(RolePermissionsException.ROLE_ALREADY_EXIST);

        role.setCreatedAt(LocalDateTime.now());
        return rRepository.save(role);
    }

    @Transactional
    public Role updateRole(Role role) {
        Role existRole = getRoleById(role.getId());

        existRole.setName(role.getName());
        System.out.println(role.getDescription());
        existRole.setDescription(role.getDescription());
        existRole.setUpdatedAt(LocalDateTime.now());

        return rRepository.save(existRole);
    }

    public String deleteRole(long id) {
        Role existRole = getRoleById(id);
        boolean isAdmin = existRole.getName().equals("ADMIN");
        if (isAdmin)
            throw new RolePermissionsException();

        rRepository.delete(existRole);
        return ROLE_DELETED;
    }
}
