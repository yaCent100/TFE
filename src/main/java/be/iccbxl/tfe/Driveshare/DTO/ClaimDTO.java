package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClaimDTO {

    private Long id;
    private String message;
    private Long reservationId;
    private String claimantRole;
    private String status;
    private LocalDateTime createdAt;  // Date de création de la réclamation
    private LocalDateTime responseAt;  // Date à laquelle une réponse a été ajoutée
    private String response;  // Champ pour stocker la réponse de l'administrateur


}
