# === Build stage ===
FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

# Copy Maven wrapper and pom
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

# Copy source and build the project
COPY src src
RUN ./mvnw -B -DskipTests clean package

# === Run stage ===
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy built jar from first stage
COPY --from=build /app/target/*.jar app.jar

# Render uses $PORT
EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
