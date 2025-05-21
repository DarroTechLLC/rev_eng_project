package com.darro_tech.revengproject;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import com.darro_tech.revengproject.util.LoggerUtils;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class RevEngProjectApplication {

    private static final Logger logger = LoggerUtils.getLogger(RevEngProjectApplication.class);

    public static void main(String[] args) {
        Instant start = Instant.now();
        LoggerUtils.logStartup(logger, "RevEngProject Application");

        // Load environment variables from .env file
        try {
            logger.info("📦 Loading environment variables from .env file");
            Dotenv dotenv = Dotenv.load();
            logger.debug("🔍 Environment variables loaded: SPRING_DATASOURCE_URL configured as {}", maskSensitiveInfo(dotenv.get("SPRING_DATASOURCE_URL")));

            // Set system properties from .env
            configureSystemProperties(dotenv);
            logger.info("✅ System properties configured from environment variables");
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "loading .env file");
            logger.warn("⚠️ Continuing with default or application.properties configuration");
        }

        // Verify JDBC driver is available
        try {
            logger.info("🔍 Verifying MySQL JDBC Driver availability");
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            LoggerUtils.logException(logger, e, "loading MySQL JDBC Driver");
            logger.error("❌ MySQL JDBC Driver not found! Application may not connect to database");
        }

        logger.debug("📂 Current working directory: {}", System.getProperty("user.dir"));

        // Start the Spring application
        logger.info("🚀 Starting Spring Boot application");
        ConfigurableApplicationContext context = null;
        try {
            context = SpringApplication.run(RevEngProjectApplication.class, args);

            // Log application startup details
            logApplicationStartupDetails(context, start);
        } catch (Exception e) {
            LoggerUtils.logException(logger, e, "Spring Boot application startup");
            logger.error("❌ Application failed to start");
            System.exit(1);
        }
    }

    /**
     * Configure system properties from dotenv
     *
     * @param dotenv The loaded Dotenv instance
     */
    private static void configureSystemProperties(Dotenv dotenv) {
        logger.debug("🔧 Setting system properties from environment variables");

        // Database configuration
        setPropertyIfExists(dotenv, "spring.datasource.url", "SPRING_DATASOURCE_URL");
        setPropertyIfExists(dotenv, "spring.datasource.username", "SPRING_DATASOURCE_USERNAME");
        setPropertyIfExists(dotenv, "spring.datasource.password", "SPRING_DATASOURCE_PASSWORD");
        setPropertyIfExists(dotenv, "spring.datasource.driver-class-name", "SPRING_DATASOURCE_DRIVER_CLASS_NAME");

        // JPA configuration
        setPropertyIfExists(dotenv, "spring.jpa.database", "SPRING_JPA_DATABASE");
        setPropertyIfExists(dotenv, "spring.jpa.show-sql", "SPRING_JPA_SHOW_SQL");
        setPropertyIfExists(dotenv, "spring.jpa.hibernate.ddl-auto", "SPRING_JPA_HIBERNATE_DDL_AUTO");
        setPropertyIfExists(dotenv, "spring.jpa.hibernate.naming.physical-strategy", "SPRING_JPA_HIBERNATE_NAMING_PHYSICAL_STRATEGY");
        setPropertyIfExists(dotenv, "spring.jpa.properties.hibernate.dialect", "SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT");
        setPropertyIfExists(dotenv, "spring.jpa.open-in-view", "SPRING_JPA_OPEN_IN_VIEW");

        // Servlet configuration
        setPropertyIfExists(dotenv, "spring.servlet.multipart.max-file-size", "SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE");
        setPropertyIfExists(dotenv, "spring.servlet.multipart.max-request-size", "SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE");

        // MVC configuration
        setPropertyIfExists(dotenv, "spring.mvc.view.prefix", "SPRING_MVC_VIEW_PREFIX");
        setPropertyIfExists(dotenv, "spring.mvc.view.suffix", "SPRING_MVC_VIEW_SUFFIX");
    }

    /**
     * Helper method to set a system property if the environment variable exists
     *
     * @param dotenv The loaded Dotenv instance
     * @param propertyName The name of the system property
     * @param envVarName The name of the environment variable
     */
    private static void setPropertyIfExists(Dotenv dotenv, String propertyName, String envVarName) {
        String value = dotenv.get(envVarName);
        if (value != null && !value.isEmpty()) {
            System.setProperty(propertyName, value);

            // Avoid logging sensitive information
            if (propertyName.contains("password") || propertyName.contains("username") || propertyName.contains("url")) {
                logger.debug("🔧 Set system property: {}=****", propertyName);
            } else {
                logger.debug("🔧 Set system property: {}={}", propertyName, value);
            }
        } else {
            logger.debug("⚠️ Environment variable {} not found or empty", envVarName);
        }
    }

    /**
     * Log application startup details including URL, profiles, and startup time
     *
     * @param context The Spring application context
     * @param startTime The application start time
     */
    private static void logApplicationStartupDetails(ConfigurableApplicationContext context, Instant startTime) {
        Environment env = context.getEnvironment();
        String protocol = "http";
        if (env.getProperty("server.ssl.key-store") != null) {
            protocol = "https";
        }

        String serverPort = env.getProperty("server.port", "8080");
        String contextPath = env.getProperty("server.servlet.context-path", "/");
        if (!contextPath.startsWith("/")) {
            contextPath = "/" + contextPath;
        }

        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LoggerUtils.logException(logger, e, "resolving host address");
        }

        String[] profiles = env.getActiveProfiles();

        logger.info("✅ Application started successfully");
        logger.info("📊 Application running with profiles: {}", profiles.length > 0 ? String.join(", ", profiles) : "default");
        logger.info("🌐 Application available at:");
        logger.info("   Local:      {}://localhost:{}{}", protocol, serverPort, contextPath);
        logger.info("   External:   {}://{}:{}{}", protocol, hostAddress, serverPort, contextPath);

        Duration startupTime = Duration.between(startTime, Instant.now());
        LoggerUtils.logPerformance(logger, "Application startup", startupTime.toMillis());
    }

    /**
     * Mask sensitive information in URLs or other strings
     *
     * @param input The input string that might contain sensitive information
     * @return A masked version of the input
     */
    private static String maskSensitiveInfo(String input) {
        if (input == null) {
            return null;
        }

        // Mask username and password in database URLs
        // jdbc:mysql://username:password@hostname:port/database
        return input.replaceAll("://([^:]+):([^@]*)@", "://*****:*****@");
    }
}
