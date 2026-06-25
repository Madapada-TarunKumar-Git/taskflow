FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/taskflow-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 7600

ENTRYPOINT ["java", "-jar", "app.jar"]