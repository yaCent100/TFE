package be.iccbxl.tfe.Driveshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {

    @GetMapping("/comment-ça-marche")
    public String showHowItWorks() {
        return "footer/comment-ca-marche";
    }

    @GetMapping("/condition-utilisation")
    public String showConditionsUtilisations() {
        return "footer/conditions-utilisations"; // Le nom de votre template Thymeleaf pour la page FAQ
    }

    @GetMapping("/louer-ma-voiture")
    public String showLouerMaVoiture() {
        return "footer/louer-ma-voiture"; // Le nom de votre template Thymeleaf pour la page FAQ
    }




}
