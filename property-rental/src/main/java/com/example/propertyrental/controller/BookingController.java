package com.example.propertyrental.controller;

import com.example.propertyrental.config.SecurityUtils;
import com.example.propertyrental.dto.BookingDTO;
import com.example.propertyrental.service.BookingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    @Autowired
    private BookingService bookingService;
    
    @PreAuthorize("hasRole('admin') or hasRole('owner') or hasRole('user')")
    @PostMapping
    public ResponseEntity<BookingDTO> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        return ResponseEntity.ok(bookingService.createBooking(bookingDTO));
    }

    @PreAuthorize("hasRole('admin') or hasRole('owner') or hasRole('user')")
    @GetMapping
    public ResponseEntity<Page<BookingDTO>> getAllBookings(Pageable pageable) {
        Page<BookingDTO> bookingDTOs = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(bookingDTOs);
    }

    @PreAuthorize("hasRole('admin') or hasRole('owner') or hasRole('user')")
    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('admin') or hasRole('owner') or hasRole('user')")
    @GetMapping("/search/property")
    public ResponseEntity<Page<BookingDTO>> searchByPropertyId(
            @RequestParam Long propertyId,            
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        Page<BookingDTO> bookingDTOs = bookingService.getBookingsByPropertyId(propertyId,pageRequest);
        return ResponseEntity.ok(bookingDTOs);
    }
} 