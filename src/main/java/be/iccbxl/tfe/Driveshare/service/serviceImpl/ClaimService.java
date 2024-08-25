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
        claim.setStatus("Résolue");
        claimRepository.save(claim);
    }

    public void addResponseToClaim(Long claimId, String responseMessage) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Réclamation introuvable"));

        // Ajouter la réponse à la réclamation et définir la date de réponse
        claim.setResponse(responseMessage);
        claim.setStatus("En cours"); // Mettre à jour le statut à "En cours"
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
        return claimRepository.countByStatus("Resolved");
    }

    public long countPendingClaims() {
        return claimRepository.countByStatus("Pending");
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

        if (claims.isEmpty()) {
            html.append("<div>Aucune réclamation à afficher.</div>");
        } else {
            for (ClaimDTO claim : claims) {
                html.append("<div class='card ")
                        .append(claim.getStatus().equals("FINISHED") ? "read" : "unread")
                        .append(" mb-3'>")
                        .append("<div class='card-body'>");

                html.append("<div class='d-flex align-items-center mb-3'>")
                        .append("<h5 class='card-title mb-0'>")
                        .append("Réclamation pour la réservation n°").append(claim.getReservationId())
                        .append("</h5>")
                        .append("<small class='text-muted'>").append(claim.getCreatedAt()).append("</small>")
                        .append("</div>")
                        .append("<p class='card-text'>").append(claim.getMessage()).append("</p>")
                        .append("<p class='card-text'>Statut: ").append(claim.getStatus()).append("</p>");

                if (claim.getStatus().equals("IN_PROGRESS")) {
                    html.append("<button class='btn btn-primary mt-2' data-bs-toggle='modal' data-bs-target='#replyModal")
                            .append(claim.getId()).append("'>Répondre</button>");
                }

                html.append("</div></div>");
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
}
