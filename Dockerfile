# 构建阶段
FROM maven:3.8.6-openjdk-8-slim AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:8-jre-slim

WORKDIR /app

COPY --from=builder /app/target/token-service-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
