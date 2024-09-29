package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Gain;
import be.iccbxl.tfe.Driveshare.model.Payment;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.repository.GainRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.List;

@Service
public class GainService {

    @Autowired
    private GainRepository gainRepository;
    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private EmailService emailService;

    public List<Gain> getGainsForUser(Long userId) {
        return gainRepository.findGainsByUserId(userId);
    }


    public void saveGain(Gain gain) {
        gainRepository.save(gain);
    }

    public List<Gain> getGainsForUserByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        return gainRepository.findByUserIdAndDateRange(userId, startDate, endDate);
    }

    public void processPendingGains() {
        LocalDateTime now = LocalDateTime.now();

        // Trouver les réservations terminées depuis plus de 3 jours avec le statut "FINISHED"
        List<Reservation> reservations = reservationRepository.findByStatutAndEndLocationBefore("FINISHED", now.minusDays(3));

        for (Reservation reservation : reservations) {
            // Vérifier qu'aucun gain n'a déjà été créé pour cette réservation
            Gain existingGain = gainRepository.findByPaymentReservation(reservation);

            if (existingGain == null) {
                // Calculer le montant du gain à partir des informations du paiement
                Payment payment = reservation.getPayment();
                double montantGain = calculerMontantGain(payment);

                // Créer un nouveau gain
                Gain gain = new Gain();
                gain.setPayment(payment);
                gain.setMontantGain(montantGain);
                gain.setStatut("versé");
                gain.setDateGain(LocalDateTime.now());
                gainRepository.save(gain);

                // Envoyer une notification au propriétaire
                String ownerEmail = reservation.getCar().getUser().getEmail();
                String subject = "Un nouveau gain a été créé";
                String text = "Votre gain de " + montantGain + "€ a été créé pour la réservation terminée.";
                emailService.sendNotificationEmail(ownerEmail, subject, text);
            }
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Exécution quotidienne à minuit
    public void checkPendingGains() {
        processPendingGains();
    }

    public double calculerMontantGain(Payment payment) {
        // Montant total payé par le client
        double totalMontant = payment.getPrixTotal();

        // Part de DriveShare (commission) que tu veux soustraire
        double partDriveShare = payment.getPartDriveShare();

        return totalMontant-partDriveShare;
    }



}
