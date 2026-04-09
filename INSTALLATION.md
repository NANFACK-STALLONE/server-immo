# 🚀 Guide d'Installation Complet

## 📋 Prérequis

Avant de commencer, assurez-vous que vous avez installés:

- **Java JDK 11+**
  ```bash
  java -version
  # openjdk 11.0.13 ou supérieur
  ```

- **Maven 3.6+**
  ```bash
  mvn -version
  # Apache Maven 3.6.3 ou supérieur
  ```

- **Git**
  ```bash
  git --version
  ```

## 📁 Structure Finale du Projet

```
immobilier-api/
│
├── src/
│   ├── main/
│   │   ├── java/com/immobilier/
│   │   │   ├── ImmobilierApiApplication.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Property.java
│   │   │   │   └── RoleEnum.java
│   │   │   ├── dto/
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── UserDTO.java
│   │   │   │   └── PropertyDTO.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── PropertyRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── PropertyService.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   ├── resource/
│   │   │   │   ├── AuthResource.java
│   │   │   │   ├── UserResource.java
│   │   │   │   └── PropertyResource.java
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtAuthenticationEntryPoint.java
│   │   │   └── exception/
│   │   │       ├── ApiError.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│       └── java/com/immobilier/service/
│           ├── UserServiceTest.java
│           ├── PropertyServiceTest.java
│           ├── AuthServiceTest.java
│           └── JwtTokenProviderTest.java
│
├── pom.xml
├── README.md
├── CURL_EXAMPLES.md
└── INSTALLATION.md (ce fichier)
```

## ⚙️ Étapes d'Installation

### Étape 1: Télécharger et Extraire le Projet

```bash
# Créer un répertoire pour le projet
mkdir -p ~/projects
cd ~/projects

# Cloner ou extraire le projet (si zip)
unzip immobilier-api.zip
cd immobilier-api
```

### Étape 2: Vérifier les Dépendances

```bash
# Vérifier Java
java -version

# Vérifier Maven
mvn -version

# Télécharger les dépendances Maven
mvn dependency:download
```

### Étape 3: Configuration de la Base de Données

#### Option A: H2 (Par défaut - Développement)
Aucune configuration requise! Le fichier `application.properties` est déjà configuré pour H2.

```properties
# Dans application.properties (déjà configuré)
spring.datasource.url=jdbc:h2:mem:immobilierdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

#### Option B: PostgreSQL (Production)

1. **Installer PostgreSQL**
   ```bash
   # Sur macOS
   brew install postgresql

   # Sur Ubuntu
   sudo apt-get install postgresql

   # Sur Windows
   # Télécharger depuis https://www.postgresql.org/download/windows/
   ```

2. **Créer la base de données**
   ```bash
   # Se connecter à PostgreSQL
   psql -U postgres

   # Créer la base de données
   CREATE DATABASE immobilier_db;
   CREATE USER immobilier_user WITH PASSWORD 'immobilier_password';
   ALTER ROLE immobilier_user SET client_encoding TO 'utf8';
   ALTER ROLE immobilier_user SET default_transaction_isolation TO 'read committed';
   ALTER ROLE immobilier_user SET default_transaction_deferrable TO on;
   ALTER ROLE immobilier_user SET default_transaction_read_only TO off;
   GRANT ALL PRIVILEGES ON DATABASE immobilier_db TO immobilier_user;
   \q
   ```

3. **Configurer application.properties**
   ```bash
   # Ouvrir le fichier
   nano src/main/resources/application.properties
   ```

   ```properties
   # Remplacer la configuration H2 par:
   spring.datasource.url=jdbc:postgresql://localhost:5432/immobilier_db
   spring.datasource.username=immobilier_user
   spring.datasource.password=immobilier_password
   spring.datasource.driver-class-name=org.postgresql.Driver

   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQL10Dialect
   spring.jpa.hibernate.ddl-auto=update
   ```

### Étape 4: Configurer la Clé JWT Secrète

1. **Générer une clé sécurisée** (32 caractères minimum)
   ```bash
   # Sur Unix/Linux/macOS
   openssl rand -base64 32

   # Résultat exemple:
   # 7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+
   ```

2. **Mettre à jour application.properties**
   ```bash
   # Remplacer la valeur de jwt.secret par votre clé générée
   jwt.secret=7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+MjNqPsRtUvWxYzAcDeFgHiJkLmNoPqRs
   ```

### Étape 5: Compiler le Projet

```bash
# Nettoyer et compiler
mvn clean compile

# Ou avec tests
mvn clean install
```

### Étape 6: Démarrer l'Application

```bash
# Option 1: Avec Maven
mvn spring-boot:run

# Option 2: Avec Java directement
java -jar target/immobilier-api-1.0.0.jar

# Option 3: À partir du répertoire de compilation
mvn clean install
cd target
java -jar immobilier-api-1.0.0.jar
```

L'application devrait démarrer et afficher:
```
🏠 API Immobilier démarrée avec succès!
```

### Étape 7: Tester l'Installation

```bash
# Ouvrir dans le navigateur
http://localhost:8080/api/auth/health

