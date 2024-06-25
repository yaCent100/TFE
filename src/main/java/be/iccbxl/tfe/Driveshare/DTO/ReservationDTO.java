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
        if (statut == null) {
            return "white"; // couleur par défaut si statut est null
        }
        switch (statut.toLowerCase()) {
            case "confirmé":
                return "#28a745"; // Vert
            case "en attente":
                return "#ffc107"; // Jaune
            case "annulée":
                return "#dc3545"; // Rouge
            default:
                return "white"; // blanc par défaut pour tous les autres cas
        }
    }
}


