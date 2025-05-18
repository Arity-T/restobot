FROM eclipse-temurin:23-jre

WORKDIR /app

# Copy the fat JAR from the build stage
COPY ./app/build/libs/app-fat.jar app-fat.jar

# Run the application
CMD ["java", "-jar", "app-fat.jar"] 