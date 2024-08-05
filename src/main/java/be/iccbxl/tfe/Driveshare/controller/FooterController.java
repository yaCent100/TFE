package be.iccbxl.tfe.Driveshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FooterController {

    @GetMapping("/comment-ça-marche")
    public String showHowItWorks() {
        return "footer/comment-ca-marche";
    }
}
