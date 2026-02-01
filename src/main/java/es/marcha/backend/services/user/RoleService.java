package es.marcha.backend.services.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import es.marcha.backend.model.user.Role;
import es.marcha.backend.repository.user.RoleRepository;

@Service
public class RoleService {
    // Attribs
    @Autowired
    private RoleRepository rRepository;

    // Methods
    public Role getRoleById(long id) {
        return rRepository.findById(id).orElse(null);
    }

    public List<Role> getAllRoles() {
        return rRepository.findAll();
    }


}
