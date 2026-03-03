# Stage 1: Build with Maven (with cache)
FROM maven:3.9.12-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy only pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR
RUN mvn clean package -DskipTests

# Stage 2: Run with Amazon Corretto (final lightweight image)
FROM amazoncorretto:21.0.10-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
