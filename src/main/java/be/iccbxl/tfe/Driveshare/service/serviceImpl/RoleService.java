package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.RoleDTO;
import be.iccbxl.tfe.Driveshare.model.Role;
import be.iccbxl.tfe.Driveshare.repository.RoleRepository;
import be.iccbxl.tfe.Driveshare.repository.UserRepository;
import be.iccbxl.tfe.Driveshare.service.RoleServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoleService implements RoleServiceI {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;




    @Override
    public Role getRoleById(Long id) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        return optionalRole.orElse(null);
    }

    @Override
    public Role saveRole(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role updateRole(Long id, Role newRole) {
        Optional<Role> optionalRole = roleRepository.findById(id);
        if (optionalRole.isPresent()) {
            Role existingRole = optionalRole.get();
           existingRole.setRole(newRole.getRole());
            return roleRepository.save(existingRole);
        }
        return null;
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    public List<RoleDTO> getAllRoles() {
        List<Role> roles = (List<Role>) roleRepository.findAll(); // Assurez-vous que ce retour est correct
        return roles.stream()
                .map(role -> new RoleDTO(role.getId(), role.getRole())) // Vérifiez bien que role.getId() et role.getName() ne sont pas null
                .collect(Collectors.toList());
    }

}
