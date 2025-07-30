package com.darro_tech.revengproject.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration class to enable Spring Retry functionality.
 * This allows the use of @Retryable annotations throughout the application.
 */
@Configuration
@EnableRetry
public class RetryConfig {
    // No additional configuration needed - @EnableRetry does all the work
}