package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class EvaluationDTO {

    private Long id;
    private Integer note;
    private String avis;
    private Long reservationId; // pour stocker l'ID de la réservation associée

}
