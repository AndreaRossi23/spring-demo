package com.example.spring_course.repository.mapper;

import com.example.spring_course.controller.model.ProductRequestMongo;
import com.example.spring_course.controller.model.ProductResponseMongo;
import com.example.spring_course.repository.model.ProductDocument;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProductMongoMapper {

    public ProductDocument toDocument(ProductRequestMongo request) {

        ProductDocument document = new ProductDocument();

        document.setName(request.name());
        document.setDescription(request.description());
        document.setPrice(request.price());
        document.setCategory(request.category());
        document.setCreatedAt(Instant.now());
        document.setBrand(request.brand());
        document.setTechnicalDetails(request.technicalDetails());

        return document;
    }

    public ProductResponseMongo toResponse(ProductDocument document) {

        return new ProductResponseMongo(
            document.getId(),
            document.getName(),
            document.getDescription(),
            document.getPrice(),
            document.getCategory(),
            document.getCreatedAt(),
            document.getBrand(),
            document.getTechnicalDetails()
        );
    }
}