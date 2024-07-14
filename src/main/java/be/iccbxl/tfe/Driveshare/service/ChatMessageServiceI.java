package be.iccbxl.tfe.Driveshare.service;

import be.iccbxl.tfe.Driveshare.model.ChatMessage;


import java.util.List;

public interface ChatMessageServiceI {

    ChatMessage save(ChatMessage chatMessage);
    List<ChatMessage> getMessagesByReservationId(Long reservationId);

}
