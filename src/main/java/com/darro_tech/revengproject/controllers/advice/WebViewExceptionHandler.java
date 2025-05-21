package com.darro_tech.revengproject.controllers.advice;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Web view exception handler to provide detailed error information for web pages
 */
@ControllerAdvice(basePackages = "com.darro_tech.revengproject.controllers")
@Order(2)  // Lower priority than the API exception handler
public class WebViewExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(WebViewExceptionHandler.class);

    /**
     * Handle all exceptions and provide detailed information for debugging
     */
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex, HttpServletRequest request, Model model) {
        // Get the request details
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String method = request.getMethod();
        String userAgent = request.getHeader("User-Agent");
        
        // Log the exception with details
        logger.error("🚨 Exception occurred at {}: {}", requestURI, ex.getMessage(), ex);
        
        // Prepare error details for display
        ModelAndView mav = new ModelAndView("error");
        
        // Set basic error information
        mav.addObject("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        mav.addObject("error", ex.getClass().getSimpleName());
        mav.addObject("message", ex.getMessage());
        mav.addObject("path", requestURI);
        
        // Add details for debugging
        Map<String, Object> debugInfo = new HashMap<>();
        debugInfo.put("exception", ex.getClass().getName());
        debugInfo.put("message", ex.getMessage());
        debugInfo.put("requestMethod", method);
        debugInfo.put("requestURI", requestURI);
        
        if (queryString != null) {
            debugInfo.put("queryString", queryString);
        }
        
        // Add stack trace
        StackTraceElement[] stackTrace = ex.getStackTrace();
        StringBuilder stackTraceStr = new StringBuilder();
        int maxStackTraceElements = Math.min(10, stackTrace.length); // Limit to first 10 elements
        
        for (int i = 0; i < maxStackTraceElements; i++) {
            stackTraceStr.append(stackTrace[i].toString()).append("\n");
        }
        
        if (stackTrace.length > maxStackTraceElements) {
            stackTraceStr.append("... ").append(stackTrace.length - maxStackTraceElements).append(" more");
        }
        
        debugInfo.put("stackTrace", stackTraceStr.toString());
        
        // Add the debug info to the model
        mav.addObject("debugInfo", debugInfo);
        
        return mav;
    }
}
