package com.example.propertyrental.controller;

import com.example.propertyrental.dto.PropertyDTO;
import com.example.propertyrental.service.PropertyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;


@RestController
@RequestMapping("/api/properties")
public class PropertyController {
    @Autowired
    private PropertyService propertyService;

    @PreAuthorize("hasRole('admin') or hasRole('owner')")
    @PostMapping
    public ResponseEntity<PropertyDTO> createProperty(@Valid @RequestBody PropertyDTO propertyDTO) {
        PropertyDTO savedProperty = propertyService.createProperty(propertyDTO);
        return ResponseEntity.ok(savedProperty);
    }


    @PreAuthorize("hasRole('admin') or hasRole('owner') or hasRole('user')")
    @GetMapping
    public ResponseEntity<Page<PropertyDTO>> getAllProperties(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<PropertyDTO> propertyDTOs = propertyService.getAllProperties(pageRequest);
        return ResponseEntity.ok(propertyDTOs);
    }


    @PreAuthorize("hasRole('admin') or hasRole('owner') or hasRole('user')")
    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable Long id) {
            PropertyDTO property = propertyService.getPropertyById(id);
            return ResponseEntity.ok(property);
    }


    @PreAuthorize("hasRole('admin') or hasRole('owner')")
    @PutMapping("/{id}")
    public ResponseEntity<PropertyDTO> updateProperty(@PathVariable Long id, @Valid @RequestBody PropertyDTO propertyDTO) {
            PropertyDTO updated = propertyService.updateProperty(id, propertyDTO);
            return ResponseEntity.ok(updated);
    }


    @PreAuthorize("hasRole('admin') or hasRole('owner')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(@PathVariable Long id) {
            propertyService.deleteProperty(id);
            return ResponseEntity.noContent().build();
    }
    
    
    @PreAuthorize("hasRole('admin') or hasRole('owner') or hasRole('user')")
    @GetMapping("/search")
    public ResponseEntity<Page<PropertyDTO>> searchProperties(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Set<Long> amenities,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<PropertyDTO> propertyDTOs = propertyService.searchPropertiesWithFallback(
            Optional.ofNullable(location),
            Optional.ofNullable(amenities),
            Optional.ofNullable(startDateTime),
            Optional.ofNullable(endDateTime),
            page,
            size
        );
       
        return ResponseEntity.ok(propertyDTOs);
    }
} 