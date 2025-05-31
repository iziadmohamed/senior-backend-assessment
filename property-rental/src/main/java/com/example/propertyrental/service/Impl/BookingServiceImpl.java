package com.example.propertyrental.service.Impl;

import com.example.propertyrental.config.SecurityUtils;
import com.example.propertyrental.dto.BookingDTO;
import com.example.propertyrental.exception.BookingConflictException;
import com.example.propertyrental.mapper.BookingMapper;
import com.example.propertyrental.model.Booking;
import com.example.propertyrental.model.Property;
import com.example.propertyrental.repository.BookingRepository;
import com.example.propertyrental.service.BookingService;
import com.example.propertyrental.service.PropertyService;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final PropertyService propertyService;
    private final SecurityUtils securityUtils;
    
    @Transactional
    @Override
    public BookingDTO createBooking(BookingDTO bookingDTO) {
        String currentUserId = securityUtils.getCurrentUserId();
        boolean overlapExists = bookingRepository.existsByPropertyIdAndDeletedAtIsNullAndEndDateGreaterThanAndStartDateLessThan(
        bookingDTO.getPropertyId(), bookingDTO.getStartDate(), bookingDTO.getEndDate());

        if (overlapExists) {
            throw new BookingConflictException("Overlapping booking exists for the given dates.");
        }
        bookingDTO.setOwnerId(currentUserId);
        Property property = propertyService.findById(bookingDTO.getPropertyId());
        Booking saved =BookingMapper.INSTANCE.toEntity(bookingDTO); 
        saved.setProperty(property);
        return BookingMapper.INSTANCE.toDTO(bookingRepository.save(saved));
    }

    @Override
    public Page<BookingDTO> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable).map(BookingMapper.INSTANCE::toDTO);
    }

    @Override
    public BookingDTO getBookingById(Long id) {
        return BookingMapper.INSTANCE.toDTO(findById(id));
    }

    private Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found"));
    }

    @Override
    public void deleteBooking(Long id) {
        String currentUserId = securityUtils.getCurrentUserId();
        Booking booking = findById(id);

        if (!securityUtils.isAdmin() && !booking.getOwnerId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own properties.");
        }
        booking.setDeletedAt(LocalDateTime.now());
        booking.setDeletedBy(currentUserId);
        
        bookingRepository.save(booking);
    }

    @Override
    public Page<BookingDTO> getBookingsByPropertyId(Long propertyId, Pageable pageable) {
        return bookingRepository.findByPropertyId(propertyId, pageable).map(BookingMapper.INSTANCE::toDTO);
    }
} 