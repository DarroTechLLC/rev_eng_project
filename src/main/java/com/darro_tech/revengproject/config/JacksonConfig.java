package com.darro_tech.revengproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson configuration for handling serialization of special types like ByteArrayInputStream
 * and proper date/time formatting
 */
@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // Register JavaTimeModule to handle Java 8 date/time types (Instant, LocalDate, etc.)
        objectMapper.registerModule(new JavaTimeModule());

        // Configure to write dates as ISO-8601 strings
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // Disable FAIL_ON_EMPTY_BEANS to prevent errors with ByteArrayInputStream
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return objectMapper;
    }
}
