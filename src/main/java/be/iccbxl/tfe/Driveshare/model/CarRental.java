package be.iccbxl.tfe.Driveshare.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "car_user_rental")
public class CarRental {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @OneToMany(mappedBy = "carRental", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Reservation> reservations;

    @OneToMany(mappedBy = "carRental", cascade = CascadeType.ALL,  fetch = FetchType.LAZY)
    private List<Evaluation> evaluations;
}
