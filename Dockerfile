# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /dockerContainerApp
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run the Application
FROM eclipse-temurin:21-jre-jammy
WORKDIR /dockerContainerApp
COPY --from=builder /dockerContainerApp/target/soundboard-0.0.1-SNAPSHOT.jar soundboard.jar
EXPOSE 8080
# Run the application
ENTRYPOINT [ "java", "-jar", "/dockerContainerApp/soundboard.jar" ]
# remember that everything is being copied within the working directory so always need to cd correctly.
