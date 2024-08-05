package be.iccbxl.tfe.Driveshare.restController;

import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.*;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
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



    // Ajout d'un point de terminaison GET pour le débogage
    @GetMapping("/create-payment-intent")
    public ResponseEntity<?> debugCreatePaymentIntent(@RequestParam double amount, @RequestParam String currency) {
        try {
            System.out.println("Received amount: " + amount + ", currency: " + currency);

            PaymentIntent paymentIntent = paymentService.createPaymentIntent(amount, currency);

            Map<String, Object> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());

            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Stripe exception: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("General exception: " + e.getMessage());
        }
    }
    @PostMapping("/create-payment-intent")
    public ResponseEntity<?> createPaymentIntent(@RequestBody Map<String, Object> paymentRequest) {
        try {
            System.out.println("Received paymentRequest: " + paymentRequest);

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

            // Ajout des métadonnées
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

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReservation(@RequestBody Map<String, Object> paymentConfirmation) {
        try {
            System.out.println("Received paymentConfirmation: " + paymentConfirmation);

            Long reservationId = Long.parseLong(paymentConfirmation.get("reservationId").toString());
            String paymentIntentId = paymentConfirmation.get("paymentIntentId").toString();
            String insurance = paymentConfirmation.get("insurance").toString();

            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            if ("succeeded".equals(paymentIntent.getStatus())) {
                Reservation reservation = reservationService.getReservationById(reservationId);

                if (reservation == null) {
                    return ResponseEntity.status(404).body("Reservation not found");
                }

                Car car = reservation.getCar();

                if ("manuelle".equalsIgnoreCase(car.getModeReservation())) {
                    reservation.setStatut("RESPONSE_PENDING");

                    // Envoyer un email au propriétaire
                    String ownerEmail = car.getUser().getEmail();
                    String subject = "Nouvelle demande de réservation";
                    String text = "Vous avez une nouvelle demande de réservation pour votre voiture " + car.getBrand() + " " + car.getModel() +
                            " du " + reservation.getDebutLocation() + " au " + reservation.getFinLocation() + ". Veuillez vous connecter pour confirmer ou refuser la demande.";
                    emailService.sendNotificationEmail(ownerEmail, subject, text);

                    reservationService.saveReservation(reservation);

                    // Notify the user that the reservation is pending owner's response
                    return ResponseEntity.ok("Reservation is pending owner's response. You will be notified once the owner responds, and you can proceed with the payment.");
                } else {
                    reservation.setStatut("CONFIRMED");

                    reservation.setInsurance(insurance);
                    reservationService.saveReservation(reservation);

                    Payment payment = new Payment();
                    payment.setPrixTotal(paymentIntent.getAmountReceived());
                    payment.setStatut(paymentIntent.getStatus());
                    payment.setPaiementMode(paymentIntent.getPaymentMethodTypes().get(0));
                    payment.setCreatedAt(LocalDate.now());
                    payment.setReservation(reservation);

                    paymentService.save(payment);

                    return ResponseEntity.ok("Reservation processed successfully!");
                }
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






