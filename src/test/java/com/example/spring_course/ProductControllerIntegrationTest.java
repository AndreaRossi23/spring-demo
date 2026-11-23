package com.example.spring_course;

import com.example.spring_course.controller.model.ProductRequest;
import com.example.spring_course.controller.model.ProductResponse;
import com.example.spring_course.repository.ProductRepository;

import com.example.spring_course.repository.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        cacheManager.getCache("products").clear();
    }

    @Test
    void shouldCreateProduct() {

        // Arrange
        ProductRequest request = new ProductRequest(
                "MacBook Pro",
                "Notebook Apple",
                new BigDecimal("2499.99"),
                "NOTEBOOK",
                "Apple"
        );

        // Act
        ResponseEntity<ProductResponse> response =
                restTemplate.postForEntity(
                        "/api/products",
                        request,
                        ProductResponse.class
                );

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        assertNotNull(response.getBody().id());
        assertEquals("MacBook Pro", response.getBody().name());
        assertEquals("Notebook Apple", response.getBody().description());
        assertEquals(0, new BigDecimal("2499.99")
                .compareTo(response.getBody().price()));
        assertEquals("NOTEBOOK", response.getBody().category());
        assertEquals("Apple", response.getBody().brand());

        // Verifica anche la persistenza su PostgreSQL
        assertTrue(productRepository.existsById(response.getBody().id()));
    }

    @Test
    void shouldRetrieveAllProducts() {

        // Arrange
        productRepository.save(new Product(
                "MacBook Pro",
                "Notebook Apple",
                new BigDecimal("2499.99"),
                "NOTEBOOK",
                "Apple"
        ));

        productRepository.save(new Product(
                "ThinkPad",
                "Notebook Lenovo",
                new BigDecimal("1299.99"),
                "NOTEBOOK",
                "Lenovo"
        ));

        // Act
        ResponseEntity<ProductResponse[]> response =
                restTemplate.getForEntity(
                        "/api/products",
                        ProductResponse[].class
                );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().length);
    }

    @Test
    void shouldRetrieveProductById() {

        // Arrange
        Product product = productRepository.save(new Product(
                "MacBook Pro",
                "Notebook Apple",
                new BigDecimal("2499.99"),
                "NOTEBOOK",
                "Apple"
        ));

        // Act
        ResponseEntity<ProductResponse> response =
                restTemplate.getForEntity(
                        "/api/products/" + product.getId(),
                        ProductResponse.class
                );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(product.getId(), response.getBody().id());
        assertEquals("MacBook Pro", response.getBody().name());

        assertNotNull(cacheManager.getCache("products"));
        assertNotNull(cacheManager.getCache("products").get(product.getId()));
    }

    @Test
    void shouldUpdateProductAndRefreshCache() {

        Product product = productRepository.save(new Product(
                "MacBook Pro",
                "Notebook Apple",
                new BigDecimal("2499.99"),
                "NOTEBOOK",
                "Apple"
        ));

        restTemplate.getForEntity(
                "/api/products/" + product.getId(),
                ProductResponse.class
        );

        ProductRequest request = new ProductRequest(
                "MacBook Air",
                "Notebook Apple aggiornato",
                new BigDecimal("1999.99"),
                "NOTEBOOK",
                "Apple"
        );

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/" + product.getId(),
                org.springframework.http.HttpMethod.PUT,
                new org.springframework.http.HttpEntity<>(request),
                ProductResponse.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("MacBook Air", response.getBody().name());
        assertEquals("MacBook Air", cacheManager.getCache("products")
                .get(product.getId(), ProductResponse.class).name());
    }

    @Test
    void shouldDeleteProduct() {

        // Arrange
        Product product = productRepository.save(new Product(
                "MacBook Pro",
                "Notebook Apple",
                new BigDecimal("2499.99"),
                "NOTEBOOK",
                "Apple"
        ));

        // Act
        ResponseEntity<Void> response =
                restTemplate.exchange(
                        "/api/products/" + product.getId(),
                        org.springframework.http.HttpMethod.DELETE,
                        null,
                        Void.class
                );

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(productRepository.existsById(product.getId()));
        assertNull(cacheManager.getCache("products").get(product.getId()));
    }

    @Test
    void shouldRetrieveProductAsynchronously() {

        Product product = productRepository.save(new Product(
                "MacBook Pro",
                "Notebook Apple",
                new BigDecimal("2499.99"),
                "NOTEBOOK",
                "Apple"
        ));

        long start = System.currentTimeMillis();

        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/products/" + product.getId() + "/async",
                ProductResponse.class
        );

        long elapsed = System.currentTimeMillis() - start;

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(product.getId(), response.getBody().id());
        assertTrue(elapsed >= 2900);
    }

    @Test
    void shouldReturnBadRequestWhenProductIsInvalid() {

        // Arrange: prezzo negativo e nome vuoto
        ProductRequest request = new ProductRequest(
                "",
                "Notebook Apple",
                new BigDecimal("-10"),
                "NOTEBOOK",
                "Apple"
        );

        // Act
        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/products",
                        request,
                        String.class
                );

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(productRepository.findAll().isEmpty());
    }
}