# Database Configuration Guide

## Overview
This document explains how to configure the database connection for the Rev-Eng-Project application.

## Development Environment

For local development, the application uses the settings in `application-dev.properties`. The default configuration is:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rev_eng_db
spring.datasource.username=root
spring.datasource.password=password
```

### Customizing Development Database

You can modify these values in `application-dev.properties` to match your local MySQL setup.

## Production Environment

For production, the application is configured to use environment variables:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

### Setting Environment Variables

#### On Windows:
```
set SPRING_DATASOURCE_URL=jdbc:mysql://your-production-host:3306/your_db_name
set SPRING_DATASOURCE_USERNAME=your_production_user
set SPRING_DATASOURCE_PASSWORD=your_production_password
```

#### On Linux/Mac:
```
export SPRING_DATASOURCE_URL=jdbc:mysql://your-production-host:3306/your_db_name
export SPRING_DATASOURCE_USERNAME=your_production_user
export SPRING_DATASOURCE_PASSWORD=your_production_password
```

#### Using .env File
If you're using a tool that supports .env files, create a .env file in the project root with:

```
SPRING_DATASOURCE_URL=jdbc:mysql://your-host:3306/your_db_name
SPRING_DATASOURCE_USERNAME=your_user
SPRING_DATASOURCE_PASSWORD=your_password
```

## Troubleshooting

If you encounter the error:
```
Driver com.mysql.cj.jdbc.Driver claims to not accept jdbcUrl, ${SPRING_DATASOURCE_URL}
```

This means the environment variables are not properly set. Either:
1. Set the environment variables as described above
2. Use the development profile which has hardcoded values
3. Modify application.properties to use hardcoded values instead of environment variables