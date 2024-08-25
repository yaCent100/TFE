package be.iccbxl.tfe.Driveshare.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminClaimController {

    @GetMapping("/admin/claims")
    public String getFinancesPage() {
        return "admin/claims/index";
    }
}
