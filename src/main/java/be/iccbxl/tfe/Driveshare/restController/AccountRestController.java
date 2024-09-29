package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.CarService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.FileStorageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PhotoService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@Tag(name = "Account Management", description = "Gestion du compte utilisateur")
public class AccountRestController {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private PhotoService photoService;

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

    @DeleteMapping("/api/deleteCar/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        Car car = carService.getCarById(id);

        if (car == null) {
            return ResponseEntity.notFound().build(); // Si la voiture n'existe pas
        }

        carService.deleteCar(id);  // Suppression de la voiture dans la base de données
        return ResponseEntity.noContent().build(); // Réponse 204 No Content après la suppression
    }

    @DeleteMapping("/api/deletePhoto/{carId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long carId, @RequestBody Map<String, String> body) {
        String photoUrl = body.get("photoUrl");

        if (photoUrl == null || photoUrl.isEmpty()) {
            return ResponseEntity.badRequest().build(); // 400 Bad Request si l'URL de la photo est manquante
        }

        boolean isDeleted = photoService.deletePhotoFromCar(carId, photoUrl);

        if (isDeleted) {
            return ResponseEntity.noContent().build(); // 204 No Content si la suppression a réussi
        } else {
            return ResponseEntity.notFound().build(); // 404 Not Found si la voiture ou la photo n'existe pas
        }
    }


    @PostMapping("/api/addPhoto")
    public ResponseEntity<String> addPhoto(@RequestParam("carId") Long carId,
                                           @RequestParam("photo") MultipartFile photo) {

        System.out.println("Reçu carId : " + carId);
        System.out.println("Reçu photo : " + (photo != null ? photo.getOriginalFilename() : "Aucune photo reçue"));
        // Vérifier si les paramètres sont bien reçus
        if (carId == null) {
            return ResponseEntity.badRequest().body("carId is missing");
        }
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.badRequest().body("photo is missing");
        }

        // Logique pour enregistrer la photo
        String photoUrl = fileStorageService.storeFile(photo, "photo-car");

        // Mise à jour de la voiture avec l'URL de la photo
        carService.updateCarPhoto(carId, photoUrl);

        return ResponseEntity.ok().build();
    }










}
