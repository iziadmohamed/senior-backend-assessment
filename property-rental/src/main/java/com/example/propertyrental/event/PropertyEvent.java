package com.example.propertyrental.event;

import com.example.propertyrental.dto.PropertyDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PropertyEvent {
    private EventType type; 
    private PropertyDTO property;
}