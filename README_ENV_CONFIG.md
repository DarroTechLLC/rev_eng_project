# Environment Configuration Guide

This guide explains how the application handles environment variables and how to configure it for different environments.

## Overview

The application is designed to run the same way in different environments (local development, Docker, production) without requiring environment-specific configuration in Docker or other deployment platforms. It achieves this by:

1. Loading environment variables from a `.env` file if it exists
2. Falling back to system environment variables if they're not found in the `.env` file
3. Using default values in `application.properties` if neither of the above are available

## Local Development

For local development:

1. Copy `.env.example` to `.env` and update the values for your environment
2. Run the application normally

```bash
cp .env.example .env
# Edit .env with your values
./gradlew bootRun
```

## Docker Deployment

For Docker deployment:

1. The Dockerfile is configured to copy `.env.prod` as `.env` in the container
2. No need to pass environment variables to Docker using `--env-file` or `-e`
3. Update `.env.prod` with production-specific values

```bash
# Build and run with Docker Compose
docker-compose up --build
```

## Production Deployment (e.g., Render)

For production deployment on platforms like Render:

1. Set environment variables in the platform's dashboard
2. The application will use these system environment variables automatically
3. No need for a `.env` file in production

## How It Works

The application loads environment variables in the following order of precedence:

1. `.env` file (if it exists)
2. System environment variables (if the variable is not found in `.env`)
3. Default values in `application.properties` (if the variable is not found in either of the above)

This approach ensures that:

- The application runs consistently across different environments
- Sensitive information can be kept out of version control
- Docker doesn't need to know about environment variables
- The application is resilient to missing configuration

## Files

- `.env`: Local development environment variables (not committed to version control)
- `.env.prod`: Production environment variables for Docker (committed to version control)
- `.env.example`: Example environment variables with dummy values (committed to version control)
- `application.properties`: Default values for environment variables

## Implementation Details

The application uses the following approach to load environment variables:

```java
// Load environment variables from .env file
Dotenv dotenv = Dotenv.configure()
    .directory(System.getProperty("user.dir"))
    .filename(".env")
    .ignoreIfMissing()  // Don't fail if .env is missing
    .load();

// First try to get from .env
String value = dotenv.get("ENV_VAR_NAME");

// If not in .env, try system environment variables
if (value == null || value.isEmpty()) {
    value = System.getenv("ENV_VAR_NAME");
}

// Use the value if found
if (value != null && !value.isEmpty()) {
    System.setProperty("property.name", value);
}
```

In `application.properties`, default values are provided using the Spring placeholder syntax:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/rev_eng?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:root}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
```

This ensures that the application can run even if some environment variables are not set.