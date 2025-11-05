package com.darro_tech.revengproject.services;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class SimpleForgotPasswordTest {

    @Test
    void testContextLoads() {
        // Simple test to verify Spring context loads
        assertTrue(true);
    }
}

