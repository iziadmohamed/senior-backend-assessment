package com.example.propertyrental.mapper;

import com.example.propertyrental.dto.BookingDTO;
import com.example.propertyrental.model.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    @Mapping(target = "propertyId", source = "property.id")
    BookingDTO toDTO(Booking booking);

    @Mapping(target = "property", ignore = true)
    @Mapping(target = "deletedBy", source = "deletedBy")
    Booking toEntity(BookingDTO bookingDTO);
} 