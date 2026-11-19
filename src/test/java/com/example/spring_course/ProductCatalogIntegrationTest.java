package com.example.spring_course;

import com.example.spring_course.controller.model.ProductRequestMongo;
import com.example.spring_course.repository.model.ProductDocument;
import com.example.spring_course.repository.ProductMongoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureTestRestTemplate
@Testcontainers
@ActiveProfiles("mongo")
class ProductCatalogIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProductMongoRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void shouldCreateProduct() {

        // ARRANGE
        ProductRequestMongo request = new ProductRequestMongo(
            "Lenovo ThinkPad",
            "Notebook professionale",
            new BigDecimal("1299.99"),
            "NOTEBOOK",
            "Lenovo",
            Map.of(
                "processor", "Intel i7",
                "ram", 32,
                "storage", "1TB SSD",
                "screenSize", 15.6,
                "touchscreen", false
            )
        );

        // ACT
        ResponseEntity<String> response =
            restTemplate.postForEntity(
                "/api/catalog/products",
                request,
                String.class
            );

        // ASSERT - risposta HTTP
        assertTrue(response.getStatusCode().is2xxSuccessful());

        assertNotNull(response.getBody());

        assertTrue(response.getBody().contains("Lenovo ThinkPad"));
        assertTrue(response.getBody().contains("NOTEBOOK"));
        assertTrue(response.getBody().contains("Lenovo"));

        // ASSERT - persistenza MongoDB
        ProductDocument savedProduct =
            repository.findAll()
                .stream()
                .filter(p -> p.getName().equals("Lenovo ThinkPad"))
                .findFirst()
                .orElseThrow();

        assertNotNull(savedProduct.getId());

        assertEquals("Lenovo ThinkPad", savedProduct.getName());
        assertEquals(new BigDecimal("1299.99"), savedProduct.getPrice());
        assertEquals("NOTEBOOK", savedProduct.getCategory());
        assertEquals("Lenovo", savedProduct.getBrand());
        assertEquals("Intel i7", savedProduct.getTechnicalDetails().get("processor"));
    }
}
