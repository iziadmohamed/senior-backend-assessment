package com.example.propertyrental.service.Impl;

import com.example.propertyrental.config.SecurityUtils;
import com.example.propertyrental.dto.PropertyDTO;
import com.example.propertyrental.elasticsearch.PropertyDocument;
import com.example.propertyrental.kafka.PropertyEventProducer;
import com.example.propertyrental.mapper.PropertyMapper;
import com.example.propertyrental.model.Amenity;
import com.example.propertyrental.model.Property;
import com.example.propertyrental.repository.AmenityRepository;
import com.example.propertyrental.repository.PropertyRepository;
import com.example.propertyrental.service.ElasticPropertyService;
import com.example.propertyrental.service.PropertyService;
import com.example.propertyrental.specification.PropertySpecifications;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final ElasticPropertyService elasticPropertyService;
    private final PropertyEventProducer propertyEventProducer;
    private final SecurityUtils securityUtils;

    @Override
    public Page<PropertyDTO> getAllProperties(Pageable pageable) {
        return propertyRepository.findAll(pageable).map(PropertyMapper.INSTANCE::toDTO);
    }

    @Override
    public PropertyDTO getPropertyById(Long id) {
        return PropertyMapper.INSTANCE.toDTO(findById(id));
    }

    @Override
    public Property findById(Long id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found"));
    }

    @Override
    public PropertyDTO createProperty(PropertyDTO propertyDTO) {
        String currentUserId = securityUtils.getCurrentUserId();
        Property property = PropertyMapper.INSTANCE.toEntity(propertyDTO);
        property.setId(null);
        property.setCreatedBy(currentUserId);
        PropertyDTO savedPropertyDTO = PropertyMapper.INSTANCE.toDTO(propertyRepository.save(property));
        propertyEventProducer.indexProperty(savedPropertyDTO);
        return savedPropertyDTO;
    }

    @Override
    public PropertyDTO updateProperty(Long id, PropertyDTO propertyDetailsDto) {
        String currentUserId = securityUtils.getCurrentUserId();
        Property propertyDetails = PropertyMapper.INSTANCE.toEntity(propertyDetailsDto);

        Property property = findById(id);

        if (!securityUtils.isAdmin() && !property.getCreatedBy().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own properties.");
        }
        property.setTitle(propertyDetails.getTitle());
        property.setLocation(propertyDetails.getLocation());
        property.setPrice(propertyDetails.getPrice());

        if (propertyDetails.getAmenities() != null) {
            Set<Long> amenityIds = propertyDetails.getAmenities()
                    .stream()
                    .map(Amenity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Amenity> managedAmenities = new HashSet<>(amenityRepository.findAllById(amenityIds));
            property.setAmenities(managedAmenities);
        }

        PropertyDTO updatedPropertyDTO = PropertyMapper.INSTANCE.toDTO(propertyRepository.save(property));
        propertyEventProducer.indexProperty(updatedPropertyDTO);

        return updatedPropertyDTO;
    }

    @Override
    public void deleteProperty(Long id) {
        String currentUserId = securityUtils.getCurrentUserId();
        Property property = findById(id);

        if (!securityUtils.isAdmin() && !property.getCreatedBy().equals(currentUserId)) {
            throw new AccessDeniedException("You can only delete your own properties.");
        }
        property.setDeletedAt(LocalDateTime.now());
        property.setDeletedBy(currentUserId);
        propertyRepository.save(property);
        propertyEventProducer.deleteProperty(PropertyMapper.INSTANCE.toDTO(property));
    }

    @Override
    public Page<PropertyDTO> searchPropertiesWithFallback(
            Optional<String> location,
            Optional<Set<Long>> amenityIds,
            Optional<LocalDateTime> availableStart,
            Optional<LocalDateTime> availableEnd,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);

        List<PropertyDocument> esResults = List.of();

        // ES search uses only location & amenities (no availability filtering in ES)
        esResults = elasticPropertyService.search(location.orElse(null), amenityIds.orElse(null), pageable)
                .getContent();

        Page<Property> propertiesPage;
        Page<PropertyDTO> resultPage;

        if (!esResults.isEmpty()) {

            if (availableStart.isPresent() && availableEnd.isPresent()) {
                // Apply availability filter by querying DB with specs and filtered IDs
                List<Long> esPropertyIds = esResults.stream()
                        .map(PropertyDocument::getId)
                        .toList();

                propertiesPage = propertyRepository.findAll(
                        buildSpecification(Optional.empty(), Optional.empty(), availableStart, availableEnd,
                                esPropertyIds),
                        pageable);

                // Map entities to DTOs, inject amenities from the map
                resultPage = propertiesPage.map(PropertyMapper.INSTANCE::toDTO);

            } else {
                // No availability filter: map ES documents directly to DTOs with amenities from
                // DB
                Map<Long, Amenity> amenityMap = getAmenitiesMap(esResults);

                List<PropertyDTO> dtos = PropertyMapper.INSTANCE.toDTOList(esResults, amenityMap);

                // Build a Page<PropertyDTO> manually since ES result is a List
                resultPage = new PageImpl<>(dtos, PageRequest.of(page, size), dtos.size());
            }
        } else {
            // Query DB fully with all filters (including availability)
            propertiesPage = propertyRepository.findAll(
                    buildSpecification(location, amenityIds, availableStart, availableEnd, Collections.emptyList()),
                    pageable);
            resultPage = propertiesPage.map(PropertyMapper.INSTANCE::toDTO);
        }

        return resultPage;
    }

    private Map<Long, Amenity> getAmenitiesMap(List<PropertyDocument> propertyDocuments) {

        
        Set<Long> allAmenityIds = propertyDocuments.stream()
        .flatMap(pd -> Optional.ofNullable(pd.getAmenities())
                               .map(Collection::stream)
                               .orElseGet(Stream::empty))
        .collect(Collectors.toSet());
        
        if(allAmenityIds.isEmpty())
            return new HashMap<>();

        
        List<Amenity> amenities = amenityRepository.findAllById(allAmenityIds);
        
        Map<Long, Amenity> amenityMap = amenities.stream()
                .collect(Collectors.toMap(Amenity::getId, Function.identity()));
        return amenityMap;
    }

    private Specification<Property> buildSpecification(
            Optional<String> location,
            Optional<Set<Long>> amenityIds,
            Optional<LocalDateTime> availableStart,
            Optional<LocalDateTime> availableEnd,
            List<Long> ids) {

        Specification<Property> spec = (root, query, cb) -> cb.conjunction();

        if (!ids.isEmpty()) {
            spec = spec.and(PropertySpecifications.idIn(ids));
        } else {
            if (location.isPresent()) {
                spec = spec.and(PropertySpecifications.hasLocation(location.get()));
            }
            if (amenityIds.isPresent()) {
                spec = spec.and(PropertySpecifications.hasAmenities(amenityIds.get()));
            }
        }

        if (availableStart.isPresent() && availableEnd.isPresent()) {
            spec = spec.and(PropertySpecifications.isAvailable(availableStart.get(), availableEnd.get()));
        }

        return spec;
    }
}