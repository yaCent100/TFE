package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationDTO {

    private Long id;
    private Integer note;
    private String avis;
    private Long reservationId; // pour stocker l'ID de la réservation associée
    private Long carId; // pour stocker l'ID de la voiture associée

    private String carPhotoUrl;
    private String carBrand;
    private String carModel;
    private String userPrenom;
    private String userNom;
    private String userProfilePhotoUrl;
    private LocalDateTime createdAt;

}
