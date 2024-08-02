package be.iccbxl.tfe.Driveshare.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Exclude;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT", name = "comment")
    private String avis;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", rating=" + note +
                ", comment='" + avis + '\'' +
                '}';
    }


}
