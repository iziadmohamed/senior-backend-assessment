package com.example.propertyrental;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@SpringBootApplication
@EnableElasticsearchRepositories
public class PropertyRentalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropertyRentalApplication.class, args);
    }
} 