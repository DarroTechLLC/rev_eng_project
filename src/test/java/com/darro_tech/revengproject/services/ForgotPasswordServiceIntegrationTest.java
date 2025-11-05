package com.darro_tech.revengproject.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.darro_tech.revengproject.dto.ForgotPasswordResponse;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ForgotPasswordServiceIntegrationTest {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @Test
    void testProcessForgotPasswordRequestByEmail_Success() {
        // Given
        String email = "test@example.com";

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(email);

        // Then
        assertNotNull(response);
        // The response should be success=true (security-safe response) even if user doesn't exist
        assertTrue(response.isSuccess());
    }

    @Test
    void testProcessForgotPasswordRequestByUsername_Success() {
        // Given
        String username = "testuser";

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(username);

        // Then
        assertNotNull(response);
        // The response should be success=true (security-safe response) even if user doesn't exist
        assertTrue(response.isSuccess());
    }

    @Test
    void testProcessForgotPasswordRequest_EmptyInput() {
        // Given
        String emptyInput = "";

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(emptyInput);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNull(response.getError());
    }

    @Test
    void testProcessForgotPasswordRequest_NullInput() {
        // Given
        String nullInput = null;

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(nullInput);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNull(response.getError());
    }
}

