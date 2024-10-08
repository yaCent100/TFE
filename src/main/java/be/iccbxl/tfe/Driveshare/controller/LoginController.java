package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;


@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;


    @GetMapping("/login")
    public String getLogin() {
        return "login/login";
    }



    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute @Valid User user,
                                          BindingResult result,
                                          Model model) {

        // Vérification si l'email existe déjà
        if (userService.emailExists(user.getEmail())) {
            result.rejectValue("email", "error.user", "Cette adresse email est déjà utilisée.");
        }

        // Vérification des erreurs de validation
        if (result.hasErrors()) {
            model.addAttribute("inscriptionTab", true);  // Activer l'onglet inscription en cas d'erreur
            return "login";  // Retourner à la même page sans redirection
        }

        // Si tout est bon, ajouter l'utilisateur
        userService.addUser(user);
        model.addAttribute("successMessage", "Inscription réussie. Veuillez vous connecter.");
        return "redirect:/login?success=true";  // Rediriger vers la page de connexion après succès
    }












}
