package com.example.propertyrental.repository;

import com.example.propertyrental.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b WHERE b.deletedAt IS NULL")
    Page<Booking> findAll(Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.id = :id AND b.deletedAt IS NULL")
    Optional<Booking> findById(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.id = :id AND b.deletedAt IS NULL")
    boolean existsById(@Param("id") Long id);

    @Query("SELECT b FROM Booking b WHERE b.property.id = :propertyId AND b.deletedAt IS NULL")
    Page<Booking> findByPropertyId(@Param("propertyId") Long propertyId, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.startDate <= :endDate AND b.endDate >= :startDate AND b.deletedAt IS NULL")
    List<Booking> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE b.property.id = :propertyId " +
           "AND b.deletedAt IS NULL " +
           "AND b.endDate > :startDate " +
           "AND b.startDate < :endDate")
    List<Booking> findOverlappingBookings(
            @Param("propertyId") Long propertyId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
    boolean existsByPropertyIdAndDeletedAtIsNullAndEndDateGreaterThanAndStartDateLessThan(
            Long propertyId, LocalDateTime startDate, LocalDateTime endDate);           
} 