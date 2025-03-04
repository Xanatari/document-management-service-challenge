# Etapa 1: Compilación con Maven
FROM maven:3.8.6-openjdk-17 AS build
WORKDIR /app
# Copiar el pom.xml y descargar las dependencias (aprovechando la caché)
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copiar el código fuente y compilar el proyecto
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución
FROM openjdk:17-jdk-alpine
WORKDIR /app
# Copiar el JAR construido en la etapa anterior
COPY --from=build /app/target/document-management-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xmx50m", "-jar", "app.jar"]
