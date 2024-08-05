package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class CategoryDTO {

    private Long id;
    private String category;


    public CategoryDTO(Long id, String category) {
    }
}
