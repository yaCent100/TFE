package be.iccbxl.tfe.Driveshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FaqController {

    @GetMapping("/faq")
    public String showFaq() {
        return "faq/index"; // Le nom de votre template Thymeleaf pour la page FAQ
    }
}
