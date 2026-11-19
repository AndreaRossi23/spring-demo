package com.example.spring_course;

import com.example.spring_course.repository.ProductRepository;
import com.example.spring_course.repository.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldSaveAndRetrieveProduct() {

        // Arrange
        Product product = new Product(
                "MacBook Pro",
                "Notebook Apple",
                new BigDecimal("2499.99"),
                "NOTEBOOK",
                "Apple"
        );

        // Act
        Product savedProduct = productRepository.save(product);

        // Assert
        assertNotNull(savedProduct.getId());

        Product retrievedProduct = productRepository
                .findById(savedProduct.getId())
                .orElseThrow();

        assertEquals("MacBook Pro", retrievedProduct.getName());
        assertEquals("Notebook Apple", retrievedProduct.getDescription());
        assertEquals(0, new BigDecimal("2499.99")
                .compareTo(retrievedProduct.getPrice()));
        assertEquals("NOTEBOOK", retrievedProduct.getCategory());
        assertEquals("Apple", retrievedProduct.getBrand());
    }

    @Test
    void shouldRetrieveAllProducts() {

        // Arrange
        productRepository.save(new Product(
                "MacBook Pro", "Notebook Apple",
                new BigDecimal("2499.99"), "NOTEBOOK", "Apple"
        ));

        productRepository.save(new Product(
                "ThinkPad", "Notebook Lenovo",
                new BigDecimal("1299.99"), "NOTEBOOK", "Lenovo"
        ));

        // Act
        List<Product> products = productRepository.findAll();

        // Assert
        assertEquals(2, products.size());
    }

    @Test
    void shouldDeleteProduct() {

        // Arrange
        Product product = productRepository.save(new Product(
                "MacBook Pro", "Notebook Apple",
                new BigDecimal("2499.99"), "NOTEBOOK", "Apple"
        ));

        // Act
        productRepository.deleteById(product.getId());

        // Assert
        assertFalse(productRepository.existsById(product.getId()));
    }
}