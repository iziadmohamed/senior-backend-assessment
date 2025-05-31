package com.example.propertyrental.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.propertyrental.dto.PropertyDTO;
import com.example.propertyrental.event.EventType;
import com.example.propertyrental.event.PropertyEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PropertyEventProducer {
    private final KafkaTemplate<String, PropertyEvent> kafkaTemplate;
    private static final String TOPIC = "property-topic";

    public void indexProperty(PropertyDTO propertyDTO) {
        kafkaTemplate.send(TOPIC, new PropertyEvent(EventType.CREATE, propertyDTO));
    }

    public void updateProperty(PropertyDTO propertyDTO) {
        kafkaTemplate.send(TOPIC, new PropertyEvent(EventType.UPDATE, propertyDTO));
    }

    public void deleteProperty(PropertyDTO propertyDTO) {
        kafkaTemplate.send(TOPIC, new PropertyEvent(EventType.DELETE, propertyDTO));
    }
}