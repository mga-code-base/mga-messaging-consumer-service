FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY target/mga-messaging-consumer-service-0.0.1.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=default"]
