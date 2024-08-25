package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.util.List;

@Data
public class CategoryDTO {

    private Long id;
    private String category;
    private List<CarDTO> cars;  // Contient uniquement les données pertinentes des voitures


    public CategoryDTO() {

    }

    public CategoryDTO(Long id, String category) {
    }
}
