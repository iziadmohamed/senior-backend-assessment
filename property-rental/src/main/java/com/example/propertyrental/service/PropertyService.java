package com.example.propertyrental.service;

import com.example.propertyrental.dto.PropertyDTO;
import com.example.propertyrental.model.Property;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface PropertyService {
    Page<PropertyDTO> getAllProperties(Pageable pageable);
    PropertyDTO getPropertyById(Long id);
    PropertyDTO createProperty(PropertyDTO propertyDTO);
    PropertyDTO updateProperty(Long id, PropertyDTO propertyDTO);
    void deleteProperty(Long id);
    public Page<PropertyDTO> searchPropertiesWithFallback(
        Optional<String> location,
        Optional<Set<Long>> amenityIds,
        Optional<LocalDateTime> availableStart,
        Optional<LocalDateTime> availableEnd,
        int page,
        int size);
    public Property findById(Long id);
} 