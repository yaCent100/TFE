package be.iccbxl.tfe.Driveshare.DTO;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ReservationDataDTO {
    private String locality;
    private int count;

    // Constructeur
    public ReservationDataDTO(String locality, int count) {
        this.locality = locality;
        this.count = count;
    }


    public static ReservationDataDTO toDTO(Map<String, Object> tuple) {
        String locality = (String) tuple.get("locality");
        int count = ((Number) tuple.get("count")).intValue();

        return new ReservationDataDTO(locality, count);
    }

    // Mapper une liste de tuples en DTOs
    public static List<ReservationDataDTO> toDTOList(List<Map<String, Object>> tuples) {
        return tuples.stream()
                .map(ReservationDataDTO::toDTO)
                .collect(Collectors.toList());
    }
}

