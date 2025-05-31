package com.example.propertyrental.mapper;

import com.example.propertyrental.dto.AmenityDTO;
import com.example.propertyrental.dto.PropertyDTO;
import com.example.propertyrental.elasticsearch.PropertyDocument;
import com.example.propertyrental.model.Amenity;
import com.example.propertyrental.model.Property;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PropertyMapper {
    PropertyMapper INSTANCE = Mappers.getMapper(PropertyMapper.class);

    PropertyDTO toDTO(Property property);


    Property toEntity(PropertyDTO propertyDTO);


    List<PropertyDTO> toDTOList(List<Property> properties);


    @Mapping(target = "amenities", source = "amenities", qualifiedByName = "amenitiesToString")

    PropertyDocument toDocument(PropertyDTO propertyDTO);

    @Mapping(target = "amenities",ignore = true)
    PropertyDTO toDTO(PropertyDocument propertyDocument);

    @Named("amenitiesToString")
    default Set<Long> amenitiesToString(Set<AmenityDTO> amenities) {
        if(amenities == null){
            return null;
        }
        return amenities.stream()
                .map(AmenityDTO::getId)
                .collect(Collectors.toSet());
    }

    default List<PropertyDTO> toDTOList(List<PropertyDocument> documents, Map<Long, Amenity> amenityMap) {
        if (documents == null) {
            return Collections.emptyList();
        }
        List<PropertyDTO> dtos = new ArrayList<>();
        for (PropertyDocument doc : documents) {
            PropertyDTO dto = INSTANCE.toDTO(doc);
            if (doc.getAmenities() != null) {
                Set<AmenityDTO> amenityDTOs = doc.getAmenities().stream()
                    .map(amenityMap::get)           
                    .filter(Objects::nonNull)   
                    .map(AmenityMapper.INSTANCE::toDTO) 
                    .collect(Collectors.toSet());
                dto.setAmenities(amenityDTOs);
            }
            dtos.add(dto);
        }
        return dtos;
    }
} 