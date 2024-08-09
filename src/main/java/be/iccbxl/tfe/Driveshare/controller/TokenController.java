package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.service.serviceImpl.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TokenController {

    @Autowired
    private TokenService tokenService;

    @GetMapping("/reset-password")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        String email = tokenService.validateToken(token);
        if (email == null) {
            model.addAttribute("message", "Invalid or expired token");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "login/resetPassword";
    }
}
