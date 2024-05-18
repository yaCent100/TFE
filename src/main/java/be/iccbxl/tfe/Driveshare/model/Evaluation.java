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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "car_rental_id")
    private CarRental carRental;

    @Column(nullable = false)
    private Integer note;

    @Column(columnDefinition = "TEXT")
    private String avis;

    @Column(name = "Created_at", nullable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "Evaluation{" +
                "id=" + id +
                ", rating=" + note +
                ", comment='" + avis + '\'' +
                '}';
    }


}
