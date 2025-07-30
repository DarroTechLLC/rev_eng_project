
# Build stage
FROM openjdk:17-jdk-slim as build
WORKDIR /app
COPY . .
RUN chmod +x ./gradlew && \
    ./gradlew bootJar --no-daemon

# Final stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/rev-eng-project-0.0.1-SNAPSHOT.jar app.jar
COPY .env.docker .env
EXPOSE 8080

# Using Docker-specific .env file with appropriate settings
# No hardcoded environment variables for better security and flexibility

# Add health check to ensure database is available before starting the application
# Give the application time to start up (30s) and check every 10s with a 5s timeout
# Retry up to 5 times before considering the container unhealthy
HEALTHCHECK --start-period=30s --interval=10s --timeout=5s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application as an executable JAR
# Set connection pool settings to improve reliability
ENTRYPOINT ["java", \
  "-Dspring.datasource.hikari.connection-timeout=30000", \
  "-Dspring.datasource.hikari.maximum-pool-size=10", \
  "-Dspring.datasource.hikari.minimum-idle=5", \
  "-Dspring.datasource.hikari.idle-timeout=300000", \
  "-Dspring.datasource.hikari.max-lifetime=1200000", \
  "-jar", "app.jar"]
