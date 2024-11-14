FROM maven:3.8.4-openjdk-11-slim AS BUILDER
WORKDIR /app
COPY pom.xml .
COPY crawler/src /app/crawler/src
COPY indexer/src /app/indexer/src
COPY query_engine/src /app/query_engine/src
RUN mvn clean install -DskipTests=true

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=BUILDER /app/crawler/target/crawler-1.0-SNAPSHOT.jar .
CMD ["java", "-jar", "./stage-2.jar"]