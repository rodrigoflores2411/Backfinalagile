# Etapa 1: construir el proyecto
FROM maven:3.8.6-amazoncorretto-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa 2: ejecutar el proyecto
FROM amazoncorretto:17
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Expone el puerto por defecto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
