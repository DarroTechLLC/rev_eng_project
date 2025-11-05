package com.darro_tech.revengproject.exceptions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler to ensure consistent error responses
 * across the application.
 */
@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * Handle general exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        logger.severe("💥 Unhandled exception: " + ex.getMessage());
        ex.printStackTrace();

        ApiError error = new ApiError(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred"
        );
        error.addDetail(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Handle CSRF token exceptions
     */
    @ExceptionHandler({InvalidCsrfTokenException.class, MissingCsrfTokenException.class})
    public ResponseEntity<Object> handleCsrfException(Exception ex) {
        logger.warning("🛡️ CSRF token error: " + ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "CSRF token validation failed");
        errorResponse.put("error", "Invalid or missing CSRF token");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * Handle NoHandlerFoundException (404 errors) - when Spring can't find a handler for the request
     */
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            @NonNull NoHandlerFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        String requestPath = ex.getRequestURL();
        String httpMethod = ex.getHttpMethod();

        logger.warning("❌ No handler found for " + httpMethod + " " + requestPath);
        logger.warning("   This indicates a missing or incorrectly mapped endpoint");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "NoHandlerFoundException");
        errorResponse.put("message", "No handler found for " + httpMethod + " " + requestPath);
        errorResponse.put("path", requestPath);
        errorResponse.put("method", httpMethod);
        errorResponse.put("details", "The requested endpoint does not exist or is not properly registered. Check the controller mapping and ensure the application has been restarted after code changes.");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle invalid JSON in request body by properly overriding the method from ResponseEntityExceptionHandler
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex, 
            @NonNull HttpHeaders headers, 
            @NonNull HttpStatusCode status, 
            @NonNull WebRequest request) {

        logger.warning("⚠️ Invalid request format: " + ex.getMessage());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "Invalid request format");
        errorResponse.put("error", "The request body contains invalid JSON or is missing required fields");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}

/**
 * High priority exception handler specific to API controllers
 * to ensure JSON responses for all errors.
 */
@RestControllerAdvice(basePackages = "com.darro_tech.revengproject.controllers.api")
@Order(Ordered.HIGHEST_PRECEDENCE)
class ApiExceptionHandler {

    private static final Logger logger = Logger.getLogger(ApiExceptionHandler.class.getName());

    /**
     * Handle NoHandlerFoundException specifically for API endpoints
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        String requestPath = ex.getRequestURL();
        String httpMethod = ex.getHttpMethod();

        logger.severe("❌ API: No handler found for " + httpMethod + " " + requestPath);
        logger.severe("   This is a routing issue - check:");
        logger.severe("   1. Controller is properly annotated with @RestController");
        logger.severe("   2. @RequestMapping/@GetMapping path matches the request");
        logger.severe("   3. Application has been restarted after code changes");
        logger.severe("   4. Security filter chain is not blocking the request");
        logger.severe("   5. Component scanning includes the controller package");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("status", HttpStatus.NOT_FOUND.value());
        errorResponse.put("error", "NoHandlerFoundException");
        errorResponse.put("message", "API endpoint not found: " + httpMethod + " " + requestPath);
        errorResponse.put("path", requestPath);
        errorResponse.put("method", httpMethod);
        errorResponse.put("troubleshooting", Map.of(
            "check_1", "Verify the controller is annotated with @RestController",
            "check_2", "Verify the @RequestMapping/@GetMapping path matches the request URL",
            "check_3", "Ensure the application has been restarted after code changes",
            "check_4", "Check SecurityConfig to ensure the path is not blocked",
            "check_5", "Verify component scanning includes the controller package"
        ));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle InvalidDefinitionException specifically for ByteArrayInputStream serialization issues
     */
    @ExceptionHandler(InvalidDefinitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDefinitionException(InvalidDefinitionException ex) {
        logger.severe("🔴 API serialization error: " + ex.getMessage());
        ex.printStackTrace();

        // Check if it's a ByteArrayInputStream issue
        if (ex.getMessage() != null && ex.getMessage().contains("ByteArrayInputStream")) {
            logger.info("🔧 Handling ByteArrayInputStream serialization issue");

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "An error occurred processing your request");
            errorResponse.put("error", "Error processing binary data");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

        // For other invalid definition exceptions, use the general handler
        return handleAllExceptions(ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        logger.severe("🔴 API error: " + ex.getMessage());
        ex.printStackTrace();

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "An error occurred processing your request");
        errorResponse.put("error", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 
