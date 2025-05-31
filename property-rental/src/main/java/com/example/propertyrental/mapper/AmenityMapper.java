package com.example.propertyrental.mapper;

import com.example.propertyrental.dto.AmenityDTO;
import com.example.propertyrental.model.Amenity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AmenityMapper {
    AmenityMapper INSTANCE = Mappers.getMapper(AmenityMapper.class);

    @Mapping(target = "deletedBy", source = "deletedBy")
    AmenityDTO toDTO(Amenity amenity);

    @Mapping(target = "deletedBy", source = "deletedBy")
    Amenity toEntity(AmenityDTO amenityDTO);
} 