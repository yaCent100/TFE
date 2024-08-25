package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.model.Gain;
import be.iccbxl.tfe.Driveshare.repository.GainRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GainService {

    @Autowired
    private GainRepository gainRepository;

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
        List<Gain> pendingGains = gainRepository.findByStatutAndDateGainBefore("EN_ATTENTE_DE_VERSEMENT", now.minusDays(3));

        for (Gain gain : pendingGains) {
            // Mise à jour du statut à "PAYE"
            gain.setStatut("PAYE");
            gainRepository.save(gain);

            // Envoyer une notification au propriétaire
            String ownerEmail = gain.getPayment().getReservation().getCar().getUser().getEmail();
            String subject = "Votre paiement a été effectué";
            String text = "Votre gain de " + gain.getMontantGain() + "€ a été versé sur votre compte IBAN: " + gain.getPayment().getReservation().getCar().getUser().getIban();
            emailService.sendNotificationEmail(ownerEmail, subject, text);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Exécution quotidienne à minuit
    public void checkPendingGains() {
        processPendingGains();
    }
}
