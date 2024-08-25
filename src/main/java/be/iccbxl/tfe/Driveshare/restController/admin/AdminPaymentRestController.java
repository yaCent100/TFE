package be.iccbxl.tfe.Driveshare.restController.admin;

import be.iccbxl.tfe.Driveshare.DTO.PaymentDTO;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.PaymentService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Payment Management", description = "API pour la gestion des paiements par les administrateurs")
public class AdminPaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReportService reportService;

    @Operation(summary = "Obtenir tous les paiements", description = "Récupérer la liste de tous les paiements effectués sur la plateforme.")
    @GetMapping
    public List<PaymentDTO> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @Operation(summary = "Supprimer un paiement", description = "Supprimer un paiement spécifique par son ID.")
    @DeleteMapping("/{id}")
    public void deletePayment(
            @Parameter(description = "L'ID du paiement à supprimer", required = true)
            @PathVariable Long id) {
        paymentService.deletePayment(id);
    }

    @Operation(summary = "Mettre à jour le statut d'un paiement", description = "Mettre à jour le statut d'un paiement spécifique.")
    @PutMapping("/{id}")
    public void updatePaymentStatus(
            @Parameter(description = "L'ID du paiement à mettre à jour", required = true)
            @PathVariable Long id,
            @RequestBody PaymentDTO paymentDTO) {
        paymentService.updatePaymentStatus(id, paymentDTO);
    }

    @Operation(summary = "Obtenir un rapport de paiements", description = "Générer et récupérer un rapport PDF contenant tous les paiements.")
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

