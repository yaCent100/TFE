package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.FinancialStatsDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.DTO.PaymentDTO;
import be.iccbxl.tfe.Driveshare.config.StripeConfig;
import be.iccbxl.tfe.Driveshare.model.Payment;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.PaymentRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import com.google.gson.Gson;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    public List<Payment> getPaymentsForUser(User user, LocalDateTime startDate, LocalDateTime endDate) {

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


    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id " + paymentId));
    }



    public BigDecimal getTotalBenefit() {
        return paymentRepository.sumTotalBenefit(); // Exemple : requête pour obtenir le total des bénéfices
    }



    public int getTotalTransactions() {
        // Logique pour calculer le nombre total de transactions
        return paymentRepository.findAll().size();
    }

    public double getCancellationPercentage() {
        // Logique pour calculer le pourcentage d'annulations
        int totalTransactions = getTotalTransactions();
        int cancelledTransactions = reservationRepository.findByStatut("CANCELLED").size();
        return totalTransactions > 0 ? (double) cancelledTransactions / totalTransactions * 100 : 0;
    }

    public double getTotalRefunds() {
        // Logique pour calculer le montant total des remboursements
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getRefund() != null) // Filtrer les paiements qui ont un remboursement
                .mapToDouble(payment -> payment.getRefund().getAmount()) // Accéder au montant du remboursement
                .sum();
    }


    public double getUserGeneratedRevenue() {
        // Logique pour calculer le montant total généré par les utilisateurs
        return paymentRepository.findAll()
                .stream()
                .filter(payment -> payment.getStatut().equals("succeeded"))
                .filter(payment -> payment.getGain() != null) // Vérifiez si le gain est non nul
                .mapToDouble(payment -> payment.getGain().getMontantGain()) // Accédez au montant du gain seulement si non nul
                .sum();
    }


    public FinancialStatsDTO getFinancialStats() {
        // Récupérer les entités Payment à partir du service
        List<PaymentDTO> payments = getAllPayments(); // Retourne directement les PaymentDTO

        Map<String, Double> benefitByMonth = new HashMap<>();
        Map<String, Double> refundsByMonth = new HashMap<>();
        Map<String, Double> userGeneratedRevenueByMonth = new HashMap<>();

        // Liste des mois de l'année au format YYYY-MM
        String[] months = {
                "2024-01", "2024-02", "2024-03", "2024-04", "2024-05",
                "2024-06", "2024-07", "2024-08", "2024-09", "2024-10",
                "2024-11", "2024-12"
        };

        // Parcourir les PaymentDTO et effectuer les calculs
        for (PaymentDTO paymentDTO : payments) {
            // Regrouper par mois (format YYYY-MM)
            String month = paymentDTO.getCreatedAt().toLocalDate().withDayOfMonth(1).toString().substring(0, 7);

            // Montant du remboursement
            double refundAmount = (paymentDTO.getRefundDTO() != null) ? paymentDTO.getRefundDTO().getAmount() : 0;

            // Bénéfice : Chiffre d'affaires - Remboursements
            double dailyBenefit = paymentDTO.getPrixTotal() - refundAmount;
            benefitByMonth.put(month, benefitByMonth.getOrDefault(month, 0.0) + dailyBenefit);

            // Remboursements
            refundsByMonth.put(month, refundsByMonth.getOrDefault(month, 0.0) + refundAmount);

            // Gain généré par les utilisateurs
            if (paymentDTO.getGainDTO() != null) {
                userGeneratedRevenueByMonth.put(month, userGeneratedRevenueByMonth.getOrDefault(month, 0.0) + paymentDTO.getGainDTO().getMontantGain());
            }
        }

        // Assurer que chaque mois est présent dans les résultats
        for (String month : months) {
            benefitByMonth.putIfAbsent(month, 0.0);
            refundsByMonth.putIfAbsent(month, 0.0);
            userGeneratedRevenueByMonth.putIfAbsent(month, 0.0);
        }

        return new FinancialStatsDTO(benefitByMonth, refundsByMonth, userGeneratedRevenueByMonth);
    }








}



