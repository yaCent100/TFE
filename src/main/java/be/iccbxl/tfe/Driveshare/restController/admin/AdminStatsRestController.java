package be.iccbxl.tfe.Driveshare.restController.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminStatsRestController {

 /*   @Autowired
    private UserService userService

    @GetMapping("/users/count")
    public long getTotalUsers() {
        // Logique pour obtenir le nombre total d'utilisateurs
        return userService.getTotalUsers();
    }

    @GetMapping("/users/new")
    public long getNewUsersThisMonth() {
        // Logique pour obtenir le nombre de nouveaux utilisateurs ce mois-ci
        return userService.getNewUsersThisMonth();
    }

    @GetMapping("/revenue")
    public double getMonthlyRevenue() {
        // Logique pour obtenir le revenu généré ce mois-ci
        return revenueService.getMonthlyRevenue();
    }

    @GetMapping("/users/growth")
    public List<Integer> getUserGrowth() {
        // Logique pour obtenir la croissance des utilisateurs par mois
        return userService.getUserGrowth();
    }

    @GetMapping("/revenue/history")
    public List<Double> getRevenueHistory() {
        // Logique pour obtenir l'historique des revenus par mois
        return revenueService.getRevenueHistory();
    }*/
}
