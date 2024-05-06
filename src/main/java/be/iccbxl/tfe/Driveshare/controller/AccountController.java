package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    private final UserService userService;

    @Autowired
    public AccountController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/account")
public String getAccount(Model model){
    /*User user = userService.getUserById(user); // Supposons que cette méthode récupère l'utilisateur connecté
    model.addAttribute("user", user);*/
    return "account/index";
}

}
