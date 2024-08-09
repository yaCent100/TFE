package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.PaymentDTO;
import be.iccbxl.tfe.Driveshare.config.StripeConfig;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Payment;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.PaymentRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private Gson gson;

    @Autowired
    private StripeConfig stripeConfig;

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public List<PaymentDTO> getAllPayments() {
        List<Payment> payments = paymentRepository.findAll();
        return payments.stream().map(MapperDTO::toPaymentDTO).collect(Collectors.toList());
    }
    public List<Payment> getPaymentsForUser(User user, LocalDate startDate, LocalDate endDate) {
        System.out.println("Recherche de paiements pour l'utilisateur : " + user.getEmail());
        System.out.println("Date de début : " + startDate);
        System.out.println("Date de fin : " + endDate);

        List<Payment> payments = paymentRepository.findByUserAndCreatedAtBetween(user, startDate, endDate);

        if (payments.isEmpty()) {
            System.out.println("Aucun paiement trouvé.");
        } else {
            System.out.println("Paiements trouvés : " + payments.size());
            for (Payment payment : payments) {
                System.out.println(payment.toString());
            }
        }

        return payments;
    }
    public Payment save(Payment payment) {
        logger.debug("Saving payment: {}", payment);
        return paymentRepository.save(payment);
    }

    public PaymentIntent createPaymentIntent(double amount, String currency) throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) amount)
                .setCurrency(currency)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                )
                .build();

        logger.debug("Creating PaymentIntent with amount: {} and currency: {}", amount, currency);
        return PaymentIntent.create(params);
    }




    // Obtenir le revenu total
    public BigDecimal getTotalRevenue() {
        return paymentRepository.calculateTotalRevenue();
    }

    // Obtenir le revenu par mois et année
    public BigDecimal getMonthlyRevenue(int month, int year) {
        return paymentRepository.calculateRevenueForMonth(month, year);
    }

    // Obtenir les revenus par mois pour une vue historique
    public List<Object[]> getRevenueByMonth() {
        return paymentRepository.getRevenuePerMonth();
    }


    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }

    public void updatePaymentStatus(Long id, PaymentDTO paymentDTO) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        payment.setStatut(paymentDTO.getStatut());
        paymentRepository.save(payment);
    }
}



