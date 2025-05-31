package com.example.propertyrental.elasticsearch;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Document(indexName = "properties")
@Data
public class PropertyDocument {
    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String location;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Keyword)
    private Set<Long> amenities;

    @Field(type = FieldType.Date)
    private LocalDateTime deletedAt;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String deletedBy;


} 