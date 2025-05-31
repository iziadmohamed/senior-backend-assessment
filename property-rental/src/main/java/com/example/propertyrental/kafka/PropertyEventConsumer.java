package com.example.propertyrental.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.example.propertyrental.dto.PropertyDTO;
import com.example.propertyrental.elasticsearch.PropertyDocument;
import com.example.propertyrental.event.EventType;
import com.example.propertyrental.event.PropertyEvent;
import com.example.propertyrental.mapper.PropertyMapper;
import com.example.propertyrental.service.ElasticPropertyService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyEventConsumer {
    private final PropertyMapper mapper;
    private final ElasticPropertyService elasticService;

    @KafkaListener(topics = "property-topic", groupId = "property-consumer-group")
    public void listen(PropertyEvent propertyEvent) {
        if (propertyEvent.getType().equals(EventType.CREATE) || propertyEvent.getType().equals(EventType.UPDATE)) {
            saveOrUpdateProperty(propertyEvent.getProperty());
        }
        if (propertyEvent.getType().equals(EventType.DELETE)) {
            deletePropertyById(propertyEvent.getProperty().getId());
        }
    }

    private void saveOrUpdateProperty(PropertyDTO propertyDTO) {
        PropertyDocument document = mapper.toDocument(propertyDTO);
        elasticService.save(document);
    }

    private void deletePropertyById(Long id) {
        elasticService.deleteProperty(id);
    }
}
