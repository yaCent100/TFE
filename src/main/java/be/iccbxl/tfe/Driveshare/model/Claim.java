package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "claims")
public class Claim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private String claimantRole; // "LOCATAIRE" or "PROPRIETAIRE"

    @Column(nullable = false)
    private String message;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "status", nullable = false)
    private String status = "EN_ATTENTE";  // Par défaut, la réclamation commence avec le statut "En attente"

    @Column
    private String response;  // Champ pour stocker la réponse de l'administrateur

    @Column
    private LocalDateTime responseAt;  // Date à laquelle une réponse a été ajoutée


    // Fonction pour marquer la réclamation comme clôturée
    public void closeClaim() {
        this.status = "TERMINE";
    }

    // Fonction pour relancer la réclamation (si l'utilisateur répond)
    public void reopenClaim(String newMessage) {
        this.message = newMessage;
        this.status = "EN_COURS";
        this.response = null;
        this.responseAt = null;
    }

}
