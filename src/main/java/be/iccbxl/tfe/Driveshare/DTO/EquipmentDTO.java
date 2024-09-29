package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class EquipmentDTO {

    private Long id;
    private String icon;
    private String equipment;



    public EquipmentDTO() {

    }

    public EquipmentDTO(Long id, String icon, String equipment) {
        this.id = id;
        this.icon = icon;
        this.equipment = equipment;
    }

}
