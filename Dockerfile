# Utiliser une image de base JDK 17 (à adapter selon votre version de Java)
FROM openjdk:17-jdk-slim

# Créer un répertoire de travail dans le conteneur
WORKDIR /app

# Copier le fichier JAR généré dans le répertoire de travail du conteneur
COPY target/Driveshare-0.0.1-SNAPSHOT.jar /app/driveshare.jar

# Exposer le port 8080 pour l'application Spring Boot
EXPOSE 8080

# Lancer l'application Spring Boot
ENTRYPOINT ["java", "-jar", "/app/driveshare.jar"]
