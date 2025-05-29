package com.darro_tech.revengproject.controllers.advice;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Unified error controller that handles both API and web errors
 */
@Controller
public class ApiErrorController implements ErrorController {

    private static final Logger logger = Logger.getLogger(ApiErrorController.class.getName());

    /**
     * Handle all errors - both API and web
     */
    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, Model model) {
        // Get error details
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        String errorMessage = (String) request.getAttribute("javax.servlet.error.message");
        String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");

        if (statusCode == null) {
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        }

        if (errorMessage == null) {
            errorMessage = "An unexpected error occurred";
        }

        // Check if this is an API request by path or Accept header
        boolean isApiRequest = isApiRequest(request);

        if (isApiRequest) {
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
        } else {
            // Handle web errors
            logger.warning("🌐 Web error occurred for " + requestUri + ": " + statusCode + " - " + errorMessage);

            // Get the exception that caused this error
            Throwable exception = (Throwable) request.getAttribute("javax.servlet.error.exception");

            // Add error details to model
            model.addAttribute("status", statusCode);
            model.addAttribute("error", HttpStatus.valueOf(statusCode).getReasonPhrase());

            // Pass the full exception object to the template for detailed error information
            if (exception != null) {
                model.addAttribute("exception", exception);
                // Use the exception's message if available, otherwise use the generic error message
                String detailedMessage = exception.getMessage() != null ? exception.getMessage() : errorMessage;
                model.addAttribute("message", detailedMessage);
                logger.warning("Exception details: " + exception.toString());
            } else {
                model.addAttribute("message", errorMessage);
            }

            model.addAttribute("path", requestUri);

            // Return the error view
            return "error";
        }
    }

    /**
     * Determine if the request is an API request based on path or Accept header
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");

        return path.startsWith("/api/")
                || (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE));
    }
}
