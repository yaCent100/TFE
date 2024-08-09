package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.TokenService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/password")
public class PasswordResetRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TokenService tokenService;


    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestParam("email") String email) {
        boolean isEmailSent = userService.sendPasswordResetEmail(email);
        if (isEmailSent) {
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Email de réinitialisation envoyé avec succès."));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", "Erreur lors de l'envoi de l'email de réinitialisation."));
        }
    }

    @GetMapping("/reset")
    public String showResetPasswordPage(@RequestParam("token") String token, Model model) {
        String email = tokenService.validateToken(token);
        if (email == null) {
            model.addAttribute("message", "Invalid or expired token");
            return "redirect:/login";
        }
        model.addAttribute("token", token);
        return "resetPassword";
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(@RequestParam("token") String token,
                                                @RequestParam("password") String password) {
        String email = tokenService.validateToken(token);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired token");
        }

        User user = userService.findByEmail(email);
        if (user != null) {
            userService.changeUserPassword(user, password);
            return ResponseEntity.ok("Password reset successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User not found");
        }
    }
}