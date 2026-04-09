# 🏠 API REST Immobilier

API REST complète pour la gestion immobilière avec authentification JWT, contrôle d'accès basé sur les rôles (RBAC), et gestion des propriétés.

## 📋 Caractéristiques

- ✅ **Authentification JWT** avec tokens d'accès et refresh tokens
- ✅ **Contrôle d'accès basé sur les rôles (RBAC)** - ADMIN, AGENT, BUYER, SELLER, USER
- ✅ **Gestion complète des utilisateurs** (CRUD, changement de mot de passe, activation/désactivation)
- ✅ **Gestion des propriétés immobilières** (CRUD, recherche avancée, filtrage)
- ✅ **Validation des données** avec JSR-303
- ✅ **Gestion d'erreurs globale**
- ✅ **Tests unitaires complets** avec JUnit 5 et Mockito
- ✅ **Documentation API** avec Swagger/OpenAPI
- ✅ **Base de données JPA/Hibernate**

## 🛠️ Stack Technologique

- **Framework**: Spring Boot 2.7.14
- **Authentification**: JWT (JJWT 0.11.5)
- **Sécurité**: Spring Security
- **REST API**: JAX-RS (Jersey)
- **ORM**: Hibernate/JPA
- **Base de données**: PostgreSQL / H2 (développement)
- **Validation**: JSR-303
- **Documentation**: Springdoc OpenAPI
- **Tests**: JUnit 5, Mockito
- **Build**: Maven

## 📁 Structure du Projet

```
immobilier-api/
├── src/main/java/com/immobilier/
│   ├── ImmobilierApiApplication.java
│   ├── config/
│   │   └── SecurityConfig.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Property.java
│   │   └── RoleEnum.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── UserDTO.java
│   │   └── PropertyDTO.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   └── PropertyRepository.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── PropertyService.java
│   │   └── CustomUserDetailsService.java
│   ├── resource/
│   │   ├── AuthResource.java
│   │   ├── UserResource.java
│   │   └── PropertyResource.java
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── JwtAuthenticationEntryPoint.java
│   └── exception/
│       ├── ApiError.java
│       └── GlobalExceptionHandler.java
├── src/main/resources/
│   └── application.properties
├── src/test/java/com/immobilier/
│   └── service/
│       ├── UserServiceTest.java
│       ├── PropertyServiceTest.java
│       └── AuthServiceTest.java
└── pom.xml
```

## 🚀 Installation et Démarrage

### Prérequis

- Java 11 ou supérieur
- Maven 3.6+
- PostgreSQL (optionnel - H2 utilisé par défaut)

### Étapes d'installation

1. **Cloner le projet**
   ```bash
   git clone <repository-url>
   cd immobilier-api
   ```

2. **Configurer la base de données**
   
   Par défaut, le projet utilise H2 (en mémoire). Pour PostgreSQL, modifiez `application.properties`:
   
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/immobilier_db
   spring.datasource.username=postgres
   spring.datasource.password=votre_mot_de_passe
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Générer la clé JWT secrète**
   
   Générez une clé sécurisée et mettez-la à jour dans `application.properties`:
   ```properties
   jwt.secret=votre_clé_secrète_min_32_caractères_très_sécurisée!@#$
   ```

4. **Compiler et démarrer l'application**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **Accéder à l'API**
   ```
   http://localhost:8080/api
   Documentation Swagger: http://localhost:8080/api/swagger-ui.html
   Console H2: http://localhost:8080/api/h2-console
   ```

## 📚 API Endpoints

### 🔐 Authentication Endpoints (`/api/auth`)

#### 1. Connexion
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}

Response 200:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "user@example.com",
    "fullName": "Test User",
    "role": "ROLE_USER"
  }
}
```

#### 2. Enregistrement
```http
POST /api/auth/register?username=newuser&email=new@example.com&password=pass123&fullName=New User

