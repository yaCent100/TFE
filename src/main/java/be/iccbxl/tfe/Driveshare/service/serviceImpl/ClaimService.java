package be.iccbxl.tfe.Driveshare.service.serviceImpl;

import be.iccbxl.tfe.Driveshare.DTO.ClaimDTO;
import be.iccbxl.tfe.Driveshare.DTO.MapperDTO;
import be.iccbxl.tfe.Driveshare.model.Car;
import be.iccbxl.tfe.Driveshare.model.Claim;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import be.iccbxl.tfe.Driveshare.repository.ClaimRepository;
import be.iccbxl.tfe.Driveshare.repository.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private NotificationService notificationService;

    public Claim createClaim(Long reservationId, String message, String claimantRole) {
        // Récupérer l'objet Reservation à partir de l'ID
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation non trouvée"));

        // Vérifier s'il y a déjà une réclamation pour ce rôle et cette réservation
        Optional<Claim> existingClaim = claimRepository.findByReservationAndClaimantRole(reservation, claimantRole);

        if (existingClaim.isPresent()) {
            throw new IllegalStateException("Il y a déjà une réclamation pour ce rôle.");
        }

        // Créer la réclamation
        Claim claim = new Claim();
        claim.setReservation(reservation);
        claim.setMessage(message);
        claim.setClaimantRole(claimantRole);
        claim.setCreatedAt(LocalDateTime.now());

        return claimRepository.save(claim);
    }


    public List<Claim> getClaimsByReservation(Reservation reservation) {
        return claimRepository.findAllByReservation(reservation);
    }

    // Méthode pour récupérer les réclamations liées à une réservation spécifique
    public List<Claim> getClaimsByReservation(Long reservationId) {
        return claimRepository.findByReservationId(reservationId);
    }

    public List<Claim> getALlClaims(){
        return claimRepository.findAll();
    }

    public void resolveClaim(Long id) {
        Claim claim = claimRepository.findById(id).orElseThrow(() -> new RuntimeException("Réclamation non trouvée"));
        claim.setStatus("FINISHED");
        claimRepository.save(claim);
    }

    public void addResponseToClaim(Long claimId, String responseMessage) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Réclamation introuvable"));

        // Ajouter la réponse à la réclamation et définir la date de réponse
        claim.setResponse(responseMessage);
        claim.setStatus("IN_PROGRESS"); // Mettre à jour le statut à "En cours"
        claim.setResponseAt(LocalDateTime.now());  // Date de réponse

        // Sauvegarder la réclamation mise à jour
        claimRepository.save(claim);

        // Déterminer l'utilisateur qui doit recevoir la notification
        User toUser;
        Car car = claim.getReservation().getCar(); // La voiture liée à la réclamation

        if ("proprietaire".equalsIgnoreCase(claim.getClaimantRole())) {
            toUser = claim.getReservation().getCar().getUser();  // Propriétaire de la voiture
        } else {
            toUser = claim.getReservation().getUser();  // Utilisateur qui a loué la voiture
        }

        // Envoi de la notification
        String notificationMessage = String.format("Réclamation n°%d : Une réponse a été ajoutée.", claim.getId());

        // Envoyer la notification avec la voiture et l'utilisateur cible
        notificationService.sendNotification(toUser, car, notificationMessage);
    }

    public long countAllClaims() {
        return claimRepository.count();
    }

    public long countResolvedClaims() {
        return claimRepository.countByStatus("FINISHED");
    }

    public long countPendingClaims() {
        return claimRepository.countByStatus("PENDING");
    }

    public long countInProgressClaims() {
        return claimRepository.countByStatus("IN_PROGRESS"); // Ou l'état correspondant
    }


    public List<Object[]> countClaimsByMonthForLastYear(LocalDateTime startDate) {
        return claimRepository.countClaimsByMonthForLastYear(startDate);
    }

    public void save(Claim claim) {
        claimRepository.save(claim);
    }

    public List<Claim> getClaimsForUser(Long user) {
        return claimRepository.findByUserId(user);
    }

    public Optional<Claim> getById(Long id) {
        return claimRepository.findById(id);
    }

    public String renderClaimsHtml(List<ClaimDTO> claims, User currentUser) {
        StringBuilder html = new StringBuilder();

        // Vérification si la liste de réclamations est vide
        if (claims.isEmpty()) {
            // Ajouter un message d'information avec une image d'alerte barrée
            html.append("<div class='d-flex flex-column align-items-center justify-content-center' style='min-height: 200px;'>")
                    .append("<img src='/icons/no-notification.png' alt='Pas de réclamations' class='img-fluid' style='max-width: 150px;'>")
                    .append("<h5 class='mt-3 text-muted'>Aucune réclamation trouvée</h5>")
                    .append("</div>");
            return html.toString();  // Retourner ici car il n'y a pas de réclamations à afficher
        } else {
            // Boucle sur les réclamations trouvées
            for (ClaimDTO claim : claims) {
                html.append("<div class='card mb-3 claim-card ")
                        .append("'>")
                        .append("<div class='row g-0'>");

                // Colonne gauche - Image et informations sur la voiture
                html.append("<div class='col-lg-9'>")
                        .append("<div class='d-flex align-items-center mb-2'>")
                        .append("<img src='/uploads/photo-car/")
                        .append(claim.getCarPhoto() == null || claim.getCarPhoto().isEmpty() ? "default-car.jpg" : claim.getCarPhoto())  // Image de la voiture ou image par défaut
                        .append("' alt='")
                        .append(claim.getBrand()).append(" ").append(claim.getModel())  // Texte alternatif de l'image
                        .append("' class='img-fluid rounded-circle me-3' style='width: 60px; height: 60px;'>")  // Image arrondie de petite taille
                        .append("<h5 class='mb-0'>Réservation n°")
                        .append(claim.getReservationId()).append(" | ").append(claim.getBrand()).append(" ")
                        .append(claim.getModel())  // Marque et modèle de la voiture
                        .append("</h5>")
                        .append("</div>");  // Fin de la ligne d'image et du titre

                // Ajout du message de réclamation
                html.append("<p class='mb-1'><strong>Réclamation :</strong> ")
                        .append(claim.getMessage()).append("</p>");

                // Si une réponse de l'administrateur est disponible, l'afficher
                if (claim.getResponse() != null && !claim.getResponse().isEmpty()) {
                    html.append("<p><strong>Solution proposée :</strong> ")
                            .append(claim.getResponse()).append("</p>");
                }

                html.append("</div>");  // Fin de la colonne gauche

                // Colonne droite - Date/Heure et Boutons
                html.append("<div class='col-lg-3 d-flex flex-column justify-content-between text-end'>")
                        .append("<div>")
                        .append("<small class='text-muted'>")
                        .append(claim.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy à HH'h'mm")))  // Date et heure formatée
                        .append("</small>")
                        .append("</div>");

                // Boutons conditionnels selon le statut
                if (claim.getStatus().equals("IN_PROGRESS")) {
                    html.append("<button class='btn btn-primary mt-2' onclick=\"showReplyModal(")
                            .append(claim.getId()).append(", '").append(claim.getMessage()).append("')\">Répondre</button>");
                    html.append("<button class='btn btn-success mt-2' onclick='markAsFinished(")
                            .append(claim.getId()).append(")'>Terminer</button>");
                }

                html.append("</div>");  // Fin de la colonne droite
                html.append("</div>");  // Fin de la rangée
                html.append("</div>");  // Fin de la carte
            }
        }

        return html.toString();
    }





    public List<ClaimDTO> findClaimsByStatus(User currentUser, String status) {
        // Filtrer les réclamations en tant que locataire ou propriétaire
        List<Claim> claimsAsTenant = claimRepository.findByTenantAndStatus(currentUser.getId(), status);
        List<Claim> claimsAsOwner = claimRepository.findByOwnerAndStatus(currentUser.getId(), status);

        // Combiner les réclamations du locataire et du propriétaire
        List<Claim> combinedClaims = new ArrayList<>();
        combinedClaims.addAll(claimsAsTenant);
        combinedClaims.addAll(claimsAsOwner);

        // Convertir les entités Claim en DTOs
        return combinedClaims.stream()
                .map(MapperDTO::toClaimDto) // Mapper chaque entité Claim en ClaimDTO
                .collect(Collectors.toList());
    }

    public boolean existsByReservationId(Long reservationId) {
        return claimRepository.existsByReservationId(reservationId);
    }
}
