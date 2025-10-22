package com.learning.catalog_service;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {
            // Optional config server for safety
            "spring.cloud.config.enabled=false",
            "spring.config.import=optional:configserver:http://localhost:8888",
            "spring.cloud.config.fail-fast=false"
        })
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
public abstract class AbstractIT {

    @LocalServerPort
    int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }
}