Response 201:
{
  "message": "Enregistrement réussi",
  "userId": 2,
  "username": "newuser",
  "email": "new@example.com"
}
```

#### 3. Rafraîchir le token
```http
POST /api/auth/refresh?refreshToken=eyJhbGc...

Response 200:
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

#### 4. Valider le token
```http
GET /api/auth/validate
Authorization: Bearer eyJhbGc...

Response 200:
{
  "valid": true,
  "message": "Token valide"
}
```

### 👤 Users Endpoints (`/api/users`)

#### 1. Obtenir le profil actuel
```http
GET /api/users/profile
Authorization: Bearer eyJhbGc...

Response 200:
{
  "id": 1,
  "username": "testuser",
  "email": "test@example.com",
  "fullName": "Test User",
  "phone": "1234567890",
  "address": "123 Main St",
  "role": "ROLE_USER",
  "createdAt": "2024-01-15T10:30:00",
  "updatedAt": "2024-01-15T10:30:00",
  "isActive": true
}
```

#### 2. Obtenir un utilisateur par ID
```http
GET /api/users/{id}
Authorization: Bearer eyJhbGc...
```

#### 3. Obtenir tous les utilisateurs (ADMIN)
```http
GET /api/users
Authorization: Bearer eyJhbGc...
```

#### 4. Mettre à jour le profil
```http
PUT /api/users/profile
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "fullName": "Updated Name",
  "phone": "9876543210",
  "address": "456 New Ave"
}
```

#### 5. Changer le mot de passe
```http
POST /api/users/change-password?oldPassword=old123&newPassword=new123
Authorization: Bearer eyJhbGc...

Response 200:
{
  "message": "Mot de passe changé avec succès"
}
```

#### 6. Désactiver un utilisateur (ADMIN)
```http
PUT /api/users/{id}/disable
Authorization: Bearer eyJhbGc...
```

### 🏠 Properties Endpoints (`/api/properties`)

#### 1. Obtenir les propriétés publiées
```http
GET /api/properties/public?page=0&size=10

Response 200:
{
  "content": [
    {
      "id": 1,
      "title": "Beautiful House",
      "description": "A beautiful house in the city",
      "price": 500000.0,
      "area": 200.0,
      "bedrooms": 4,
      "bathrooms": 2,
      "propertyType": "HOUSE",
      "city": "Bamenda",
      "neighborhood": "Downtown",
      "status": "AVAILABLE",
      "ownerId": 1,
      "ownerName": "Property Owner",
      "createdAt": "2024-01-15T10:30:00",
      "isPublished": true
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

#### 2. Rechercher des propriétés
```http
GET /api/properties/search?city=Bamenda&minPrice=400000&maxPrice=600000&bedrooms=4&page=0&size=10
```

#### 3. Obtenir une propriété
```http
GET /api/properties/{id}
```

#### 4. Créer une propriété (SELLER/AGENT/ADMIN)
```http
POST /api/properties
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{
  "title": "New House",
  "description": "A new house",
  "price": 500000.0,
  "area": 200.0,
  "bedrooms": 4,
  "bathrooms": 2,
  "propertyType": "HOUSE",
  "city": "Bamenda",
  "neighborhood": "Downtown",
  "address": "123 Main St",
  "latitude": 3.8667,
  "longitude": 10.1567,
  "isPublished": true,
  "features": ["Swimming Pool", "Garden", "Garage"]
}

Response 201: [PropertyDTO object]
```

#### 5. Mettre à jour une propriété
```http
PUT /api/properties/{id}
Authorization: Bearer eyJhbGc...
Content-Type: application/json

