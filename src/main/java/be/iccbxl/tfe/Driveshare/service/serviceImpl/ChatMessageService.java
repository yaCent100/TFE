package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.*;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
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
    public List<ChatMessageDTO> getMessagesByReservationId(Long reservationId) {
        List<ChatMessage> messages = chatMessageRepository.findByReservationId(reservationId);

        // Convertissez chaque message en DTO
        return messages.stream()
                .map(message -> MapperDTO.toChatMessageDTO(message, message.getFromUserId())) // Utilisez fromUserId pour identifier correctement l'expéditeur
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userService.getUserById(id);
    }

    public List<ChatMessageDTO> getMessagesByReservationId(Long reservationId, Long currentUserId) {
        List<ChatMessage> messages = chatMessageRepository.findByReservationId(reservationId);

        // Convertir les messages en DTO tout en passant l'utilisateur actuel pour déterminer qui est l'expéditeur
        return messages.stream()
                .map(message -> MapperDTO.toChatMessageDTO(message, currentUserId))
                .collect(Collectors.toList());
    }





    public List<ConversationDTO> getAllConversations() {
        List<ChatMessage> messages = chatMessageRepository.findAll(); // Récupérer tous les messages
        Map<String, ConversationDTO> conversations = new HashMap<>();

        for (ChatMessage message : messages) {
            Long fromUserId = message.getReservation().getUser().getId();
            Long toUserId = message.getReservation().getCar().getUser().getId();

            // Générer une clé unique pour chaque conversation
            String conversationKey = generateConversationKey(fromUserId, toUserId);

            // Initialiser la conversation si elle n'existe pas encore
            conversations.computeIfAbsent(conversationKey, k -> new ConversationDTO(fromUserId, toUserId));

            // Ajouter le message à la conversation
            ChatMessageDTO messageDTO = MapperDTO.toChatMessageDTO(message, null); // Null car on ne se base pas sur l'utilisateur courant
            conversations.get(conversationKey).getMessages().add(messageDTO);
        }

        return new ArrayList<>(conversations.values());
    }



    public List<ChatMessageDTO> getMessagesByUser(Long userId) {
        return chatMessageRepository.findByReservationUserIdOrReservationCarUserId(userId, userId)
                .stream()
                .map(message -> {
                    // Passez userId comme second paramètre à toChatMessageDTO
                    ChatMessageDTO messageDTO = MapperDTO.toChatMessageDTO(message, userId);

                    // La logique pour identifier l'expéditeur et le destinataire est déjà gérée dans toChatMessageDTO
                    return messageDTO;
                })
                .collect(Collectors.toList());
    }

    private String generateConversationKey(Long userId1, Long userId2) {
        return userId1 < userId2 ? userId1 + "_" + userId2 : userId2 + "_" + userId1;
    }

    // Méthode pour obtenir les utilisateurs avec des messages
    public List<User> getAllUsersWithMessages() {
        List<ChatMessage> messages = chatMessageRepository.findAll();
        return messages.stream()
                .map(message -> message.getReservation().getUser()) // Accéder à l'utilisateur via la réservation
                .distinct()
                .collect(Collectors.toList());
    }


    public Map<String, Long> getMessagesByDay() {
        return chatMessageRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        message -> message.getSentAt().toLocalDate().toString(),
                        Collectors.counting()
                ));
    }
}
