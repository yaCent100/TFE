package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.Role;

import java.util.List;

public interface RoleServiceI {

    List<Role> getAllRoles();
    Role getRoleById(Long id);
    Role saveRole(Role role);
    Role updateRole(Long id, Role role);
    void deleteRole(Long id);
}
