package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.security.CustomUserDetail;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ChatMessageService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ChatMessageService chatMessageService, UserService userService) {
        this.chatMessageService = chatMessageService;
        this.userService = userService;
    }


    @MessageMapping("/chat/{reservationId}")
    @SendTo("/topic/messages/{reservationId}")
    public ChatMessageDTO sendMessage(ChatMessageDTO chatMessageDTO, @DestinationVariable Long reservationId, Principal principal) {
        if (principal == null) {
            throw new RuntimeException("No authentication available");
        }

        System.out.println("Received ChatMessageDTO: " + chatMessageDTO);

        if (chatMessageDTO.getToUserId() == null || chatMessageDTO.getFromUserId() == null) {
            throw new RuntimeException("toUserId or fromUserId is null");
        }

        CustomUserDetail userDetails = (CustomUserDetail) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        User fromUser = userService.getUserById(chatMessageDTO.getFromUserId());
        System.out.println("fromUser: " + fromUser);

        User toUser = userService.getUserById(chatMessageDTO.getToUserId());
        if (toUser == null) {
            throw new RuntimeException("ToUser not found");
        }
        System.out.println("toUser: " + toUser);

        ChatMessage chatMessage = CarMapper.toChatMessage(chatMessageDTO);
        chatMessage.setFromUser(fromUser);
        chatMessage.setToUser(toUser);
        chatMessage.setSentAt(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageService.save(chatMessage);
        return CarMapper.toChatMessageDTO(savedMessage);
    }


}




