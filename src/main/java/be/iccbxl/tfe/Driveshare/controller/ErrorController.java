package be.iccbxl.tfe.Driveshare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ErrorController {

    @GetMapping("/errorPage")
    public String showErrorPage(Model model, @RequestParam(value = "error", required = false) String error) {
        model.addAttribute("error", error);
        return "errorPage";
    }
}