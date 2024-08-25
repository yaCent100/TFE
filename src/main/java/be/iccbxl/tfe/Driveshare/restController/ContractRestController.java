package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ContractService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@Tag(name = "Contract Management", description = "Gestion des contrats de réservation")
public class ContractRestController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private ReservationService reservationService;

    @Operation(summary = "Télécharger le contrat de réservation", description = "Permet au propriétaire de la voiture de télécharger le contrat de location.")
    @GetMapping("/download-contract/{reservationId}")
    public ResponseEntity<byte[]> downloadContract(
            @Parameter(description = "L'ID de la réservation", required = true) @PathVariable Long reservationId, Principal principal) {
        Reservation reservation = reservationService.getReservationById(reservationId);
        if (reservation == null || !reservation.getCar().getUser().getEmail().equals(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        byte[] pdfBytes = contractService.generateContractPdf(reservationId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contract_" + reservationId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}

