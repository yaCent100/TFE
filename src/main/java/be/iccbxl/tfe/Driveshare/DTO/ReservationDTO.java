package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ReservationDTO {

    private Long id;
    private LocalDate debutLocation;
    private LocalDate finLocation;
    private LocalDateTime createdAt;
    private String carName;
    private String userName;
    private String statut;
    private int nbJours;



    public String getTitle() {
        return "Réservation " + statut;
    }

    public String getBackgroundColor() {
        if (statut.equals("confirmé")) {
            return "#28a745"; // Vert
        } else if (statut.equals("en attente")) {
            return "#ffc107"; // Jaune
        } else if (statut.equals("annulée")) {
            return "#dc3545"; // Rouge
        }
        return "white"; // blanc par défaut
    }
}


