# Étape 1 : Utiliser une image officielle OpenJDK 17
FROM openjdk:17-jdk-slim AS build

# Étape 2 : Définir le répertoire de travail
WORKDIR /app

# Étape 3 : Copier les fichiers du projet dans le conteneur
COPY . .

# Étape 4 : Donner les permissions d'exécution au fichier mvnw
RUN chmod +x ./mvnw

# Étape 5 : Construire l'application avec Maven
RUN ./mvnw clean package -DskipTests

# Étape 6 : Utiliser une image plus légère pour exécuter l'application
FROM openjdk:17-jdk-slim

# Étape 7 : Définir le répertoire de travail
WORKDIR /app

# Étape 8 : Copier le fichier JAR généré depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Étape 9 : Exposer le port de l'application
EXPOSE 8080

# Étape 10 : Démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