{...PropertyDTO...}
```

#### 6. Changer le statut d'une propriété
```http
PUT /api/properties/{id}/status?status=SOLD
Authorization: Bearer eyJhbGc...
```

#### 7. Assigner un agent
```http
PUT /api/properties/{id}/assign-agent?agentId=2
Authorization: Bearer eyJhbGc...
```

#### 8. Supprimer une propriété
```http
DELETE /api/properties/{id}
Authorization: Bearer eyJhbGc...
```

## 🔐 Rôles et Permissions

| Rôle | Description | Permissions |
|------|-------------|-------------|
| ADMIN | Administrateur | Tous les droits |
| AGENT | Agent immobilier | Gérer propriétés, voir utilisateurs |
| SELLER | Vendeur | Créer/modifier ses propriétés |
| BUYER | Acheteur | Consulter propriétés |
| USER | Utilisateur standard | Consulter propriétés publiques |

## 🧪 Tests

### Exécuter tous les tests
```bash
mvn test
```

### Exécuter un test spécifique
```bash
mvn test -Dtest=UserServiceTest
```

### Couverture de code
```bash
mvn clean test jacoco:report
```

## 📊 Modèles de Données

### User
```java
{
  id: Long,
  username: String (unique),
  email: String (unique),
  password: String (encoded),
  fullName: String,
  phone: String,
  address: String,
  role: RoleEnum,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime,
  lastLogin: LocalDateTime,
  isActive: Boolean
}
```

### Property
```java
{
  id: Long,
  title: String,
  description: String,
  price: Double,
  area: Double,
  bedrooms: Integer,
  bathrooms: Integer,
  propertyType: PropertyType (APARTMENT, HOUSE, LAND, COMMERCIAL, OFFICE, VILLA),
  city: String,
  neighborhood: String,
  address: String,
  latitude: Double,
  longitude: Double,
  status: PropertyStatus (AVAILABLE, RESERVED, SOLD, RENT),
  owner: User,
  agent: User,
  createdAt: LocalDateTime,
  updatedAt: LocalDateTime,
  isPublished: Boolean,
  features: List<String>
}
```

## 🛡️ Sécurité

- **JWT Tokens**: Tokens d'accès (24h) et refresh tokens (7j)
- **Password Encoding**: BCrypt avec salt aléatoire
- **CORS**: Configuré pour les domaines autorisés
- **Role-based Access Control**: Contrôle granulaire des permissions
- **Entity Validation**: Validation JSR-303 sur tous les inputs
- **Error Handling**: Gestion d'erreurs sécurisée sans exposition de détails sensibles

## 📝 Exemples d'Utilisation

### Flux d'authentification complet

1. **Enregistrement**
   ```bash
   curl -X POST "http://localhost:8080/api/auth/register?username=john&email=john@example.com&password=Pass123&fullName=John Doe"
   ```

2. **Connexion**
   ```bash
   curl -X POST "http://localhost:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"email":"john@example.com","password":"Pass123"}'
   ```

3. **Utiliser le token**
   ```bash
   curl -X GET "http://localhost:8080/api/users/profile" \
     -H "Authorization: Bearer <ACCESS_TOKEN>"
   ```

4. **Rafraîchir le token**
   ```bash
   curl -X POST "http://localhost:8080/api/auth/refresh?refreshToken=<REFRESH_TOKEN>"
   ```

## 🐛 Dépannage

### Port déjà utilisé
```bash
lsof -i :8080
kill -9 <PID>
```

### Erreur de base de données
Vérifiez la configuration dans `application.properties` et assurez-vous que PostgreSQL est en cours d'exécution.

### Problèmes JWT
Vérifiez que la clé secrète JWT est suffisamment longue (min 32 caractères).

## 📞 Support

Pour toute question ou problème:
- Consultez la documentation Swagger: `http://localhost:8080/api/swagger-ui.html`
- Vérifiez les logs de l'application: `target/logs/`

## 📄 Licence

Ce projet est sous licence MIT.

## ✅ Prochaines Étapes

- [ ] Ajouter la pagination aux listes
- [ ] Implémenter les images de propriétés
- [ ] Ajouter les avis et commentaires
- [ ] Implémenter les favoris
- [ ] Ajouter les notifications email
- [ ] Implémenter OAuth2
- [ ] Ajouter la géolocalisation avancée
- [ ] Créer une application mobile

---

**Version**: 1.0.0  
**Dernière mise à jour**: 2024
