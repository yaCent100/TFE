package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.service.serviceImpl.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/revenues")
public class RevenueRestController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/total-revenue")
    public BigDecimal getTotalRevenue() {
        return paymentService.getTotalRevenue();
    }

    @GetMapping("/monthly-revenue")
    public BigDecimal getMonthlyRevenue(@RequestParam int month, @RequestParam int year) {
        return paymentService.getMonthlyRevenue(month, year);
    }

    @GetMapping("/revenue-by-month")
    public List<Object[]> getRevenueByMonth() {
        return paymentService.getRevenueByMonth();
    }

}
