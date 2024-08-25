# Étape 1 : Utiliser une image officielle OpenJDK 17
FROM openjdk:17-jdk-slim AS build

# Étape 2 : Définir le répertoire de travail
WORKDIR /app

# Étape 3 : Copier les fichiers du projet dans le conteneur
COPY . .

# Étape 4 : Construire l'application avec Maven
RUN ./mvnw clean package -DskipTests

# Étape 5 : Utiliser une image plus légère pour exécuter l'application
FROM openjdk:17-jdk-slim

# Étape 6 : Définir le répertoire de travail
WORKDIR /app

# Étape 7 : Copier le fichier JAR généré depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Étape 8 : Exposer le port de l'application
EXPOSE 8080

# Étape 9 : Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
