package com.example.propertyrental.specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;

import com.example.propertyrental.model.Amenity;
import com.example.propertyrental.model.Booking;
import com.example.propertyrental.model.Property;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public class PropertySpecifications {

    public static Specification<Property> hasLocation(String location) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<Property> hasAmenities(Set<Long> amenityIds) {
        return (root, query, cb) -> {
            root.fetch("amenities", JoinType.INNER);
            if (amenityIds == null || amenityIds.isEmpty()) {
                return cb.conjunction();
            }
            Join<Property, Amenity> amenities = root.join("amenities", JoinType.INNER);
            return amenities.get("id").in(amenityIds);
        };
    }

    public static Specification<Property> isAvailable(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start == null || end == null) {
                return cb.conjunction();
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            Root<Booking> booking = subquery.from(Booking.class);

            subquery.select(booking.get("property").get("id"));
            Predicate overlap = cb.and(
                cb.lessThanOrEqualTo(booking.get("startDate"), end),
                cb.greaterThanOrEqualTo(booking.get("endDate"), start)
            );
            subquery.where(overlap);

            return cb.not(root.get("id").in(subquery));
        };
    }

    public static Specification<Property> idIn(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return (root, query, cb) -> cb.disjunction(); // no matches
        }
        return (root, query, cb) -> root.get("id").in(ids);
    }
}