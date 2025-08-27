# ===== Build stage =====
FROM maven:3.9.9-eclipse-temurin-22 AS build
WORKDIR /src
COPY . .
RUN mvn -q -DskipTests package

# ===== Runtime stage =====
FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=build /src/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
