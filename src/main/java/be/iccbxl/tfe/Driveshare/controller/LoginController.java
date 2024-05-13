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

import java.io.File;
import java.io.IOException;

@Controller
public class LoginController {





    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Value("${upload.photo.dir}")
    private String photoUploadDir;

    @Value("${upload.licence.dir}")
    private String licenceUploadDir;

    @Value("${upload.identity.dir}")
    private String identityUploadDir;



    @GetMapping("/login")
    public String getLogin() {
        return "login/login";
    }


    @PostMapping("/register")
    public String processRegistrationForm(@ModelAttribute @Valid User user,
                                          @RequestParam("photo") MultipartFile profilePhoto,
                                          @RequestParam("permis") MultipartFile drivingLicense,
                                          @RequestParam("identity") MultipartFile idCard,
                                          BindingResult result,
                                          RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorLogin", "Veuillez corriger les champs invalides.");
            return "redirect:/login";
        }

        // Enregistrer la photo
           /* String profilePhotoUrl = userService.uploadFile(profilePhoto,photoUploadDir );
            user.setPhotoUrl(profilePhotoUrl);

            // Enregistrer la licence
            String drivingLicenseUrl = userService.uploadFile(drivingLicense, licenceUploadDir);
            user.setPermisConduire(drivingLicenseUrl);

            // Enregistrer la carte d'identité
            String idCardUrl = userService.uploadFile(idCard, identityUploadDir);
            user.setCarteIdentite(idCardUrl);*/

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        userService.addUser(user);
        redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie. Veuillez vous connecter.");
        return "redirect:/login?success=true";
    }









}
