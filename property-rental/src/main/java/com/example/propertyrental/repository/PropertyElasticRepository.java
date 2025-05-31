package com.example.propertyrental.repository;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.example.propertyrental.elasticsearch.PropertyDocument;

public interface PropertyElasticRepository extends ElasticsearchRepository<PropertyDocument, Long> {
    Page<PropertyDocument> findByLocationAndAmenities(String location, Set<String> amenities, Pageable pageable);
}