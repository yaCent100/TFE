# Étape 1: Utilisez une image officielle Java (OpenJDK 17 par exemple)
FROM openjdk:17-jdk-slim

# Étape 2: Définissez le répertoire de travail dans le conteneur
WORKDIR /app

# Étape 3: Copiez le fichier .jar généré de votre application dans l'image Docker
COPY target/*.jar app.jar

# Étape 4: Exposez le port sur lequel votre application Spring Boot s'exécute
EXPOSE 8080

# Étape 5: Démarrez l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
