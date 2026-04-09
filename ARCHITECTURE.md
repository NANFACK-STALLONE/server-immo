# 🏗️ Architecture & Diagrammes

## Architecture Générale

```
┌────────────────────────────────────────────────────────────────┐
│                        CLIENT (Web/Mobile)                      │
├────────────────────────────────────────────────────────────────┤
│                                                                  │
│                         HTTP/REST                               │
│                                                                  │
├────────────────────────────────────────────────────────────────┤
│                    API REST - Spring Boot                       │
├──────────┬──────────────┬──────────────┬──────────────┬────────┤
│  Auth    │   Users      │  Properties  │  Security    │ Config │
│ Resource │  Resource    │  Resource    │  (JWT)       │        │
├──────────┴──────────────┴──────────────┴──────────────┴────────┤
│                                                                  │
│                      SERVICE LAYER                              │
├──────────┬──────────────┬──────────────┬──────────────┐        │
│  Auth    │   User       │  Property    │   Custom     │        │
│ Service  │  Service     │  Service     │   Details    │        │
│          │              │              │   Service    │        │
├──────────┴──────────────┴──────────────┴──────────────┤        │
│                                                                  │
│                    REPOSITORY LAYER                             │
├──────────┬──────────────┐                                       │
│  User    │  Property    │                                       │
│Repository│Repository    │                                       │
├──────────┴──────────────┤                                       │
│                                                                  │
│                  DATABASE (JPA/Hibernate)                       │
├──────────┬──────────────┬──────────────┐                        │
│  Users   │  Properties  │   Roles      │                        │
│  Table   │  Table       │  Table       │                        │
└──────────┴──────────────┴──────────────┘
```

## Flux d'Authentification

```
┌─────────────────┐
│ User           │
└────────┬────────┘
         │
         │ POST /api/auth/login
         │ {email, password}
         ↓
┌─────────────────────────────────┐
│ AuthResource.login()            │
└────────┬────────────────────────┘
         │
         │ Appelle
         ↓
┌─────────────────────────────────┐
│ AuthService.login()             │
│                                 │
│ 1. Récupère l'utilisateur      │
│ 2. Vérifie le mot de passe     │
│ 3. Génère tokens JWT            │
│ 4. Retourne LoginResponse       │
└────────┬────────────────────────┘
         │
         │ Utilise
         ↓
┌──────────────────────────────────┐
│ UserRepository                   │
│ JwtTokenProvider                 │
│ PasswordEncoder (BCrypt)         │
└──────────────┬───────────────────┘
               │
               │ Retourne
               ↓
         ┌─────────────┐
         │ LoginResponse
         │ - accessToken
         │ - refreshToken
         │ - user
         └─────────────┘
```

## Flux de Requête Sécurisée

```
┌──────────────────────┐
│ Client               │
│ GET /api/users/1     │
│ Authorization: Bearer│
│ <accessToken>        │
└──────────┬───────────┘
           │
           │
           ↓
┌──────────────────────────────────────┐
│ JwtAuthenticationFilter              │
│                                      │
│ 1. Extrait le token du header       │
│ 2. Valide le token                  │
│ 3. Récupère le username             │
│ 4. Charge les détails utilisateur   │
│ 5. Crée Authentication              │
│ 6. Définit SecurityContext          │
└──────────┬───────────────────────────┘
           │
           │ Requête authentifiée
           ↓
┌──────────────────────────────────────┐
│ UserResource.getUserById(1)          │
│ (Authentification confirmée)         │
└──────────┬───────────────────────────┘
           │
           │ Appelle
           ↓
┌──────────────────────────────────────┐
│ UserService.getUserById(1)           │
└──────────┬───────────────────────────┘
           │
           │ Accède
           ↓
┌──────────────────────────────────────┐
│ UserRepository.findById(1)           │
└──────────┬───────────────────────────┘
           │
           │ Retourne
           ↓
┌──────────────────────────────────────┐
│ User Entity                          │
└──────────┬───────────────────────────┘
           │
           │ Convertit en
           ↓
┌──────────────────────────────────────┐
│ UserDTO                              │
└──────────┬───────────────────────────┘
           │
           │ Répond avec
           ↓
┌──────────────────────────────────────┐
│ HTTP 200 OK                          │
│ {UserDTO en JSON}                    │
└──────────────────────────────────────┘
```

