package com.darro_tech.revengproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.DelegatingRequestMatcherHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public HeaderWriter securityHeadersWriter() {
        StaticHeadersWriter headersWriter = new StaticHeadersWriter(
                "Content-Security-Policy", "default-src 'self'; script-src 'self' 'unsafe-inline' https://code.jquery.com https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://cdn.datatables.net https://code.highcharts.com; style-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://cdnjs.cloudflare.com https://cdn.datatables.net https://cdn.jsdelivr.net; img-src 'self' data: https:; font-src 'self' data: https: https://fonts.gstatic.com; connect-src 'self'",
                "X-Content-Type-Options", "nosniff",
                "X-Frame-Options", "SAMEORIGIN",
                "X-XSS-Protection", "1; mode=block",
                "Referrer-Policy", "strict-origin-when-cross-origin",
                "Permissions-Policy", "geolocation=(), camera=(), microphone=()"
        );

        // Don't apply these headers to API responses
        return new DelegatingRequestMatcherHeaderWriter(
                new NegatedRequestMatcher(new AntPathRequestMatcher("/api/**")),
                headersWriter
        );
    }
}
