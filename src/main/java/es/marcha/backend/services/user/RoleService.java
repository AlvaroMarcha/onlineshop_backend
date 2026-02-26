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
    /**
     * Obtiene un rol por su ID.
     *
     * @param id El ID del rol a buscar.
     * @return La entidad {@link Role} correspondiente.
     * @throws RolePermissionsException si el rol no existe.
     */
    public Role getRoleById(long id) {
        return rRepository.findById(id).orElseThrow(
                () -> new RolePermissionsException(RolePermissionsException.NOT_EXIST));
    }

    /**
     * Obtiene todos los roles existentes en el sistema.
     *
     * @return Lista de {@link Role} con todos los roles.
     * @throws RolePermissionsException si no hay ningún rol en la base de datos.
     */
    public List<Role> getAllRoles() {
        List<Role> roles = rRepository.findAll();
        if (roles.isEmpty()) {
            throw new RolePermissionsException(RolePermissionsException.FAILED_FETCH);
        }
        return roles;
    }

    /**
     * Crea y persiste un nuevo rol en el sistema.
     * Verifica que no exista otro rol con el mismo nombre antes de guardarlo.
     *
     * @param role El {@link Role} a crear.
     * @return La entidad {@link Role} persistida.
     * @throws RolePermissionsException si ya existe un rol con el mismo nombre.
     */
    @Transactional
    public Role saveRole(Role role) {
        if (rRepository.existsByName(role.getName()))
            throw new RolePermissionsException(RolePermissionsException.ROLE_ALREADY_EXIST);

        role.setCreatedAt(LocalDateTime.now());
        return rRepository.save(role);
    }

    /**
     * Actualiza el nombre y la descripción de un rol existente.
     *
     * @param role El {@link Role} con los nuevos datos. Debe incluir un ID válido.
     * @return La entidad {@link Role} actualizada.
     * @throws RolePermissionsException si el rol no existe.
     */
    @Transactional
    public Role updateRole(Role role) {
        Role existRole = getRoleById(role.getId());

        existRole.setName(role.getName());
        System.out.println(role.getDescription());
        existRole.setDescription(role.getDescription());
        existRole.setUpdatedAt(LocalDateTime.now());

        return rRepository.save(existRole);
    }

    /**
     * Elimina un rol por su ID. No permite eliminar el rol {@code ADMIN}.
     *
     * @param id El ID del rol a eliminar.
     * @return Mensaje de confirmación de la eliminación.
     * @throws RolePermissionsException si el rol no existe o es el rol ADMIN.
     */
    public String deleteRole(long id) {
        Role existRole = getRoleById(id);
        boolean isAdmin = existRole.getName().equals("ADMIN");
        if (isAdmin)
            throw new RolePermissionsException();

        rRepository.delete(existRole);
        return ROLE_DELETED;
    }
}
