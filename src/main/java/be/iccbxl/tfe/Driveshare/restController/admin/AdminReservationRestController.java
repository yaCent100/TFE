package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDTO;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AdminReservationRestController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;


    @Operation(summary = "Obtenir le nombre de réservations par localité", description = "Récupérer le nombre de réservations effectuées, groupées par localité.")
    @GetMapping("/api/reservations/count-by-locality")
    public ResponseEntity<List<Map<String, Object>>> getReservationCountByLocality(
            @Parameter(description = "L'année des réservations", required = true)
            @RequestParam int year,
            @Parameter(description = "Le mois des réservations (optionnel)") @RequestParam(required = false) Integer month) {

        List<Object[]> results;
        if (month != null) {
            results = reservationService.getReservationCountByLocality(year, month);
        } else {
            results = reservationService.getReservationCountByLocality(year);
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("locality", result[0]);
            map.put("count", result[1]);
            response.add(map);
        }

        return ResponseEntity.ok(response);
    }




    @Operation(summary = "Obtenir toutes les réservations", description = "Récupérer toutes les réservations effectuées sur la plateforme.")
    @GetMapping("/api/admin/reservations")
    public List<ReservationDTO> getAllReservations() {
        return reservationService.getAllReservationsDTOs();
    }

    @Operation(summary = "Obtenir les réservations d'un utilisateur", description = "Récupérer toutes les réservations effectuées par un utilisateur spécifique.")
    @GetMapping("/api/admin/reservations/user/{userId}")
    @ResponseBody
    public List<ReservationDTO> getReservationsByUser(
            @Parameter(description = "L'ID de l'utilisateur", required = true)
            @PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return reservationService.getReservationsByUser(user);
    }


}
