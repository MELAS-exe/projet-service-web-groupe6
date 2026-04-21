# Utiliser une image de base légère avec Java 17
FROM eclipse-temurin:17-jdk-alpine

# Méta-données
LABEL maintainer="voom-team"
LABEL version="1.0"

# Répertoire de travail
WORKDIR /app

# Copier le JAR généré
COPY target/*.jar app.jar

# Exposer le port
EXPOSE 8084

# Commande pour démarrer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]