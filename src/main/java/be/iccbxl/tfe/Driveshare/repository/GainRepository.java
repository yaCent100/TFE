package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Gain;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GainRepository extends JpaRepository<Gain, Long> {
    @Query("SELECT g FROM Gain g WHERE g.payment.reservation.car.user.id = :userId")
    List<Gain> findGainsByUserId(@Param("userId") Long userId);

    @Query("SELECT g FROM Gain g " +
            "JOIN g.payment p " +
            "JOIN p.reservation r " +
            "JOIN r.car c " +
            "JOIN c.user u " +
            "WHERE u.id = :userId AND g.dateGain BETWEEN :startDate AND :endDate")
    List<Gain> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);


    Gain findByPaymentReservation(Reservation reservation);


}


