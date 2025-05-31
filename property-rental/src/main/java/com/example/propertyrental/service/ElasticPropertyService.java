package com.example.propertyrental.service;

import com.example.propertyrental.elasticsearch.PropertyDocument;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;




public interface ElasticPropertyService {

    public void save(PropertyDocument document);

    public void deleteProperty(Long id);

    public Page<PropertyDocument> search(String location, Set<Long> amenityIds, Pageable pageable);

} 