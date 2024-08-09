package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ConversationDTO {
    private Long user1Id;
    private Long user2Id;
    private List<ChatMessageDTO> messages = new ArrayList<>();

    public ConversationDTO(Long user1Id, Long user2Id) {
        this.user1Id = user1Id;
        this.user2Id = user2Id;
    }

}