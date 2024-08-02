package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FileStorageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @PostMapping("/{id}/update-profile-picture")
    public String updateProfilePicture(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        String directory = "photo-profile"; // Spécifiez le répertoire de stockage pour les photos de profil
        String fileName = fileStorageService.storeFile(file, directory);

        User user = userService.getUserById(id);
        if (user != null) {
            user.setPhotoUrl(fileName);
            userService.save(user);
            return "Photo de profil mise à jour avec succès";
        } else {
            return "Utilisateur non trouvé";
        }
    }
}
