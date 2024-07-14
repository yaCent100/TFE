package be.iccbxl.tfe.Driveshare.controller;

import be.iccbxl.tfe.Driveshare.DTO.CarMapper;
import be.iccbxl.tfe.Driveshare.DTO.ChatMessageDTO;
import be.iccbxl.tfe.Driveshare.model.ChatMessage;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ChatMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class ChatMessageRestController {

    @Autowired
    private ChatMessageService chatMessageService;

    @GetMapping("/reservation/{reservationId}")
    public List<ChatMessageDTO> getMessagesByReservation(@PathVariable Long reservationId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByReservationId(reservationId);
        return messages.stream()
                .map(CarMapper::toChatMessageDTO)
                .collect(Collectors.toList());
    }
}