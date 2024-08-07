# Use the official maven/Java 8 image to create a build artifact.
FROM maven:3.8.1-openjdk-8 as builder

# Set the working directory in the image
WORKDIR /app

# Copy the pom.xml file to download dependencies
COPY pom.xml .

# Download the dependencies
RUN mvn install

# Copy the rest of the application
COPY src ./src

# Build the application
RUN mvn package

# Use OpenJDK to run the application
FROM openjdk:8-jdk-alpine

# Set the working directory in the image
WORKDIR /app

# Copy the jar file from the builder stage
COPY --from=builder /app/target/*.jar ./app.jar

# Expose the port the app runs on
EXPOSE 9005

# Command to run the application
CMD ["java", "-jar", "app.jar"]
