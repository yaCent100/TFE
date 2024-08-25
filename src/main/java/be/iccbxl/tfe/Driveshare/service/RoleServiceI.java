package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.DTO.RoleDTO;
import be.iccbxl.tfe.Driveshare.model.Role;

import java.util.List;

public interface RoleServiceI {

    List<RoleDTO> getAllRoles();
    Role getRoleById(Long id);
    Role saveRole(Role role);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);
}
