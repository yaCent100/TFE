package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.model.Claim;
import be.iccbxl.tfe.Driveshare.model.Reservation;
import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByReservationId(Long reservationId);

    List<Claim> findAllByReservation(Reservation reservation);


    Optional<Claim> findByReservationAndClaimantRole(Reservation reservation, String claimantRole);

    long countByStatus(String resolved);

    @Query("SELECT DATE_FORMAT(c.createdAt, '%m-%Y'), COUNT(c) FROM Claim c WHERE c.createdAt >= :startDate GROUP BY DATE_FORMAT(c.createdAt, '%m-%Y') ORDER BY DATE_FORMAT(c.createdAt, '%m-%Y')")
    List<Object[]> countClaimsByMonthForLastYear(@Param("startDate") LocalDateTime startDate);


    @Query("SELECT c FROM Claim c WHERE " +
            "(c.claimantRole = 'PROPRIETAIRE' AND c.reservation.car.user.id = :userId) OR " +
            "(c.claimantRole = 'LOCATAIRE' AND c.reservation.user.id = :userId)")
    List<Claim> findByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Claim c WHERE c.reservation.user.id = :tenantId AND c.claimantRole = 'LOCATAIRE' AND c.status = :status")
    List<Claim> findByTenantAndStatus(@Param("tenantId") Long tenantId, @Param("status") String status);

    // Récupérer les réclamations en tant que propriétaire
    @Query("SELECT c FROM Claim c WHERE c.reservation.car.user.id = :ownerId AND c.claimantRole = 'PROPRIETAIRE' AND c.status = :status")
    List<Claim> findByOwnerAndStatus(@Param("ownerId") Long ownerId, @Param("status") String status);

    boolean existsByReservationId(Long reservationId);


}
    // Récupérer les réclamations en tant que locataire