# Ou avec curl
curl http://localhost:8080/api/auth/health

# Réponse attendue:
# {"status":"UP","message":"API Immobilier est en ligne","timestamp":1705325400000}
```

## 🔧 Configuration Avancée

### Configuration de Logs

Créer ou modifier `src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <property name="LOG_FILE" value="logs/application.log"/>
    
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/application-%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <timeBasedFileNamingAndTriggeringPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
                <maxFileSize>100MB</maxFileSize>
            </timeBasedFileNamingAndTriggeringPolicy>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE"/>
        <appender-ref ref="CONSOLE"/>
    </root>
    
    <logger name="com.immobilier" level="DEBUG"/>
    <logger name="org.springframework.security" level="DEBUG"/>
</configuration>
```

### Variables d'Environnement

Créer un fichier `.env`:

```bash
# .env
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/immobilier_db
SPRING_DATASOURCE_USERNAME=immobilier_user
SPRING_DATASOURCE_PASSWORD=immobilier_password
JWT_SECRET=7x!A%D*G-JaNdRgUkXp2s5v8y/B?E(H+MjNqPsRtUvWxYzAcDeFgHiJkLmNoPqRs
SERVER_PORT=8080
```

Puis charger dans le terminal:
```bash
source .env
mvn spring-boot:run
```

### Configuration Docker (Optionnel)

Créer `Dockerfile`:

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app

COPY target/immobilier-api-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Créer `docker-compose.yml`:

```yaml
version: '3.8'

services:
  db:
    image: postgres:14-alpine
    environment:
      POSTGRES_DB: immobilier_db
      POSTGRES_USER: immobilier_user
      POSTGRES_PASSWORD: immobilier_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  api:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - db
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/immobilier_db
      SPRING_DATASOURCE_USERNAME: immobilier_user
      SPRING_DATASOURCE_PASSWORD: immobilier_password

volumes:
  postgres_data:
```

Démarrer avec Docker:
```bash
docker-compose up --build
```

## 🧪 Exécuter les Tests

```bash
# Tous les tests
mvn test

# Test spécifique
mvn test -Dtest=UserServiceTest

# Avec rapport de couverture
mvn clean test jacoco:report

# Voir le rapport
open target/site/jacoco/index.html
```

## 🐛 Dépannage Courants

### Erreur: "Port 8080 is already in use"
```bash
# Trouver le processus utilisant le port
lsof -i :8080

# Tuer le processus
kill -9 <PID>

# Ou changer le port dans application.properties
server.port=8081
```

### Erreur: "Cannot find symbol"
```bash
# Nettoyer et recompiler
mvn clean compile
mvn clean install
```

### Erreur de Base de Données
```bash
# Vérifier la connexion PostgreSQL
psql -U immobilier_user -d immobilier_db -h localhost

# Vérifier les logs
tail -f target/logs/application.log
```

### Erreur JWT: "token invalid or expired"
- Assurez-vous que la clé secrète JWT est correcte
- Vérifiez que le token n'a pas expiré (24h)
- Utilisez un nouveau token si nécessaire

## 📦 Publier l'Application

### JAR Executable
```bash
mvn clean package
# Le JAR sera dans target/immobilier-api-1.0.0.jar
```

### War pour Tomcat
Modifier `pom.xml`:
```xml
<packaging>war</packaging>
```

```bash
mvn clean package
# Le WAR sera dans target/immobilier-api-1.0.0.war
```

## 🔐 Sécurité en Production

1. **Changer les mots de passe par défaut**
2. **Utiliser HTTPS**
3. **Définir une clé JWT très sécurisée**
4. **Activer le CORS uniquement pour les domaines autorisés**
5. **Mettre à jour les dépendances régulièrement**
6. **Utiliser une base de données sécurisée**
7. **Activer les logs d'audit**

## 📚 Ressources Supplémentaires

- [Documentation Spring Boot](https://spring.io/projects/spring-boot)
- [Documentation JWT](https://jwt.io/)
- [Documentation JPA](https://www.oracle.com/java/technologies/persistence-jsp.html)
- [Maven Guide](https://maven.apache.org/guides/index.html)

## ✅ Vérification de l'Installation

Une fois démarrée, vérifiez que vous pouvez accéder à:

- **API Health**: http://localhost:8080/api/auth/health
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **API Docs**: http://localhost:8080/api/v3/api-docs
- **H2 Console**: http://localhost:8080/api/h2-console

## 🎉 Installation Réussie!

Vous êtes prêt à utiliser l'API Immobilier!

Pour commencer:
1. Consultez `CURL_EXAMPLES.md` pour les exemples d'utilisation
2. Consultez `README.md` pour la documentation complète
3. Utilisez Swagger UI pour tester les endpoints interactivement

Bon développement! 🚀
