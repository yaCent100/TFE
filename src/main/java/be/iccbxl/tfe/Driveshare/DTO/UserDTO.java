package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.util.List;

@Data
public class UserDTO {

    private Long id;
    private String nom;

    private String prenom;
    private List<CarDTO> ownedCars;
}
