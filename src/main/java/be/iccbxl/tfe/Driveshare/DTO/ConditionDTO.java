package be.iccbxl.tfe.Driveshare.DTO;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class ConditionDTO {

    private Long id;

    private String condition;

    public ConditionDTO(Long id, String condition) {
    }
    public ConditionDTO() {
    }
}
