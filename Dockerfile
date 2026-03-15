# ./Dockerfile
# multi-stage build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jdk-jammy
ARG JAR_FILE=/workspace/target/ecommerce-backend-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} /app/app.jar
EXPOSE 8080
# create non-root user and run as it
RUN addgroup --system app && adduser --system --ingroup app app
USER app
ENTRYPOINT ["java","-jar","/app/app.jar"]