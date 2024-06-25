package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    private boolean reservationEmailLocataire;
    private boolean reservationSmsLocataire;
    private boolean messagesEmailLocataire;
    private boolean confirmedEmailLocataire;
    private boolean confirmedSmsLocataire;
    private boolean startEmailLocataire;
    private boolean startSmsLocataire;
    private boolean endEmailLocataire;
    private boolean endSmsLocataire;
    private boolean cancelEmailLocataire;

    private boolean reservationEmailProprietaire;
    private boolean reservationSmsProprietaire;
    private boolean messagesEmailProprietaire;
    private boolean confirmedEmailProprietaire;
    private boolean confirmedSmsProprietaire;
    private boolean startEmailProprietaire;
    private boolean startSmsProprietaire;
    private boolean endEmailProprietaire;
    private boolean endSmsProprietaire;
    private boolean cancelEmailProprietaire;
}

