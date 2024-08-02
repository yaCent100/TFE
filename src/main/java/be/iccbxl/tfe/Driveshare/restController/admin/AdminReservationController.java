package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class AdminReservationController {

    @Autowired
    private ReservationService reservationService;

    @GetMapping("/count-by-locality")
    public ResponseEntity<List<Map<String, Object>>> getReservationCountByLocality(
            @RequestParam int year,
            @RequestParam(required = false) Integer month) {

        List<Object[]> results;
        if (month != null) {
            results = reservationService.getReservationCountByLocality(year, month);
            System.out.println("Fetching reservations for year " + year + " and month " + month);
        } else {
            results = reservationService.getReservationCountByLocality(year);
            System.out.println("Fetching reservations for year " + year);
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("locality", result[0]);
            map.put("count", result[1]);
            response.add(map);
        }

        System.out.println("Results: " + response);
        return ResponseEntity.ok(response);
    }

   /* @GetMapping("/status")
    public Map<String, Object> getReservationsByStatus() {
        Map<String, Object> response = new HashMap<>();
        List<Reservation> accepted = reservationService.findByStatus("accepted");
        List<Reservation> pending = reservationService.findByStatus("pending");
        List<Reservation> rejected = reservationService.findByStatus("rejected");

        //double acceptanceRate = reservationService.calculateAcceptanceRate();

        response.put("accepted", accepted);
        response.put("pending", pending);
        response.put("rejected", rejected);
        //response.put("acceptanceRate", acceptanceRate);

        return response;
    }*/


}
