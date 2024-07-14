package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.ChatMessageRepository;
import be.iccbxl.tfe.Driveshare.service.ChatMessageServiceI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
