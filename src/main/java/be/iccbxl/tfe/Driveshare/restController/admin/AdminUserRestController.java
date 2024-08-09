package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.DocumentDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.UserDTO;
import be.iccbxl.tfe.Driveshare.model.Document;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.DocumentService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.RoleService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminUserRestController {

    @Autowired
    private UserService userService;


    @Autowired
    private DocumentService documentService;

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream().map(MapperDTO::toDTO).collect(Collectors.toList());

        users.forEach(user -> {
            System.out.println("User: " + user.getNom() + " " + user.getPrenom());
            user.getDocuments().forEach(document -> {
                System.out.println("Document Type: " + document.getDocumentType() + ", URL: " + document.getUrl());
            });
        });

        return users;    }

    @GetMapping("/users-by-role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@RequestParam String role) {
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/add-user")
    public ResponseEntity<User> addUser(@RequestBody User user) {
        User newUser = userService.save(user);
        return ResponseEntity.ok(newUser);
    }

    @PutMapping("/api/admin/users/{userId}/permissions")
    public ResponseEntity<?> updateUserPermissions(@PathVariable Long userId, @RequestBody Map<String, List<String>> payload) {
        List<String> permissions = payload.get("permissions");
        if (permissions == null) {
            return ResponseEntity.badRequest().body("Permissions not provided");
        }
        try {
            userService.updateUserPermissions(userId, permissions);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/users/{userId}/documents/{documentType}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long userId, @PathVariable String documentType) {
        System.out.println("Tentative de suppression des documents de type: " + documentType + " pour l'utilisateur avec l'ID: " + userId);
        documentService.deleteDocumentsByUserIdAndType(userId, documentType);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        System.out.println("Tentative de suppression de l'utilisateur avec l'ID: " + userId);
        try {
            userService.deleteUser(userId);
            System.out.println("Utilisateur supprimé avec succès");
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.noContent().build();
    }



}
