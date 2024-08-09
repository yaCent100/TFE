package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.model.Notification;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.ChatMessageRepository;
import be.iccbxl.tfe.Driveshare.service.ChatMessageServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatMessageService implements ChatMessageServiceI {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserService userService;

    @Override
    public ChatMessage save(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> getMessagesByReservationId(Long reservationId) {
        return chatMessageRepository.findByReservationId(reservationId);
    }

    public User getUserById(Long id) {
        return userService.getUserById(id);
    }

    public List<ChatMessageDTO> getAllChatMessages() {
        List<ChatMessage> messages = chatMessageRepository.findAll();
        return messages.stream().map(MapperDTO::toChatMessageDTO).collect(Collectors.toList());
    }

    public List<ConversationDTO> getAllConversations() {
        List<ChatMessage> messages = chatMessageRepository.findAll();
        Map<String, ConversationDTO> conversations = new HashMap<>();

        for (ChatMessage message : messages) {
            Long fromUserId = message.getReservation().getUser().getId();
            Long toUserId = message.getReservation().getCar().getUser().getId();
            String conversationKey = generateConversationKey(fromUserId, toUserId);

            conversations.computeIfAbsent(conversationKey, k -> new ConversationDTO(fromUserId, toUserId));
            ChatMessageDTO messageDTO = MapperDTO.toChatMessageDTO(message);
            messageDTO.setFromUserId(fromUserId);
            messageDTO.setToUserId(toUserId);
            conversations.get(conversationKey).getMessages().add(messageDTO);
        }

        return new ArrayList<>(conversations.values());
    }

    public List<ChatMessageDTO> getMessagesByUser(Long userId) {
        return chatMessageRepository.findByReservationUserIdOrReservationCarUserId(userId, userId)
                .stream()
                .map(message -> {
                    ChatMessageDTO messageDTO = MapperDTO.toChatMessageDTO(message);
                    messageDTO.setFromUserId(message.getReservation().getUser().getId());
                    messageDTO.setToUserId(message.getReservation().getCar().getUser().getId());
                    return messageDTO;
                })
                .collect(Collectors.toList());
    }

    private String generateConversationKey(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }


}
