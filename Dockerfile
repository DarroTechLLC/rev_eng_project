
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

# Add build argument to specify which env file to use (.env, .env.prod, or .env.docker)
ARG ENV_FILE=.env.prod
COPY ${ENV_FILE} .env
EXPOSE 8080

# Environment file can be specified during build with:
# docker build --build-arg ENV_FILE=.env.prod -t app-name .
# Default is .env if not specified

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
