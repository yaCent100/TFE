package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FileStorageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@RestController
@Tag(name = "Account Management", description = "Gestion du compte utilisateur")
public class AccountRestController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Mettre à jour la photo de profil", description = "Met à jour la photo de profil d'un utilisateur spécifique.")
    @PostMapping("/api/account/{id}/update-profile-picture")
    public ResponseEntity<String> updateProfilePicture(
            @Parameter(description = "L'ID de l'utilisateur", required = true) @PathVariable Long id,
            @Parameter(description = "Le fichier image de la photo de profil", required = true) @RequestParam("file") MultipartFile file) {
        try {
            String directory = "profil";
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour de la photo de profil");
        }
    }

    @Operation(summary = "Supprimer un utilisateur", description = "Supprime un utilisateur de la plateforme.")
    @DeleteMapping("/api/users/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "L'ID de l'utilisateur à supprimer", required = true) @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Utilisateur supprimé avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression de l'utilisateur");
        }
    }
}
