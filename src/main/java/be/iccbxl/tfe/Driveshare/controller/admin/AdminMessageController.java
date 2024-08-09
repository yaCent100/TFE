package be.iccbxl.tfe.Driveshare.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminMessageController {

    @GetMapping("/admin/messages")
    public String adminInterface(){
        return "admin/messages/index";
    }


}
