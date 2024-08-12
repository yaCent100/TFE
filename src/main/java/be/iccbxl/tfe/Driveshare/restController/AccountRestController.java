package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FileStorageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountRestController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @PostMapping("/{id}/update-profile-picture")
    public ResponseEntity<String> updateProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            // Assurez-vous que le répertoire est correct et accessible
            String directory = "profil"; // Répertoire de stockage des photos de profil
            String fileName = fileStorageService.storeFile(file, directory);

            User user = userService.getUserById(id);
            if (user != null) {
                user.setPhotoUrl(fileName);
                userService.save(user);
                return ResponseEntity.ok("Photo de profil mise à jour avec succès");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé");
            }
        } catch (Exception e) {
            // Gérer toute autre exception
            System.err.println("Erreur lors de la mise à jour de la photo de profil : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la mise à jour de la photo de profil");
        }
    }

}
