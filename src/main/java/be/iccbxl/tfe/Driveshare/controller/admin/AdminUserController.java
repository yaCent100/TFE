package be.iccbxl.tfe.Driveshare.controller.admin;

import be.iccbxl.tfe.Driveshare.model.Role;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.RoleService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {


    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @GetMapping("/all")
    public String getAllUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users/all";
    }

    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/users/add-user";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        userService.save(user);
        return "redirect:/admin/users/all";
    }

    @GetMapping("/roles")
    public String showRolesAndPermissions(Model model) {
        // Implement fetching roles and permissions
        model.addAttribute("roles", userService.getAuthenticatedUser().getRoles());
        return "admin/users/add-role";
    }

    @PostMapping("/roles")
    public String saveRole(@RequestParam Role role) {
        // Implement saving roles and permissions
        roleService.saveRole(role);
        return "redirect:/admin/users/roles";
    }


    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users/all";
    }
}
