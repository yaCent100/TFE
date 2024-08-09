package be.iccbxl.tfe.Driveshare.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminEvaluationController {

    @GetMapping("/evaluations")
    public String showEvaluationsPage() {

        return "admin/evaluation/index";
    }
}
