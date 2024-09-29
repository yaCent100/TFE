package be.iccbxl.tfe.Driveshare.repository;

import be.iccbxl.tfe.Driveshare.DTO.ReservationDataDTO;
import be.iccbxl.tfe.Driveshare.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByEmail(String email);

    long countCarsById(Long userId);



    long countByCreatedAtAfter(LocalDate date);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate GROUP BY MONTH(u.createdAt)")
    List<Integer> getUserGrowthPerMonth();

    List<User> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.role = :role")
    List<User> findByRole(@Param("role") String role);



    @Query(value = "SELECT DATE_FORMAT(created_at, '%Y-%m') as period, COUNT(*) as count FROM users GROUP BY period ORDER BY period", nativeQuery = true)
    List<ReservationDataDTO> findRegistrationsByMonth();

    @Query("SELECT new map(u.locality as locality, COUNT(u) as count) " +
            "FROM User u " +
            "GROUP BY u.locality")
    List<Map<String, Object>> findUsersGroupedByLocality();

    // Comptage des nouvelles inscriptions pour les 30 derniers jours
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :targetDate")
    int countNewRegistrationsSince(@Param("targetDate") LocalDateTime targetDate);


    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.ownedCars c WHERE c IS NOT NULL")
    int countUsersWithRegisteredCars();

    boolean existsByEmail(String email); // Cette méthode vérifie si l'email existe


}
