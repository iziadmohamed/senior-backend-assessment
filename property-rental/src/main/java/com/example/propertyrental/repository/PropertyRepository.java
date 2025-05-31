package com.example.propertyrental.repository;

import com.example.propertyrental.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> ,JpaSpecificationExecutor<Property>{

    @Query("SELECT p FROM Property p WHERE p.id = :id AND p.deletedAt IS NULL")
    Optional<Property> findById(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Property p WHERE p.id = :id AND p.deletedAt IS NULL")
    boolean existsById(@Param("id") Long id);

    @Query("SELECT DISTINCT p FROM Property p " +
           "LEFT JOIN p.amenities a " +
           "LEFT JOIN p.bookings b " +
           "WHERE p.deletedAt IS NULL " +
           "AND (:location IS NULL OR LOWER(p.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:amenityIds IS NULL OR a.id IN :amenityIds AND a.deletedAt IS NULL) " +
           "AND (:startDateTime IS NULL OR :endDateTime IS NULL OR " +
           "NOT EXISTS (SELECT 1 FROM Booking bk WHERE bk.property = p " +
           "AND bk.startDate <= :endDateTime AND bk.endDate >= :startDateTime " +
           "AND bk.deletedAt IS NULL))")
    Page<Property> searchProperties(
            @Param("location") String location,
            @Param("amenityIds") Set<Long> amenityIds,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);
} 