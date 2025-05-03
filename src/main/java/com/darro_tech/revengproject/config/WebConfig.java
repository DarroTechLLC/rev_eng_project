package com.darro_tech.revengproject.config;

import java.util.List;
import java.util.logging.Logger;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for content negotiation and message converters
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = Logger.getLogger(WebConfig.class.getName());

    /**
     * Configure content negotiation to prefer JSON for API endpoints
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        logger.info("🔧 Configuring content negotiation to prefer JSON for API endpoints");
        
        configurer
            .favorParameter(false)
            .favorPathExtension(false)
            .ignoreAcceptHeader(false)
            .defaultContentType(MediaType.APPLICATION_JSON);
    }
    
    /**
     * Configure message converters to ensure JSON is properly handled
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        logger.info("🔧 Configuring message converters for JSON support");
        
        // Add Jackson converter at the beginning to ensure it's used preferentially
        converters.add(0, new MappingJackson2HttpMessageConverter());
    }
} 