package be.iccbxl.tfe.Driveshare.DTO;

import be.iccbxl.tfe.Driveshare.model.Car;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservationDTO {

    private Long id;
    private Long carId;
    private String debutLocation;
    private String finLocation;
    private LocalDateTime createdAt;
    private String carName;
    private String userName;
    private String statut;
    private int nbJours;
    private String carBrand;
    private String carModel;
    private String userProfileImage;
    private String carImage;
    private String carPostal;
    private String carLocality;
    private String insurance;
    private String modeReservation;

    public Boolean isUserVerified;

    // Ajouter le champ PaymentDTO
    private PaymentDTO payment;



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


