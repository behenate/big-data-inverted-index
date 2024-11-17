# Build stage using Maven and OpenJDK
FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /app

# Optimizing build cache by copying pom files and installing dependencies
COPY pom.xml /app/
COPY crawler/pom.xml /app/crawler/
COPY indexer/pom.xml /app/indexer/
COPY query_engine/pom.xml /app/query_engine/

# Copying source code and resources
COPY crawler/src /app/crawler/src
COPY indexer/src /app/indexer/src
COPY query_engine/src /app/query_engine/src
COPY ./src/main/resources /app/src/main/resources

# Building the project without running tests
RUN mvn clean install -DskipTests

# Runtime stage using only JRE
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copying all built artifacts from the builder stage
COPY --from=builder /app/crawler/target/crawler-1.0-SNAPSHOT.jar /app/crawler.jar

# Running the application

CMD ["java", "-jar", "crawler.jar"]

