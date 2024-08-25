package be.iccbxl.tfe.Driveshare.classes;

import be.iccbxl.tfe.Driveshare.model.*;
import be.iccbxl.tfe.Driveshare.repository.*;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ClaimService;
import be.iccbxl.tfe.Driveshare.service.serviceImpl.ReservationService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;

@Component
public class GenerationData {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private CarRepository carRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private GainRepository gainRepository;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ClaimService claimService;

   /* @PostConstruct
    public void generateReservationsAfterStartup() {
        // Appeler la méthode pour générer les réservations
        generateReservationsWithPaymentsAndGainsFor2024(100);  // Par exemple, générer 100 réservations
    }*/

    // Méthode pour insérer des données lors du démarrage de l'application
   /* @PostConstruct
    public void initData() {
        Random random = new Random();
        String[] roles = {"LOCATAIRE", "PROPRIETAIRE"};
        String[] messages = {
                "Problème de réservation", "Demande d'annulation",
                "Erreur de paiement", "Problème de communication",
                "Erreur système", "Retard de confirmation",
                "Mauvaise expérience", "Besoin de support", "Autre"
        };
        String[] adminResponses = {
                "Problème résolu.", "Annulation acceptée.",
                "Paiement corrigé.", "Communication améliorée.",
                "Problème technique résolu.", "Confirmation envoyée.",
                "Support ajouté.", "Expérience client améliorée."
        };

        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();

        // Récupérer toutes les réservations disponibles dans la base de données
        List<Reservation> reservations = reservationService.getAllReservations();

        for (int i = 0; i < 50; i++) {
            Claim claim = new Claim();

            // Générer une date aléatoire entre le 1er janvier 2024 et maintenant
            long days = random.nextInt((int) startDate.until(endDate, java.time.temporal.ChronoUnit.DAYS));
            LocalDateTime randomDate = startDate.plusDays(days);

            claim.setCreatedAt(randomDate);
            claim.setClaimantRole(roles[random.nextInt(roles.length)]);
            claim.setMessage(messages[random.nextInt(messages.length)]);
            claim.setStatus("Résolue");  // Toutes les réclamations sont résolues

            // Sélectionner une réservation aléatoire parmi celles récupérées
            Reservation randomReservation = reservations.get(random.nextInt(reservations.size()));
            claim.setReservation(randomReservation);

            // Ajouter une réponse de l'administrateur
            claim.setResponse(adminResponses[random.nextInt(adminResponses.length)]);
            claim.setResponseAt(randomDate.plusDays(random.nextInt(10)));  // Ajouter une réponse après quelques jours

            // Enregistrer la réclamation
            claimService.save(claim);
        }
    }*/

    @Transactional
    public void generateReservationsWithPaymentsAndGainsFor2024(int numberOfReservations) {
        Random random = new Random();
        List<Car> cars = carRepository.findAll();
        List<User> users = (List<User>) userRepository.findAll();

        if (cars.isEmpty() || users.isEmpty()) {
            throw new IllegalStateException("No cars or users found in the database.");
        }

        LocalDate startDate = LocalDate.of(2024, 1, 1);  // Date de début fixée au 1er janvier 2024
        LocalDate now = LocalDate.now();  // Date actuelle

        for (int i = 0; i < numberOfReservations; i++) {
            // Générer une date aléatoire entre le 1er janvier 2024 et aujourd'hui
            long daysBetween = ChronoUnit.DAYS.between(startDate, now);
            LocalDate randomDate = startDate.plusDays(random.nextInt((int) daysBetween + 1));

            // Choisir une voiture aléatoire et un utilisateur aléatoire
            Car randomCar = cars.get(random.nextInt(cars.size()));
            User randomUser = users.get(random.nextInt(users.size()));

            // Créer une nouvelle réservation
            Reservation reservation = new Reservation();
            reservation.setCar(randomCar);
            reservation.setStatut("CONFIRMED");
            reservation.setStartLocation(randomDate);
            reservation.setEndLocation(randomDate.plusDays(random.nextInt(15) + 1));  // Durée de location de 1 à 15 jours

            // Calculer le nombre de jours entre le début et la fin de la réservation
            long nbJours = ChronoUnit.DAYS.between(reservation.getStartLocation(), reservation.getEndLocation());
            reservation.setNbJours((int) nbJours);

            double prixTotal = 500 + (random.nextDouble() * 1500);  // Prix entre 500 et 2000 euros
            reservation.setUser(randomUser);
            reservation.setInsurance("basic");

            // Persister la réservation avant de créer le paiement
            reservationRepository.save(reservation);

            // Créer le paiement associé à la réservation
            Payment payment = new Payment();
            payment.setReservation(reservation);
            payment.setPrixTotal(prixTotal);
            payment.setPartDriveShare(prixTotal * 0.30);  // 30% pour DriveShare
            payment.setPaiementMode("card");
            payment.setStatut("succeeded");
            payment.setCreatedAt(randomDate.atStartOfDay());

            // Sauvegarder le paiement avant d'associer le gain
            paymentRepository.save(payment);

            // Créer le gain associé au paiement
            Gain gain = new Gain();
            gain.setPayment(payment);
            gain.setDescription("Commission for payment with visa card");
            gain.setDateGain(randomDate.atStartOfDay());
            gain.setStatut("succeeded");

            // Calcul du montant du gain (prixTotal - partDriveShare)
            gain.setMontantGain(payment.getPrixTotal() - payment.getPartDriveShare());

            // Sauvegarder le gain
            gainRepository.save(gain);
        }
    }
}

