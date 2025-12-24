# Build stage
FROM eclipse-temurin:17-jdk-alpine as builder
WORKDIR /app

# Copy maven wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the application
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S spring && adduser -u 1001 -S spring -G spring

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring

EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
