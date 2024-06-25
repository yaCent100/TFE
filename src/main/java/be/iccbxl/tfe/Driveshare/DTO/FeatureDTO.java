package be.iccbxl.tfe.Driveshare.DTO;

import jakarta.persistence.Entity;
import lombok.Data;

@Data
public class FeatureDTO {
    private Long id;
    private String name;

    public FeatureDTO(Long id, String name) {
        this.id = id;
        this.name = name;
    }

}