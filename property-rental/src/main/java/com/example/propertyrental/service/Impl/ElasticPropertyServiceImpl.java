package com.example.propertyrental.service.Impl;

import com.example.propertyrental.repository.PropertyElasticRepository;
import com.example.propertyrental.service.ElasticPropertyService;

import lombok.RequiredArgsConstructor;

import com.example.propertyrental.elasticsearch.PropertyDocument;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ElasticPropertyServiceImpl implements ElasticPropertyService{
    private final PropertyElasticRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;
    
    @Override
    public void save(PropertyDocument document) {
        repository.save(document);
    }

    @Override
    public void deleteProperty(Long id) {
        repository.deleteById(id);
    }
    
    @Override
    public Page<PropertyDocument> search(String location, Set<Long> amenityIds, Pageable pageable) {
        Criteria criteria = new Criteria();

        if (location != null && !location.isBlank()) {
            criteria = criteria.and(new Criteria("location").is(location));
        }

        if (amenityIds != null && !amenityIds.isEmpty()) {
            for (Long amenityId : amenityIds) {
                criteria = criteria.and(new Criteria("amenities").in(amenityId));
            }
        }

        CriteriaQuery query = new CriteriaQuery(criteria, pageable);
        SearchHits<PropertyDocument> hits = elasticsearchOperations.search(query, PropertyDocument.class);

        List<PropertyDocument> results = hits.stream()
            .map(SearchHit::getContent)
            .toList();

        return new PageImpl<>(results, pageable, hits.getTotalHits());
    }

} 