package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.CarDTO;
import be.iccbxl.tfe.Driveshare.DTO.CarReservationKpiDTO;
import be.iccbxl.tfe.Driveshare.DTO.DashboardKpiDTO;
import be.iccbxl.tfe.Driveshare.DTO.UserReservationKpiDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PaymentService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class AdminDashboardKpiRestController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final ReservationService reservationService;

    @Autowired
    public AdminDashboardKpiRestController(PaymentService paymentService, UserService userService, ReservationService reservationService) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.reservationService = reservationService;
    }

    @GetMapping("/kpi")
    public DashboardKpiDTO getDashboardKpi() {
        DashboardKpiDTO kpi = new DashboardKpiDTO();

        // Récupérer le revenu total
        BigDecimal totalRevenue = paymentService.getTotalRevenue();
        kpi.setTotalRevenue(totalRevenue);

        // Récupérer le bénéfice total
        BigDecimal totalBenefit = paymentService.getTotalBenefit();  // Cette méthode doit être implémentée dans votre service
        kpi.setTotalBenefit(totalBenefit);

        // Récupérer le nombre total d'utilisateurs
        long totalUsers = userService.getTotalUsers();  // Cette méthode doit être implémentée dans votre service
        kpi.setTotalUsers(totalUsers);

        // Récupérer le nombre total de réservations
        long totalReservations = reservationService.getTotalConfirmedReservations();  // Cette méthode doit être implémentée dans votre service
        kpi.setTotalReservations(totalReservations);

        return kpi;
    }

    @GetMapping("/top10-cars")
    public List<CarReservationKpiDTO> getTop10CarsThisMonth() {
        return reservationService.getTop10MostReservedCars();
    }

    @GetMapping("/top10-users")
    public List<UserReservationKpiDTO> getTop10UsersByReservations() {
        return reservationService.getTop10UsersByReservationsForCurrentMonth();
    }
}
