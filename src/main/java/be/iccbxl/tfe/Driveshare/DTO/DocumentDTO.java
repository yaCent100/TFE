package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class DocumentDTO {
    private Long id;
    private Long userId;
    private String documentType;
    private String url;
    private String userPhotoUrl;
    private String userNom;
    private String userPrenom;
}