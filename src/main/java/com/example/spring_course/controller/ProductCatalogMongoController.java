package com.example.spring_course.controller;

import com.example.spring_course.controller.model.ProductRequestMongo;
import com.example.spring_course.controller.model.ProductResponseMongo;
import com.example.spring_course.service.ProductCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalog/products")
public class ProductCatalogMongoController {

    private final ProductCatalogService service;

    public ProductCatalogMongoController(ProductCatalogService service) {
        this.service = service;
    }

    @PostMapping("/admin/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseMongo create(@Valid @RequestBody ProductRequestMongo request) {

        return service.create(request);
    }

    @GetMapping("/public/all")
    public List<ProductResponseMongo> findAll() {
        return service.findAll();
    }

    @GetMapping("/user/{id}")
    public ProductResponseMongo findById(@PathVariable String id) {
        return service.findById(id);
    }

    @PutMapping("/admin/update/{id}")
    public ProductResponseMongo update(
            @PathVariable String id,
            @Valid @RequestBody ProductRequestMongo request) {

        return service.update(id, request);
    }

    @DeleteMapping("/admin/delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }

    @GetMapping("/user/category/{category}")
    public List<ProductResponseMongo> findByCategory(@PathVariable String category) {

        return service.findByCategory(category);
    }

    @GetMapping("/user/search")
    public List<ProductResponseMongo> searchByName(
            @RequestParam String name) {

        return service.searchByName(name);
    }
}