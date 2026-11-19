package com.example.spring_course.service;

import com.example.spring_course.controller.model.ProductRequest;
import com.example.spring_course.controller.model.ProductResponse;
import com.example.spring_course.repository.ProductRepository;
import com.example.spring_course.repository.mapper.ProductMapper;
import com.example.spring_course.repository.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductService(ProductRepository productRepository,
                          ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return productMapper.toResponse(product);
    }

    @Transactional
    public void delete(Long id) {
        productRepository.deleteById(id);
    }
}