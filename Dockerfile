# Stage 1: Build the fat JAR with Maven
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /dockerContainerApp
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Extract Spring Boot layers
FROM eclipse-temurin:21-jre-alpine AS extractor
WORKDIR /dockerContainerApp
COPY --from=builder /dockerContainerApp/target/soundboard-0.0.1-SNAPSHOT.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Stage 3: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /dockerContainerApp
COPY --from=extractor /dockerContainerApp/extracted/dependencies/ ./
COPY --from=extractor /dockerContainerApp/extracted/spring-boot-loader/ ./
COPY --from=extractor /dockerContainerApp/extracted/snapshot-dependencies/ ./
COPY --from=extractor /dockerContainerApp/extracted/application/ ./
RUN mkdir -p /dockerContainerApp/SoundAudio
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
