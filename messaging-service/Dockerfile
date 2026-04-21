# Utiliser une image de base légère avec Java 17
FROM eclipse-temurin:17-jdk-alpine

# Méta-données
LABEL maintainer="Voom App"

# Créer un répertoire de travail
WORKDIR /app

# Copier le fichier JAR compilé dans le conteneur
COPY target/*.jar app.jar

# Exposer le port du microservice (défini dans ton config-repo)
EXPOSE 8083

# Commande pour démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]