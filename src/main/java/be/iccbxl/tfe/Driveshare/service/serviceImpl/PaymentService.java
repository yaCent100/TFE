package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Payment;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

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
    public void save(Payment payment) {
        paymentRepository.save(payment);
    }
}
