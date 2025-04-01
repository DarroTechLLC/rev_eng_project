package com.darro_tech.revengproject;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class RevEngProjectApplication {
    private static final Logger logger = LoggerFactory.getLogger(RevEngProjectApplication.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        System.out.println("📦 Dotenv SPRING_DATASOURCE_URL: " + dotenv.get("SPRING_DATASOURCE_URL"));

        // Set system properties (or use another strategy to integrate with Spring)
        System.setProperty("spring.datasource.url", dotenv.get("SPRING_DATASOURCE_URL"));
        System.setProperty("spring.datasource.username", dotenv.get("SPRING_DATASOURCE_USERNAME"));
        System.setProperty("spring.datasource.password", dotenv.get("SPRING_DATASOURCE_PASSWORD"));
        System.setProperty("spring.datasource.driver-class-name", dotenv.get("SPRING_DATASOURCE_DRIVER_CLASS_NAME"));
        System.setProperty("spring.jpa.database", dotenv.get("SPRING_JPA_DATABASE"));
        System.setProperty("spring.jpa.show-sql", dotenv.get("SPRING_JPA_SHOW_SQL"));
        System.setProperty("spring.jpa.hibernate.ddl-auto", dotenv.get("SPRING_JPA_HIBERNATE_DDL_AUTO"));
        System.setProperty("spring.jpa.hibernate.naming.physical-strategy", dotenv.get("SPRING_JPA_HIBERNATE_NAMING_PHYSICAL_STRATEGY"));
        System.setProperty("spring.jpa.properties.hibernate.dialect", dotenv.get("SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT"));
        System.setProperty("spring.jpa.open-in-view", dotenv.get("SPRING_JPA_OPEN_IN_VIEW"));
        System.setProperty("spring.servlet.multipart.max-file-size", dotenv.get("SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE"));
        System.setProperty("spring.servlet.multipart.max-request-size", dotenv.get("SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE"));
        System.setProperty("spring.mvc.view.prefix", dotenv.get("SPRING_MVC_VIEW_PREFIX"));
        System.setProperty("spring.mvc.view.suffix", dotenv.get("SPRING_MVC_VIEW_SUFFIX"));

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            logger.info("✅ MySQL JDBC Driver Loaded Successfully!");
        } catch (ClassNotFoundException e) {
            logger.error("❌ MySQL JDBC Driver Not Found!", e);
        }
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        SpringApplication.run(RevEngProjectApplication.class, args);
    }
}