package com.example.propertyrental.repository;

import com.example.propertyrental.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    @Query("SELECT a FROM Amenity a WHERE a.id IN :ids AND a.deletedAt IS NULL")
    List<Amenity> findAllById(@Param("ids") Set<Long> ids);
} 