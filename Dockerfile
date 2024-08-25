# Étape 1 : Utiliser une image officielle OpenJDK 17
FROM openjdk:17-jdk-slim

# Étape 2 : Définir le répertoire de travail
WORKDIR /app

# Étape 3 : Copier le fichier .jar généré dans le conteneur
COPY target/*.jar app.jar

# Étape 4 : Exposer le port de l'application
EXPOSE 8080

# Étape 5 : Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]

