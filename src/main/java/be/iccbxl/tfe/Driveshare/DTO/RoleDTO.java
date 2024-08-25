package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class RoleDTO {
    private Long id;
    private String name;

    public RoleDTO(Long id, String role) {
        this.id=id;
        this.name=role;
    }
}
