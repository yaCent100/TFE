package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDataDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PaymentService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AdminStatController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserService userService;



    @GetMapping("/api/revenues/total-revenue")
    public BigDecimal getTotalRevenue() {
        return paymentService.getTotalRevenue();
    }

    @GetMapping("/api/revenues/monthly-revenue")
    public BigDecimal getMonthlyRevenue(@RequestParam int month, @RequestParam int year) {
        return paymentService.getMonthlyRevenue(month, year);
    }

    @GetMapping("/api/revenues/revenue-by-month")
    public List<Object[]> getRevenueByMonth() {
        return paymentService.getRevenueByMonth();

    }

    @GetMapping("/api/stats/registrations")
    public ResponseEntity<?> getUserRegistrationsByMonth() {
        List<ReservationDataDTO> registrationData = userService.getRegistrationsByMonth();
        return ResponseEntity.ok(registrationData);
    }

    @GetMapping("/api/stats/geolocation")
    public ResponseEntity<List<ReservationDataDTO>> getUserGeolocationStats() {
        List<ReservationDataDTO> stats = userService.getUsersGroupedByLocality();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/api/stats/user-kpi")
    public ResponseEntity<Map<String, Object>> getUserKpi() {
        Map<String, Object> kpiData = new HashMap<>();

        // Total des utilisateurs
        int totalUsers = userService.countTotalUsers();
        kpiData.put("totalUsers", totalUsers);

        // Nouvelle inscriptions (exemple pour les 30 derniers jours)
        int newRegistrations = userService.countNewRegistrations();
        kpiData.put("newRegistrations", newRegistrations);

        // Utilisateurs en ligne
        int usersOnline = userService.countConnectedUsers();  // Utilisation du service pour les utilisateurs connectés
        kpiData.put("usersOnline", usersOnline);

        // Utilisateurs ayant inscrit une voiture
        int usersWithCars = userService.countUsersWithRegisteredCars();
        kpiData.put("usersWithCars", usersWithCars);

        return ResponseEntity.ok(kpiData);
    }



}
