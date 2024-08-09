package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.PaymentDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PaymentService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReportService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportService reportService;

    @GetMapping
    public List<PaymentDTO> getAllPayments() {
        return paymentService.getAllPayments();
    }
    @DeleteMapping("/{id}")
    public void deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
    }

    @PutMapping("/{id}")
    public void updatePaymentStatus(@PathVariable Long id, @RequestBody PaymentDTO paymentDTO) {
        paymentService.updatePaymentStatus(id, paymentDTO);
    }

    @GetMapping("/report")
    public ResponseEntity<InputStreamResource> getPaymentReport() {
        List<PaymentDTO> payments = paymentService.getAllPayments();
        ByteArrayInputStream bis = reportService.generatePaymentReport(payments);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=payment_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }



}