## Architecture en Couches

```
┌──────────────────────────────────────────────────────┐
│              PRESENTATION LAYER                      │
│  (REST Controllers / Resources)                      │
├──────────────────────────────────────────────────────┤
│  AuthResource    UserResource    PropertyResource    │
│                                                      │
│  - Valide les requêtes HTTP                         │
│  - Gère les réponses                               │
│  - Appelle les services                            │
└──────────────────┬───────────────────────────────────┘
                   │
┌──────────────────────────────────────────────────────┐
│              BUSINESS LOGIC LAYER                    │
│  (Services)                                         │
├──────────────────────────────────────────────────────┤
│  AuthService     UserService    PropertyService     │
│                                                      │
│  - Logique métier                                   │
│  - Validations complexes                            │
│  - Transformations de données                       │
└──────────────────┬───────────────────────────────────┘
                   │
┌──────────────────────────────────────────────────────┐
│              DATA ACCESS LAYER                       │
│  (Repositories)                                     │
├──────────────────────────────────────────────────────┤
│  UserRepository    PropertyRepository               │
│                                                      │
│  - Accès à la base de données                      │
│  - Requêtes JPA                                    │
│  - Transactions                                     │
└──────────────────┬───────────────────────────────────┘
                   │
┌──────────────────────────────────────────────────────┐
│              DATABASE LAYER                          │
│                                                      │
├──────────────────────────────────────────────────────┤
│  users_table    properties_table    roles_table     │
│                                                      │
│  (PostgreSQL / H2)                                  │
└──────────────────────────────────────────────────────┘
```

## Modèle Entité-Relation (ER)

```
┌─────────────────────────────┐         ┌──────────────────────────┐
│          USERS              │         │     PROPERTIES           │
├─────────────────────────────┤         ├──────────────────────────┤
│ PK id                       │ ◄────── │ PK id                    │
│    username (unique)        │ 1    ∞  │    title                 │
│    email (unique)           │         │    description           │
│    password                 │         │    price                 │
│    fullName                 │         │    area                  │
│    phone                    │         │    bedrooms              │
│    address                  │         │    bathrooms             │
│    role (ENUM)              │         │    propertyType (ENUM)   │
│    createdAt                │         │    city                  │
│    updatedAt                │         │    neighborhood          │
│    lastLogin                │         │    address               │
│    isActive                 │         │    latitude              │
│                             │         │    longitude             │
└─────────────────────────────┘         │    status (ENUM)         │
           ▲                            │    FK owner_id (→ users) │
           │                            │    FK agent_id (→ users) │
           │                            │    createdAt             │
           │                            │    updatedAt             │
           │                            │    isPublished           │
           │                            │    features (List)       │
           │                            └──────────────────────────┘
           └────── 1 utilisateur peut être propriétaire
                   de plusieurs propriétés


         PROPERTY_FEATURES
        ┌──────────────────┐
        │ FK property_id   │ ──┐
        │    feature       │   │
        └──────────────────┘   │
                               │
        (Liste des             │
         caractéristiques      │
         des propriétés)       │
                               │
                      ┌────────┘
                      │
                      ↓
              ┌──────────────────┐
              │ PROPERTY_FEATURES│
              └──────────────────┘
```

## Sécurité & JWT

```
┌─────────────────────────────────────────────────────────┐
│                 JWT Token Structure                     │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJqb2huIiwi...        │
│  ▲                     ▲                     ▲          │
│  │                     │                     │          │
│  Header                Payload               Signature  │
│  ┌─────────────────┐   ┌──────────────────┐ ┌────────┐ │
│  │ {               │   │ {                │ │ HMAC   │ │
│  │ "alg": "HS512", │   │ "sub": "john",   │ │ SHA512 │ │
│  │ "typ": "JWT"    │   │ "email": "j@...", │ │ Hash   │ │
│  │ }               │   │ "role": "SELLER",│ │        │ │
│  │                 │   │ "iat": 1234567890,│ │        │ │
│  │ Base64URL       │   │ "exp": 1234671490 │ │        │ │
│  │ Encoded         │   │ }                │ │ Base64 │ │
│  │                 │   │                  │ │ Encoded│ │
│  │                 │   │ Base64URL        │ │        │ │
│  │                 │   │ Encoded          │ │        │ │
│  └─────────────────┘   └──────────────────┘ └────────┘ │
│                                                          │
├─────────────────────────────────────────────────────────┤
│              Access Token: Valide 24h                   │
│              Refresh Token: Valide 7 jours             │
└─────────────────────────────────────────────────────────┘
```

