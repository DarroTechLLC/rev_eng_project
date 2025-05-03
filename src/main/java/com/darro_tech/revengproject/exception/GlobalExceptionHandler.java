package com.darro_tech.revengproject.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler to ensure consistent error responses
 * across the application.
 */
@ControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = Logger.getLogger(GlobalExceptionHandler.class.getName());

    /**
     * Handle HttpMessageNotReadableException properly by overriding the method from ResponseEntityExceptionHandler
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, 
            HttpHeaders headers, 
            HttpStatusCode status, 
            WebRequest request) {
        
        logger.warning("🔴 Error parsing JSON request: " + ex.getMessage());
        
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Malformed JSON request",
                ex.getMessage());
        
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle CSRF token exceptions
     */
    @ExceptionHandler({InvalidCsrfTokenException.class, MissingCsrfTokenException.class})
    public ResponseEntity<Object> handleCsrfExceptions(Exception ex) {
        logger.warning("🔒 CSRF token error: " + ex.getMessage());
        
        ApiError apiError = new ApiError(
                HttpStatus.FORBIDDEN.value(),
                "CSRF Token Error",
                "Invalid or missing CSRF token");
        
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        logger.severe("🔴 Unhandled exception: " + ex.getMessage());
        ex.printStackTrace();
        
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Server Error",
                "An unexpected error occurred");
        
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
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