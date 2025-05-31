package com.example.propertyrental.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AmenityDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private LocalDateTime deletedAt;
    private String deletedBy;
} 