package be.iccbxl.tfe.Driveshare.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPaymentController {
    @GetMapping("/admin/finances")
    public String getFinancesPage() {
        return "admin/paiement/index";
    }

}
