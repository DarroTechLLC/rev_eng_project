package com.darro_tech.revengproject.services;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.darro_tech.revengproject.repositories.projections.ForgotPasswordLookup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.darro_tech.revengproject.dto.ForgotPasswordResponse;
import com.darro_tech.revengproject.models.UserContactType;
import com.darro_tech.revengproject.repositories.UserContactInfoRepository;
import com.darro_tech.revengproject.repositories.UserContactTypeRepository;
import com.darro_tech.revengproject.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
public class ForgotPasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserContactInfoRepository userContactInfoRepository;

    @Mock
    private UserContactTypeRepository userContactTypeRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private UserContactType emailType;
    private ForgotPasswordLookup forgotPasswordLookup;

    @BeforeEach
    void setUp() {
        // Setup email type
        emailType = new UserContactType();
        emailType.setId(String.valueOf(2));
        emailType.setName("Email");

        // Setup forgot password lookup result
        forgotPasswordLookup = new ForgotPasswordLookup() {
            @Override
            public String getId() {
                return "test-user-id-123";
            }

            @Override
            public String getUsername() {
                return "testuser";
            }

            @Override
            public String getEmail() {
                return "test@example.com";
            }
        };
    }

    @Test
    void testProcessForgotPasswordRequestByEmail_Success() {
        // Given
        String email = "test@example.com";

        when(userContactTypeRepository.findByName("Email")).thenReturn(emailType);
        when(userRepository.findForgotPasswordByUsername(eq(email), eq(emailType))).thenReturn(Optional.empty());
        when(userContactInfoRepository.findForgotPasswordByEmail(eq(emailType), eq(email))).thenReturn(Optional.of(forgotPasswordLookup));
        when(emailService.sendForgotPasswordEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(email);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNull(response.getError());

        // Verify interactions
        verify(userContactTypeRepository).findByName("Email");
        verify(userRepository).findForgotPasswordByUsername(eq(email), eq(emailType));
        verify(userContactInfoRepository).findForgotPasswordByEmail(eq(emailType), eq(email));
        verify(userRepository).updateResetToken(eq("test-user-id-123"), anyString(), any(Instant.class));
        verify(emailService).sendForgotPasswordEmail(eq("test@example.com"), eq("testuser"), anyString());
    }

    @Test
    void testProcessForgotPasswordRequestByUsername_Success() {
        // Given
        String username = "testuser";

        when(userContactTypeRepository.findByName("Email")).thenReturn(emailType);
        when(userRepository.findForgotPasswordByUsername(eq(username), eq(emailType))).thenReturn(Optional.of(forgotPasswordLookup));
        when(emailService.sendForgotPasswordEmail(anyString(), anyString(), anyString())).thenReturn(true);

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(username);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNull(response.getError());

        // Verify interactions
        verify(userContactTypeRepository).findByName("Email");
        verify(userRepository).findForgotPasswordByUsername(eq(username), eq(emailType));
        verify(userContactInfoRepository, never()).findForgotPasswordByEmail(any(), any());
        verify(userRepository).updateResetToken(eq("test-user-id-123"), anyString(), any(Instant.class));
        verify(emailService).sendForgotPasswordEmail(eq("test@example.com"), eq("testuser"), anyString());
    }

    @Test
    void testProcessForgotPasswordRequest_EmailNotFound() {
        // Given
        String email = "nonexistent@example.com";

        when(userContactTypeRepository.findByName("Email")).thenReturn(emailType);
        when(userRepository.findForgotPasswordByUsername(eq(email), eq(emailType))).thenReturn(Optional.empty());
        when(userContactInfoRepository.findForgotPasswordByEmail(eq(emailType), eq(email))).thenReturn(Optional.empty());

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(email);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess()); // Security-safe response
        assertNull(response.getError());

        // Verify interactions
        verify(userContactTypeRepository).findByName("Email");
        verify(userRepository).findForgotPasswordByUsername(eq(email), eq(emailType));
        verify(userContactInfoRepository).findForgotPasswordByEmail(eq(emailType), eq(email));
        verify(userRepository, never()).updateResetToken(any(), any(), any());
        verify(emailService, never()).sendForgotPasswordEmail(any(), any(), any());
    }

    @Test
    void testProcessForgotPasswordRequest_EmailTypeNotFound() {
        // Given
        String email = "test@example.com";

        when(userContactTypeRepository.findByName("Email")).thenReturn(null);

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(email);

        // Then
        assertNotNull(response);
        assertTrue(response.isSuccess()); // Security-safe response
        assertNull(response.getError());

        // Verify interactions
        verify(userContactTypeRepository).findByName("Email");
        verify(userRepository, never()).findForgotPasswordByUsername(any(), any());
        verify(userContactInfoRepository, never()).findForgotPasswordByEmail(any(), any());
        verify(userRepository, never()).updateResetToken(any(), any(), any());
        verify(emailService, never()).sendForgotPasswordEmail(any(), any(), any());
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

        // Verify no interactions with repositories
        verify(userContactTypeRepository, never()).findByName(any());
        verify(userRepository, never()).findForgotPasswordByUsername(any(), any());
        verify(userContactInfoRepository, never()).findForgotPasswordByEmail(any(), any());
        verify(userRepository, never()).updateResetToken(any(), any(), any());
        verify(emailService, never()).sendForgotPasswordEmail(any(), any(), any());
    }

    @Test
    void testProcessForgotPasswordRequest_EmailSendFailure() {
        // Given
        String email = "test@example.com";

        when(userContactTypeRepository.findByName("Email")).thenReturn(emailType);
        when(userRepository.findForgotPasswordByUsername(eq(email), eq(emailType))).thenReturn(Optional.empty());
        when(userContactInfoRepository.findForgotPasswordByEmail(eq(emailType), eq(email))).thenReturn(Optional.of(forgotPasswordLookup));
        when(emailService.sendForgotPasswordEmail(anyString(), anyString(), anyString())).thenReturn(false);

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(email);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("send-email", response.getError());

        // Verify interactions
        verify(userContactTypeRepository).findByName("Email");
        verify(userRepository).findForgotPasswordByUsername(eq(email), eq(emailType));
        verify(userContactInfoRepository).findForgotPasswordByEmail(eq(emailType), eq(email));
        verify(userRepository).updateResetToken(eq("test-user-id-123"), anyString(), any(Instant.class));
        verify(emailService).sendForgotPasswordEmail(eq("test@example.com"), eq("testuser"), anyString());
    }

    @Test
    void testProcessForgotPasswordRequest_UserWithNoEmail() {
        // Given
        String email = "test@example.com";

        // Create a lookup result with no email
        ForgotPasswordLookup lookupWithoutEmail = new ForgotPasswordLookup() {
            @Override
            public String getId() {
                return "test-user-id-123";
            }

            @Override
            public String getUsername() {
                return "testuser";
            }

            @Override
            public String getEmail() {
                return null; // No email
            }
        };

        when(userContactTypeRepository.findByName("Email")).thenReturn(emailType);
        when(userRepository.findForgotPasswordByUsername(eq(email), eq(emailType))).thenReturn(Optional.empty());
        when(userContactInfoRepository.findForgotPasswordByEmail(eq(emailType), eq(email))).thenReturn(Optional.of(lookupWithoutEmail));

        // When
        ForgotPasswordResponse response = forgotPasswordService.processForgotPasswordRequest(email);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertEquals("no-email", response.getError());

        // Verify interactions
        verify(userContactTypeRepository).findByName("Email");
        verify(userRepository).findForgotPasswordByUsername(eq(email), eq(emailType));
        verify(userContactInfoRepository).findForgotPasswordByEmail(eq(emailType), eq(email));
        verify(userRepository, never()).updateResetToken(any(), any(), any());
        verify(emailService, never()).sendForgotPasswordEmail(any(), any(), any());
    }
}

