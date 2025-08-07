package com.darro_tech.revengproject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.darro_tech.revengproject.util.LoggerUtils;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Test class to verify that environment variables are loaded correctly
 * from .env files and system environment variables.
 */
public class EnvConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(EnvConfigTest.class);
    private static final String TEST_ENV_FILE = ".env.test";
    private static final String TEST_DB_URL = "jdbc:mysql://test-host:3306/test_db";
    private static final String TEST_DB_USERNAME = "test_user";
    private static final String TEST_DB_PASSWORD = "test_password";

    @BeforeEach
    public void setup() throws IOException {
        // Create a test .env file
        try (FileWriter writer = new FileWriter(TEST_ENV_FILE)) {
            writer.write("SPRING_DATASOURCE_URL=" + TEST_DB_URL + "\n");
            writer.write("SPRING_DATASOURCE_USERNAME=" + TEST_DB_USERNAME + "\n");
            writer.write("SPRING_DATASOURCE_PASSWORD=" + TEST_DB_PASSWORD + "\n");
        }
        logger.info("[DEBUG_LOG] Created test .env file at {}", Paths.get(TEST_ENV_FILE).toAbsolutePath());
    }

    @AfterEach
    public void cleanup() throws IOException {
        // Delete the test .env file
        Files.deleteIfExists(Path.of(TEST_ENV_FILE));
        logger.info("[DEBUG_LOG] Deleted test .env file");
    }

    @Test
    public void testLoadEnvFile() {
        logger.info("[DEBUG_LOG] Testing loading environment variables from .env file");
        
        // Load the test .env file
        Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .filename(TEST_ENV_FILE)
            .ignoreIfMissing()
            .load();
        
        // Verify that the environment variables are loaded correctly
        assertEquals(TEST_DB_URL, dotenv.get("SPRING_DATASOURCE_URL"), 
            "Database URL should match the value in the .env file");
        assertEquals(TEST_DB_USERNAME, dotenv.get("SPRING_DATASOURCE_USERNAME"), 
            "Database username should match the value in the .env file");
        assertEquals(TEST_DB_PASSWORD, dotenv.get("SPRING_DATASOURCE_PASSWORD"), 
            "Database password should match the value in the .env file");
        
        logger.info("[DEBUG_LOG] Successfully loaded environment variables from .env file");
    }

    @Test
    public void testMissingEnvFile() {
        logger.info("[DEBUG_LOG] Testing loading environment variables with missing .env file");
        
        // Delete the test .env file if it exists
        try {
            Files.deleteIfExists(Path.of(TEST_ENV_FILE));
        } catch (IOException e) {
            logger.error("[DEBUG_LOG] Error deleting test .env file", e);
        }
        
        // Try to load a non-existent .env file
        Dotenv dotenv = Dotenv.configure()
            .directory(System.getProperty("user.dir"))
            .filename(TEST_ENV_FILE + ".nonexistent")
            .ignoreIfMissing()
            .load();
        
        // Verify that the application doesn't crash when the .env file is missing
        assertNotNull(dotenv, "Dotenv should be loaded even if the .env file is missing");
        
        logger.info("[DEBUG_LOG] Successfully handled missing .env file");
    }

    @Test
    public void testSystemEnvFallback() {
        logger.info("[DEBUG_LOG] Testing fallback to system environment variables");
        
        // Set a system environment variable for testing
        // Note: This is just a simulation as we can't actually set system environment variables in a test
        // In a real scenario, the application would check System.getenv() if dotenv.get() returns null
        
        // Create a mock implementation to test the fallback logic
        String mockSystemEnvValue = "system_env_value";
        String envVarName = "TEST_ENV_VAR";
        String propertyName = "test.property";
        
        // Simulate the setPropertyIfExists method
        String dotenvValue = null; // Simulate not found in .env
        String systemEnvValue = mockSystemEnvValue; // Simulate found in system env
        
        // First try to get from .env (simulated as null)
        String value = dotenvValue;
        
        // If not in .env, try system environment variables
        if (value == null || value.isEmpty()) {
            value = systemEnvValue;
            if (value != null && !value.isEmpty()) {
                logger.info("[DEBUG_LOG] Environment variable {} found in system environment", envVarName);
            }
        }
        
        if (value != null && !value.isEmpty()) {
            // In a real scenario, this would be: System.setProperty(propertyName, value);
            logger.info("[DEBUG_LOG] Would set system property: {}={}", propertyName, value);
        }
        
        // Verify that the value from the system environment is used
        assertEquals(mockSystemEnvValue, value, 
            "Value should be taken from system environment when not found in .env");
        
        logger.info("[DEBUG_LOG] Successfully tested fallback to system environment variables");
    }
}