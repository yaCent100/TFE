package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class GainDTO {

    private Long id;
    private Long paymentId;
    private double montantGain;
    private LocalDateTime dateGain;
    private String statut;
    private String description;


    private String carBrand;
    private String carModel;
    private String carImage; // Ajouter cette ligne
    private LocalDate debutLocation;
    private LocalDate finLocation;
}
