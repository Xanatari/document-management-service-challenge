FROM openjdk:17-jdk
VOLUME /tmp
COPY target/document-management-service.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xmx50m", "-jar", "/app.jar"]