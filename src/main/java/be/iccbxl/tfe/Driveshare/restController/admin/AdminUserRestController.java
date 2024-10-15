package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.Document;
import be.iccbxl.tfe.Driveshare.model.Role;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.DocumentService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.EmailService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.RoleService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin User Management", description = "API pour la gestion des utilisateurs par les administrateurs")
public class AdminUserRestController {

    @Autowired
    private UserService userService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private RoleService roleService;
    @Autowired
    private EmailService emailService;

    @Operation(summary = "Obtenir tous les utilisateurs", description = "Récupérer tous les utilisateurs inscrits sur la plateforme.")
    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers().stream().map(MapperDTO::toDTO).collect(Collectors.toList());

        users.forEach(user -> {
            System.out.println("User: " + user.getLastName() + " " + user.getFirstName());
            user.getDocuments().forEach(document -> {
                System.out.println("Document Type: " + document.getDocumentType() + ", URL: " + document.getUrl());
            });
        });

        return users;
    }


    @Operation(summary = "Obtenir les utilisateurs par rôle", description = "Récupérer une liste d'utilisateurs en fonction de leur rôle.")
    @GetMapping("/users-by-role")
    public ResponseEntity<List<UserDTO>> getUsersByRole(
            @Parameter(description = "Le rôle des utilisateurs à récupérer", required = true)
            @RequestParam String role) {
        List<UserDTO> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Ajouter un nouvel utilisateur", description = "Ajouter un nouvel utilisateur à la plateforme.")
    @PostMapping("/add-user")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        try {
            // Appel au service pour ajouter l'utilisateur
            UserDTO newUser = userService.addUserAdmin(user);
            return ResponseEntity.ok(newUser); // 200 OK avec le nouvel utilisateur
        } catch (IllegalArgumentException e) {
            // Retourne un code 400 BAD REQUEST si l'email est déjà utilisé
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            // Retourne un code 500 INTERNAL SERVER ERROR pour toute autre erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur est survenue lors de l'ajout de l'utilisateur.");
        }
    }

    @Operation(summary = "Obtenir les permissions d'un utilisateur", description = "Récupérer les rôles attribués à un utilisateur.")
    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<?> getUserPermissions(@PathVariable Long userId) {
        // Trouver l'utilisateur par son ID
        User user = userService.getUserById(userId);

        // Vérifier si l'utilisateur existe
        if (user != null) {
            // Récupérer les rôles attribués à l'utilisateur
            List<String> permissions = userService.getUserRoles(userId);

            // Créer une réponse avec les rôles et les informations de base de l'utilisateur
            Map<String, Object> response = new HashMap<>();
            response.put("roles", permissions);
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());

            // Retourner la réponse avec un statut OK
            return ResponseEntity.ok(response);
        } else {
            // Si l'utilisateur n'est pas trouvé, retourner une erreur 404
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @Operation(summary = "Obtenir les permissions d'un utilisateur", description = "Récupérer les rôles attribués à un utilisateur.")
    @PutMapping("/users/{userId}/permissions")
    public ResponseEntity<?> updateUserPermissions(
            @PathVariable Long userId,
            @RequestBody Map<String, List<String>> payload) {

        // Extraire les rôles du corps de la requête
        List<String> permissions = payload.get("permissions");

        if (permissions == null) {
            return ResponseEntity.badRequest().body("Permissions not provided");
        }

        // Appeler le service pour mettre à jour les rôles de l'utilisateur
        try {
            userService.updateUserPermissions(userId, permissions);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @Operation(summary = "Obtenir tous les rôles disponibles", description = "Récupérer une liste de tous les rôles disponibles.")
    @GetMapping("/roles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleDTO> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(summary = "Récupérer les documents d'un utilisateur", description = "Renvoie la liste des documents d'un utilisateur spécifique.")
    @GetMapping("/users/{userId}/documents")
    public List<DocumentDTO> getUserDocuments(@PathVariable Long userId) {
        return documentService.getByUserIdDocumentDTO(userId);
    }


    @Operation(summary = "Supprimer un document d'un utilisateur", description = "Supprimer un document spécifique d'un utilisateur.")
    @DeleteMapping("/users/{userId}/documents/{documentType}")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "L'ID de l'utilisateur", required = true)
            @PathVariable Long userId,
            @Parameter(description = "Le type de document à supprimer", required = true)
            @PathVariable String documentType) {
        documentService.deleteDocumentsByUserIdAndType(userId, documentType);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{id}/verify")
    public ResponseEntity<?> verifyUserDocuments(@PathVariable Long id) {
        Optional<User> optionalUser = Optional.ofNullable(userService.getUserById(id));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setVerified(true);  // Marquer l'utilisateur comme vérifié
            userService.save(user);
            return ResponseEntity.ok("Les documents ont été vérifiés avec succès.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Utilisateur non trouvé.");
        }
    }


    @Operation(summary = "Supprimer un utilisateur", description = "Supprimer un utilisateur de la plateforme.")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "L'ID de l'utilisateur à supprimer", required = true)
            @PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*@PostMapping("/users/{userId}/send-email")
    public ResponseEntity<String> sendEmail(
            @PathVariable Long userId,
            @RequestBody Map<String, String> emailRequest) {
        String subject = emailRequest.get("subject");
        String message = emailRequest.get("message");

        // Assurez-vous que les valeurs ne sont pas nulles
        if (subject == null || message == null) {
            return ResponseEntity.badRequest().body("Required parameters are missing.");
        }

        try {
            emailService.sendEmailToUser(userId, subject, message);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending email: " + e.getMessage());
        }
    }*/







}

