FROM ubuntu:latest as build
WORKDIR /app
RUN apt-get update
RUN apt-get install openjdk-17-jdk -y
COPY . .
RUN chmod +x ./gradlew  # Add execute permission
RUN ./gradlew bootJar --no-daemon

# Final Stage
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
EXPOSE 8080
COPY --from=build /app/build/libs/*.jar app.jar
# Copy production .env file into container
COPY .env.prod .env

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
