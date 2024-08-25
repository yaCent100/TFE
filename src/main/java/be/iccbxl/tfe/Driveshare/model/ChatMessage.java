package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation; // Lien vers Reservation

    @Column(nullable = false)
    private String content;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;  // ID de l'utilisateur qui envoie le message

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;  // ID de l'utilisateur qui reçoit le message


    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;
}
