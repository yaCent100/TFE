package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Management", description = "Gestion des paiements et transactions")
public class PaymentRestController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private Gson gson;

    private static final Logger logger = LoggerFactory.getLogger(PaymentRestController.class);

    @Operation(summary = "Créer une intention de paiement", description = "Crée une intention de paiement avec le montant spécifié.")
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(
            @Parameter(description = "Les détails du paiement", required = true) @RequestBody Map<String, Object> paymentRequest) {
        try {
            if (paymentRequest.get("amount") == null || paymentRequest.get("currency") == null) {
                return ResponseEntity.status(400).body("Invalid request: amount or currency is missing.");
            }

            String amountString = paymentRequest.get("amount").toString();
            String currency = paymentRequest.get("currency").toString();
            Long reservationId = Long.parseLong(paymentRequest.get("reservationId").toString());

            double amount;
            try {
                amount = Double.parseDouble(amountString);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(400).body("Invalid amount format.");
            }

            PaymentIntent paymentIntent = paymentService.createPaymentIntent(amount, currency);
            paymentIntent.update(Map.of("metadata", Map.of("reservationId", reservationId.toString())));

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            response.put("paymentIntentId", paymentIntent.getId());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Stripe exception: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("General exception: " + e.getMessage());
        }
    }

    @Operation(summary = "Confirmer une réservation", description = "Confirme la réservation après la réussite du paiement.")
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReservation(
            @Parameter(description = "Les informations de confirmation de la réservation", required = true) @RequestBody Map<String, Object> paymentConfirmation) {
        try {
            Long reservationId = Long.parseLong(paymentConfirmation.get("reservationId").toString());
            String paymentIntentId = paymentConfirmation.get("paymentIntentId").toString();
            String insurance = paymentConfirmation.get("insurance").toString();

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                Reservation reservation = reservationService.getReservationById(reservationId);
                if (reservation == null) {
                    return ResponseEntity.status(404).body("Reservation not found");
                }

                reservation.setStatut("CONFIRMED");
                reservation.setInsurance(insurance);
                reservationService.saveReservation(reservation);

                Payment payment = new Payment();
                BigDecimal amountReceived = BigDecimal.valueOf(paymentIntent.getAmountReceived());
                BigDecimal driveShareAmount = amountReceived.multiply(BigDecimal.valueOf(0.3)).setScale(2, RoundingMode.HALF_UP);

                payment.setPrixTotal(amountReceived.doubleValue());
                payment.setStatut(paymentIntent.getStatus());
                payment.setPaiementMode(paymentIntent.getCharges().getData().get(0).getPaymentMethodDetails().getType());
                payment.setPartDriveShare(driveShareAmount.doubleValue());
                payment.setCreatedAt(LocalDateTime.now());
                payment.setReservation(reservation);

                paymentService.save(payment);

                return ResponseEntity.ok("Reservation processed successfully!");
            } else {
                return ResponseEntity.status(400).body("Payment failed, reservation not confirmed.");
            }
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Stripe exception: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("General exception: " + e.getMessage());
        }
    }
}







