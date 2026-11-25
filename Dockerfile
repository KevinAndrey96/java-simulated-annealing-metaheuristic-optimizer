# Stage 1: Build & Package with Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

COPY --from=builder /build/target/simulated-annealing-optimizer.jar ./optimizer.jar

ENTRYPOINT ["java", "-jar", "optimizer.jar"]
CMD ["--benchmark", "linear", "--seed", "42"]
