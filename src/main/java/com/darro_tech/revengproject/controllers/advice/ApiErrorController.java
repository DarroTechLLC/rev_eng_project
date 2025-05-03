package com.darro_tech.revengproject.controllers.advice;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller to handle error responses for API endpoints
 * This ensures that error responses are returned as JSON for API paths
 */
@RestController
public class ApiErrorController implements ErrorController {

    private static final Logger logger = Logger.getLogger(ApiErrorController.class.getName());

    /**
     * Handle errors for API endpoints
     */
    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> handleError(HttpServletRequest request) {
        // Get error status
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        
        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }
        
        if (errorMessage == null) {
            errorMessage = "An unexpected error occurred";
        }
        
        // Check if this is an API request
        if (requestUri != null && requestUri.startsWith("/api/")) {
            logger.warning("🔴 API error occurred for " + requestUri + ": " + statusCode + " - " + errorMessage);
            
            // Create JSON error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("status", statusCode);
            errorResponse.put("message", errorMessage);
            errorResponse.put("path", requestUri);
            
            return ResponseEntity
                    .status(statusCode)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
        
        // For non-API requests, return null to let the default error handler take over
        return null;
    }
} 