package com.example.propertyrental.service;

import com.example.propertyrental.dto.BookingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingDTO createBooking(BookingDTO bookingDTO);
    Page<BookingDTO> getAllBookings(Pageable pageable);
    BookingDTO getBookingById(Long id);
    void deleteBooking(Long id);
    Page<BookingDTO> getBookingsByPropertyId(Long propertyId, Pageable pageable);
} 