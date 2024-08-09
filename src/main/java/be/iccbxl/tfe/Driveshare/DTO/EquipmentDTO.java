package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class EquipmentDTO {

    private Long id;
    private String icon;
    private String equipment;

    public EquipmentDTO(Long id, String icone, String equipment) {
    }

    public EquipmentDTO() {

    }
}
