package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class FeatureDTO {
    private Long id;
    private String name;
    private String description;

    public FeatureDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description= description;
    }

}