package com.example.spring_course.service;


import com.example.spring_course.controller.model.ProductRequestMongo;
import com.example.spring_course.controller.model.ProductResponseMongo;
import com.example.spring_course.repository.ProductMongoRepository;
import com.example.spring_course.repository.mapper.ProductMongoMapper;
import com.example.spring_course.repository.model.ProductDocument;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ProductCatalogService {

    private final ProductMongoRepository repository;
    private final ProductMongoMapper mapper;

    public ProductCatalogService(
            ProductMongoRepository repository,
            ProductMongoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ProductResponseMongo create(ProductRequestMongo request) {

        ProductDocument document = mapper.toDocument(request);

        ProductDocument saved = repository.save(document);

        return mapper.toResponse(saved);
    }


    public List<ProductResponseMongo> findAll() {

        return repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public ProductResponseMongo findById(String id) {

        ProductDocument document = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Product not found: " + id
                        )
                );

        return mapper.toResponse(document);
    }

    public ProductResponseMongo update(
            String id,
            ProductRequestMongo request) {

        ProductDocument document = repository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                NOT_FOUND,
                                "Product not found: " + id
                        )
                );

        document.setName(request.name());
        document.setDescription(request.description());
        document.setPrice(request.price());
        document.setCategory(request.category());

        ProductDocument updated = repository.save(document);

        return mapper.toResponse(updated);
    }

    public void delete(String id) {

        if (!repository.existsById(id)) {
            throw new ResponseStatusException(
                    NOT_FOUND,
                    "Product not found: " + id
            );
        }

        repository.deleteById(id);
    }

    public List<ProductResponseMongo> findByCategory(String category) {

        return repository.findByCategory(category)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<ProductResponseMongo> searchByName(String name) {

        return repository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}