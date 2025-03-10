FROM openjdk:21-jdk
WORKDIR /app
COPY target/iPeyu-Backend-admin-1.2.0.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
