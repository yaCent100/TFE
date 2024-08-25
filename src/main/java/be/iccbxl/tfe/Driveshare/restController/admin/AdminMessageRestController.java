package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ChatMessageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/messages")
@Tag(name = "Admin Message and Notification Management", description = "API pour la gestion des messages et notifications par les administrateurs")
public class AdminMessageRestController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private NotificationService notificationService;

    @Operation(summary = "Obtenir toutes les conversations", description = "Récupérer la liste de toutes les conversations entre tous les utilisateurs.")
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getAllConversations() {
        // Récupérez toutes les conversations
        List<ConversationDTO> conversations = chatMessageService.getAllConversations();

        // Retourner la liste complète des conversations
        return ResponseEntity.ok(conversations);
    }






    @Operation(summary = "Obtenir les messages pour une réservation", description = "Récupérer tous les messages échangés pour une réservation spécifique.")
    @GetMapping("/chats/reservation/{reservationId}")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessagesByReservation(
            @Parameter(description = "L'ID de la réservation pour récupérer les messages", required = true)
            @PathVariable Long reservationId) {

        // Récupérez tous les messages de la réservation en utilisant l'ID de la réservation
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByReservationId(reservationId);

        return ResponseEntity.ok(messages);
    }


    @Operation(summary = "Obtenir les notifications d'un utilisateur", description = "Récupérer toutes les notifications pour un utilisateur spécifique.")
    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            @Parameter(description = "L'ID de l'utilisateur pour récupérer les notifications", required = true)
            @RequestParam Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    // Nouvelle API pour récupérer uniquement les utilisateurs avec messages ou notifications
    @Operation(summary = "Obtenir les utilisateurs avec des messages ou des notifications", description = "Récupérer les utilisateurs ayant des messages ou des notifications.")
    @GetMapping("/users-with-messages-or-notifications")
    public ResponseEntity<List<UserDTO>> getUsersWithMessagesOrNotifications() {
        List<User> usersWithMessages = chatMessageService.getAllUsersWithMessages();
        List<User> usersWithNotifications = notificationService.getAllUsersWithNotifications();

        // Fusionner les utilisateurs ayant soit des messages, soit des notifications, en supprimant les doublons
        List<User> uniqueUsers = usersWithMessages;
        usersWithNotifications.forEach(user -> {
            if (!uniqueUsers.contains(user)) {
                uniqueUsers.add(user);
            }
        });

        // Transformer en DTO
        List<UserDTO> userDTOs = uniqueUsers.stream()
                .map(user -> new UserDTO(user.getId(),user.getFirstName(), user.getLastName(), user.getPhotoUrl()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        Map<String, Object> dashboardData = new HashMap<>();

        // Obtenir toutes les conversations
        List<ConversationDTO> conversations = chatMessageService.getAllConversations();
        int totalChats = conversations.size();  // Total des discussions

        // Calcul du total des notifications
        int totalNotifications = notificationService.getTotalNotifications();  // Total des notifications

        // Calcul des messages par jour
        Map<String, Long> messagesByDay = chatMessageService.getMessagesByDay();  // Ex: {"2024-08-01": 5, "2024-08-02": 7}

        // Calcul des notifications par jour
        Map<String, Long> notificationsByDay = notificationService.getNotificationsByDay();  // Ex: {"2024-08-01": 2, "2024-08-02": 4}

        // Ajouter les KPI calculés dans la réponse
        dashboardData.put("totalChats", totalChats);
        dashboardData.put("totalNotifications", totalNotifications);
        dashboardData.put("messagesByDay", messagesByDay);  // Ajoutez les données des messages par jour
        dashboardData.put("notificationsByDay", notificationsByDay);  // Ajoutez les données des notifications par jour

        return ResponseEntity.ok(dashboardData);
    }

}


