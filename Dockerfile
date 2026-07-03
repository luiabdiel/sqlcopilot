FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY . .
RUN mvn -Dmaven.test.skip=true clean install

FROM eclipse-temurin:21-jdk-alpine

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]