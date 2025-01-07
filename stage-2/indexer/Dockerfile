# Build stage using Maven and OpenJDK
FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /app

# Update and install required packages
RUN apt-get update && \
    apt-get install -y curl unzip && \
    rm -rf /var/lib/apt/lists/*

# Install specific version of protobuf
RUN curl -L -o /tmp/protoc-29.2-linux-x86_64.zip https://github.com/protocolbuffers/protobuf/releases/download/v29.2/protoc-29.2-linux-x86_64.zip && \
    unzip /tmp/protoc-29.2-linux-x86_64.zip -d /usr/local && \
    rm /tmp/protoc-29.2-linux-x86_64.zip




# Optimizing build cache by copying pom files and installing dependencies
COPY pom.xml /app/
COPY crawler/pom.xml /app/crawler/
COPY indexer/pom.xml /app/indexer/
COPY query_engine/pom.xml /app/query_engine/

# Copying source code and resources
COPY crawler/src /app/crawler/src
COPY indexer/src /app/indexer/src
COPY common-proto/ /app/common-proto
COPY test-runs/ /app/test-runs
COPY query_engine/src /app/query_engine/src
COPY ./src/main/resources /app/src/main/resources

# Building the project without running tests
RUN mvn compile
RUN mvn clean install -DskipTests

# Runtime stage using only JRE
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copying all built artifacts from the builder stage
COPY --from=builder /app/indexer/target/indexer-1.0-SNAPSHOT.jar /app/indexer.jar

# Running the application

CMD ["java", "-jar", "indexer.jar"]

