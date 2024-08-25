package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.DTO.PaymentDTO;
import be.iccbxl.tfe.Driveshare.model.Payment;
import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query("SELECT p FROM Payment p JOIN p.reservation r JOIN r.car c WHERE c.user = :user AND p.createdAt BETWEEN :startDate AND :endDate")
    List<Payment> findByUserAndCreatedAtBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);



    // Calculer le revenu total
    @Query("SELECT SUM(p.prixTotal) FROM Payment p")
    BigDecimal calculateTotalRevenue();

    // Calculer le revenu pour un mois et une année spécifiques
    @Query("SELECT SUM(p.prixTotal) FROM Payment p WHERE MONTH(p.createdAt) = :month AND YEAR(p.createdAt) = :year")
    BigDecimal calculateRevenueForMonth(int month, int year);

    // Obtenir les revenus par mois
    @Query("SELECT MONTH(p.createdAt), YEAR(p.createdAt), SUM(p.prixTotal) FROM Payment p GROUP BY MONTH(p.createdAt), YEAR(p.createdAt) ORDER BY YEAR(p.createdAt), MONTH(p.createdAt)")
    List<Object[]> getRevenuePerMonth();


    @Query("SELECT SUM(p.partDriveShare) FROM Payment p") // Cette méthode nécessite que votre entité Payment ait un champ 'benefit'
    BigDecimal sumTotalBenefit();




}

