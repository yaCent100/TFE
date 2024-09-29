package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

@Data
public class FeatureDTO {
    private Long id;
    private String name;
    private String description;
    private String iconUrl;


    public FeatureDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description= description;
        this.iconUrl = getIconForFeature(name); // Associer l'icône ici
    }

    public FeatureDTO() {

    }

    public static String getIconForFeature(String featureName) {
        switch (featureName.toLowerCase()) {
            case "boite":
                return "/icons/boite-de-vitesses.png";
            case "portes":
                return "/icons/porte-de-voiture.png";
            case "compteur":
                return "/icons/jauge.png";
            case "moteur":
                return "/icons/station-essence.png";
            case "places":
                return "/icons/siege-de-voiture.png";
            default:
                return "/icons/default.png";
        }
    }

}