## Flux de Contrôle d'Accès (RBAC)

```
┌──────────────────────────┐
│ Requête HTTP             │
│ GET /api/properties/1    │
└────────┬─────────────────┘
         │
         ├─ Token invalide ou expiré?
         │  └─ 401 Unauthorized
         │
         ├─ Public endpoint?
         │  └─ Accès accordé
         │
         └─ Endpoint protégé?
            │
            ├─ @PreAuthorize("hasAnyRole(...)")
            │
            └─ Rôle de l'utilisateur autorisé?
               ├─ OUI
               │  └─ Logique métier
               │     └─ Propriétaire de la ressource?
               │        ├─ OUI
               │        │  └─ 200 OK
               │        │
               │        └─ NON
               │           └─ 403 Forbidden
               │
               └─ NON
                  └─ 403 Forbidden
```

## Pipeline de Requête

```
HTTP Request
    │
    ↓
┌────────────────────────────────┐
│ Spring DispatcherServlet       │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ CORS Filter                    │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ JwtAuthenticationFilter        │
│ - Validation du token JWT      │
│ - Création de l'authentification
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ Security Filter Chain          │
│ - Vérification des rôles       │
│ - Vérification des permissions │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ Controller Method Dispatch     │
│ - Routage vers la bonne        │
│   resource/controller          │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ Service Layer                  │
│ - Logique métier               │
│ - Validations                  │
│ - Transformations              │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ Repository Layer               │
│ - Accès base de données        │
│ - Transactions                 │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ Database                       │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ Response Serialization         │
│ - Conversion en JSON           │
└────────┬───────────────────────┘
         │
         ↓
┌────────────────────────────────┐
│ HTTP Response                  │
└────────────────────────────────┘
```

## Structure de Dossiers

```
immobilier-api/
│
├── src/
│   ├── main/
│   │   ├── java/com/immobilier/
│   │   │   ├── config/                    (Configuration)
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   ├── entity/                    (Modèles)
│   │   │   │   ├── User.java
│   │   │   │   ├── Property.java
│   │   │   │   └── RoleEnum.java
│   │   │   │
│   │   │   ├── dto/                       (Data Transfer Objects)
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── LoginResponse.java
│   │   │   │   ├── UserDTO.java
│   │   │   │   └── PropertyDTO.java
│   │   │   │
│   │   │   ├── repository/                (Accès données)
│   │   │   │   ├── UserRepository.java
│   │   │   │   └── PropertyRepository.java
│   │   │   │
│   │   │   ├── service/                   (Logique métier)
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── UserService.java
│   │   │   │   ├── PropertyService.java
│   │   │   │   └── CustomUserDetailsService.java
│   │   │   │
│   │   │   ├── resource/                  (Contrôleurs REST)
│   │   │   │   ├── AuthResource.java
│   │   │   │   ├── UserResource.java
│   │   │   │   └── PropertyResource.java
│   │   │   │
│   │   │   ├── security/                  (Sécurité & JWT)
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtAuthenticationEntryPoint.java
│   │   │   │
│   │   │   ├── exception/                 (Gestion d'erreurs)
│   │   │   │   ├── ApiError.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   └── ImmobilierApiApplication.java  (Point d'entrée)
│   │   │
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
├── pom.xml                   (Maven configuration)
├── README.md                 (Documentation)
├── INSTALLATION.md           (Guide d'installation)
├── CURL_EXAMPLES.md          (Exemples d'utilisation)
└── SUMMARY.md                (Synthèse du projet)
```

---

**Cette architecture est scalable, sécurisée et suit les bonnes pratiques de développement Spring Boot! 🎯**
