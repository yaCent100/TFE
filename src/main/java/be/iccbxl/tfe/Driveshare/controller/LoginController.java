package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class LoginController {

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

    @PostMapping("/inscription")
    public String soumettreFormulaireInscription(@RequestParam String password,
                                                 @RequestParam String nom,
                                                 @RequestParam String prenom,
                                                 @RequestParam String email,
                                                 @RequestParam String adresse,
                                                 @RequestParam String codepostal,
                                                 @RequestParam String commune,
                                                 @RequestParam String date_naissance,
                                                 @RequestParam MultipartFile profil_photo,
                                                 @RequestParam MultipartFile permis_conduire,
                                                 @RequestParam MultipartFile carte_identite,
                                                 Model model) {
        // Validation des champs (exemple: vérifier si les champs obligatoires sont remplis)

        if (password == null || password.isEmpty()) {
            model.addAttribute("error", "Le mot de passe est obligatoire.");
            return "inscription"; // Redirection vers la page d'inscription en cas de mot de passe vide
        }

        // Hashage du mot de passe avec BCrypt
        String hashedPassword = passwordEncoder.encode(password);

        // Autres validations à ajouter si nécessaire

        try {
            // Enregistrement de l'utilisateur (exemple: sauvegarde dans la base de données)
            User user = new User(nom, prenom, email, adresse, codepostal, commune, date_naissance, hashedPassword, profil_photo,
                                    permis_conduire, carte_identite);
            // Vous pouvez également traiter les fichiers téléchargés ici


            // Redirection vers la page de succès après inscription
            return "redirect:/dashboard"; // Redirection vers la page de tableau de bord après inscription
        } catch (Exception ex) {
            // Gestion des erreurs (exemple: envoi d'un message d'erreur à l'utilisateur)
            model.addAttribute("error", "Une erreur s'est produite lors de l'inscription. Veuillez réessayer.");
            return "inscription"; // Redirection vers la page d'inscription en cas d'erreur
        }
}
