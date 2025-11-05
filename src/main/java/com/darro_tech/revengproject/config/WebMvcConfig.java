package com.darro_tech.revengproject.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.darro_tech.revengproject.interceptors.CompanyUrlInterceptor;

/**
 * Web MVC configuration for the application
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcConfig.class);
    
    @Autowired
    private CompanyUrlInterceptor companyUrlInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Register the company URL interceptor for all requests
        registry.addInterceptor(companyUrlInterceptor);
        logger.info("🌐 Registered CompanyUrlInterceptor for all requests");
    }
} 