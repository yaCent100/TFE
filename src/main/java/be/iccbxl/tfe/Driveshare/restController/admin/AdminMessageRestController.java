package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Driveshare.DTO.ConversationDTO;
import be.iccbxl.tfe.Driveshare.DTO.NotificationDTO;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ChatMessageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/messages")
public class AdminMessageRestController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private NotificationService  notificationService;


    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getAllConversations() {
        List<ConversationDTO> conversations = chatMessageService.getAllConversations();
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/chats")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@RequestParam Long userId) {
        List<ChatMessageDTO> messages = chatMessageService.getMessagesByUser(userId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationDTO>> getNotifications(@RequestParam Long userId) {
        List<NotificationDTO> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }


    }


