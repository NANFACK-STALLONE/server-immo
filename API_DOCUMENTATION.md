# Documentation API REST Immobilier

**Version:** 2.0.0 — Migration JAX-RS (Jersey)
**Base URL:** `http://localhost:8080`
**Auth:** Bearer JWT Token
**Framework:** Spring Boot 2.7.14 + JAX-RS (Jersey)
**Base de données:** MongoDB

---
ƒöÉ Compte ADMIN cr├®├® avec succ├¿s !
2026-04-10 04:40:37.053  INFO 9964 --- [           main] com.immobilier.config.DataInitializer    :    Username : admin
2026-04-10 04:40:37.055  INFO 9964 --- [           main] com.immobilier.config.DataInitializer    :    Email    : admin@immo.com
2026-04-10 04:40:37.055  INFO 9964 --- [           main] com.immobilier.config.DataInitializer    :    Password : Admin@2024!
2026-04-10 04:40:37.055  INFO 9964 --- [           main] com.immobilier.config.DataInitializer    :    R├┤le     : ROLE_ADMIN
2026-04-10 04:40:37.055  INFO 9964 --- [           main] com.immobilier.config.DataInitializer    : ÔÜá´©Å  Pensez ├á changer le mot de passe en production !
## Stack Technique

| Composant | Technologie | Rôle |
|-----------|-------------|------|
| Framework REST | **JAX-RS (Jersey 2.x)** | Gestion des endpoints HTTP |
| Sécurité | Spring Security 5.7 + JWT (JJWT 0.11.5) | Authentification & autorisation |
| Base de données | MongoDB (Spring Data MongoDB) | Persistance des données |
| Serveur | Apache Tomcat 9 (embarqué) | Conteneur de servlets |
| Sérialisation | Jackson via `JacksonFeature` | Conversion JSON |
| Validation | Bean Validation JSR-380 | Validation des DTOs |

### Correspondance des annotations Spring MVC → JAX-RS

| Spring MVC (ancienne version) | JAX-RS / Jersey (version actuelle) |
|-------------------------------|-------------------------------------|
| `@RestController` | `@Component` + `@Path` |
| `@RequestMapping("/path")` | `@Path("/path")` sur la classe |
| `@GetMapping("/sub")` | `@GET` + `@Path("/sub")` |
| `@PostMapping` | `@POST` |
| `@PutMapping("/sub")` | `@PUT` + `@Path("/sub")` |
| `@DeleteMapping("/sub")` | `@DELETE` + `@Path("/sub")` |
| `@PathVariable String id` | `@PathParam("id") String id` |
| `@RequestParam String x` | `@QueryParam("x") String x` |
| `@RequestParam(defaultValue="v")` | `@QueryParam("x") @DefaultValue("v")` |
| `@RequestHeader String h` | `@HeaderParam("h") String h` |
| `@RequestBody DTO dto` | `DTO dto` (pas d'annotation, Jersey désérialise le body) |
| `ResponseEntity<T>` | `Response` (javax.ws.rs.core.Response) |
| `ResponseEntity.ok(body)` | `Response.ok(body).build()` |
| `ResponseEntity.status(201)` | `Response.status(Status.CREATED).entity(x).build()` |
| `@RestControllerAdvice` + `@ExceptionHandler` | `@Provider` + `ExceptionMapper<T>` |
| `Authentication authentication` (paramètre) | `SecurityContextHolder.getContext().getAuthentication()` |

---

## Rôles & Permissions

| Rôle | Description | Accès |
|------|-------------|-------|
| `ROLE_ADMIN` | Administrateur | Tous les droits |
| `ROLE_AGENT` | Agent immobilier | Gestion propriétés + consultation users |
| `ROLE_SELLER` | Vendeur | Créer/modifier ses propriétés |
| `ROLE_BUYER` | Acheteur | Consultation propriétés |
| `ROLE_USER` | Utilisateur standard | Consultation propriétés publiques |

> Les contrôles d'accès sont assurés par `@PreAuthorize` (Spring Security AOP)
> appliqué directement sur les méthodes des resources JAX-RS.

---

## Format des erreurs

Toutes les erreurs sont gérées par des `ExceptionMapper` JAX-RS (`@Provider`)
et retournent le format JSON suivant :

```json
{
  "status": 404,
  "error": "Ressource non trouvée",
  "message": "Propriété non trouvée avec l'ID: xxx",
  "path": "",
  "timestamp": "2026-04-09T10:00:00",
  "details": null
}
```

| Mapper JAX-RS | Exception interceptée | Code HTTP |
|---|---|---|
| `ResourceNotFoundExceptionMapper` | `ResourceNotFoundException` | 404 |
| `BadCredentialsExceptionMapper` | `BadCredentialsException` | 401 |
| `AccessDeniedExceptionMapper` | `AccessDeniedException` | 403 |
| `ValidationExceptionMapper` | `ConstraintViolationException` | 400 |
| `IllegalArgumentExceptionMapper` | `IllegalArgumentException` | 400 |
| `GlobalExceptionMapper` | `Exception` (catch-all) | 500 |

---

## Format de pagination

Les endpoints paginés retournent un objet `PageResponse<T>` :

```json
{
  "content": [ ... ],
  "totalElements": 42,
  "totalPages": 5,
  "number": 0,
  "size": 10,
  "first": true,
  "last": false
}
```

> `PageResponse<T>` est un DTO JAX-RS personnalisé qui remplace
> `org.springframework.data.domain.Page` (spécifique à Spring MVC).

---

## 1. Authentification — `/api/auth`

### POST `/api/auth/register`
Créer un nouveau compte utilisateur.

**Accès :** Public

**Annotation JAX-RS :** `@POST @Path("/register") @Consumes(APPLICATION_FORM_URLENCODED)`

**Paramètres (Query) :**
| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `username` | String | ✅ | Nom d'utilisateur unique |
| `email` | String | ✅ | Email unique |
| `password` | String | ✅ | Mot de passe |
| `fullName` | String | ✅ | Nom complet |

**Exemple de requête :**
```http
POST /api/auth/register?username=john&email=john@example.com&password=Pass123&fullName=John Doe
```

**Réponse 201 Created :**
```json
{
  "message": "Enregistrement réussi",
  "userId": "6073f6a9e5f3a12b3c4d5e6f",
  "username": "john",
  "email": "john@example.com"
}
```

**Erreurs :**
| Code | Cause |
|------|-------|
| `400` | Username ou email déjà utilisé |

---

### POST `/api/auth/login`
Authentifier un utilisateur et obtenir les tokens JWT.

**Accès :** Public

**Annotation JAX-RS :** `@POST @Path("/login") @Consumes(APPLICATION_JSON)`

**Body (JSON) :**
```json
{
  "email": "john@example.com",
  "password": "Pass123"
}
```

**Réponse 200 OK :**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": "6073f6a9e5f3a12b3c4d5e6f",
    "username": "john",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "ROLE_USER"
  }
}
```

> **accessToken** : valide 24h (86 400 000 ms)
> **refreshToken** : valide 7 jours (604 800 000 ms)

**Erreurs :**
| Code | Cause |
|------|-------|
| `401` | Email ou mot de passe incorrect |
| `400` | Compte désactivé |

---

### POST `/api/auth/refresh`
Obtenir un nouvel access token avec un refresh token valide.

**Accès :** Public

**Annotation JAX-RS :** `@POST @Path("/refresh")`

**Paramètres (Query) :**
| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `refreshToken` | String | ✅ | Refresh token JWT |

**Exemple de requête :**
```http
POST /api/auth/refresh?refreshToken=eyJhbGciOiJIUzUxMiJ9...
```

**Réponse 200 OK :**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**Erreurs :**
| Code | Cause |
|------|-------|
| `400` | Refresh token invalide ou expiré |

---

### GET `/api/auth/validate`
Vérifier la validité d'un access token.

**Accès :** Public

**Annotation JAX-RS :** `@GET @Path("/validate")`

**Headers :**
```
Authorization: Bearer <ACCESS_TOKEN>
```

> Le token est lu via `@HeaderParam("Authorization")` en JAX-RS
> (anciennement `@RequestHeader` en Spring MVC).

**Réponse 200 OK :**
```json
{
  "valid": true,
  "message": "Token valide"
}
```

**Réponse 401 Unauthorized :**
```json
{
  "valid": false,
  "message": "Token invalide ou expiré"
}
```

---

### GET `/api/auth/health`
Vérifier que l'API est en ligne.

**Accès :** Public

**Annotation JAX-RS :** `@GET @Path("/health")`

**Réponse 200 OK :**
```json
{
  "status": "UP",
  "message": "API Immobilier est en ligne",
  "timestamp": 1744185600000
}
```

---

## 2. Utilisateurs — `/api/users`

> Tous les endpoints users nécessitent un Bearer Token sauf mention contraire.
> L'utilisateur connecté est récupéré via `SecurityContextHolder.getContext().getAuthentication()`.

### GET `/api/users/profile`
Récupérer le profil de l'utilisateur connecté.

**Accès :** `ROLE_USER`, `ROLE_BUYER`, `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@GET @Path("/profile")`

**Headers :**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Réponse 200 OK :**
```json
{
  "id": "6073f6a9e5f3a12b3c4d5e6f",
  "username": "john",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phone": "655123456",
  "address": "123 Rue Principale, Yaoundé",
  "role": "ROLE_USER",
  "createdAt": "2026-04-09T10:00:00",
  "updatedAt": "2026-04-09T10:00:00",
  "isActive": true
}
```

---

### PUT `/api/users/profile`
Mettre à jour le profil de l'utilisateur connecté.

**Accès :** `ROLE_USER`, `ROLE_BUYER`, `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@PUT @Path("/profile") @Consumes(APPLICATION_JSON)`

**Body (JSON) :**
```json
{
  "fullName": "John Updated",
  "phone": "655999888",
  "address": "456 Nouvelle Avenue"
}
```

**Réponse 200 OK :** `UserDTO` mis à jour

---

### GET `/api/users/{id}`
Récupérer un utilisateur par son ID.

**Accès :** `ROLE_USER`, `ROLE_BUYER`, `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@GET @Path("/{id}")`

**Paramètre (Path) :**
| Paramètre | Annotation JAX-RS | Type | Description |
|-----------|-------------------|------|-------------|
| `id` | `@PathParam("id")` | String | ID MongoDB de l'utilisateur |

**Exemple :**
```http
GET /api/users/6073f6a9e5f3a12b3c4d5e6f
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK :** `UserDTO`

**Erreurs :**
| Code | Cause |
|------|-------|
| `404` | Utilisateur non trouvé |

---

### GET `/api/users`
Récupérer la liste de tous les utilisateurs.

**Accès :** `ROLE_ADMIN` uniquement

**Annotation JAX-RS :** `@GET` (pas de `@Path` supplémentaire)

**Réponse 200 OK :**
```json
[
  {
    "id": "6073f6a9e5f3a12b3c4d5e6f",
    "username": "john",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "ROLE_USER",
    "isActive": true
  }
]
```

---

### PUT `/api/users/{id}`
Mettre à jour un utilisateur (admin seulement).

**Accès :** `ROLE_ADMIN`

**Annotation JAX-RS :** `@PUT @Path("/{id}") @Consumes(APPLICATION_JSON)`

**Paramètre (Path) :** `@PathParam("id")` — ID MongoDB de l'utilisateur

**Body (JSON) :**
```json
{
  "fullName": "Nom Modifié",
  "phone": "655000111",
  "address": "Nouvelle adresse"
}
```

**Réponse 200 OK :** `UserDTO` mis à jour

---

### POST `/api/users/change-password`
Changer le mot de passe de l'utilisateur connecté.

**Accès :** `ROLE_USER`, `ROLE_BUYER`, `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@POST @Path("/change-password")`

**Paramètres (Query) :**
| Paramètre | Annotation JAX-RS | Type | Requis | Description |
|-----------|-------------------|------|--------|-------------|
| `oldPassword` | `@QueryParam("oldPassword")` | String | ✅ | Ancien mot de passe |
| `newPassword` | `@QueryParam("newPassword")` | String | ✅ | Nouveau mot de passe |

**Exemple :**
```http
POST /api/users/change-password?oldPassword=Pass123&newPassword=NewPass456
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK :**
```json
{
  "message": "Mot de passe changé avec succès"
}
```

**Erreurs :**
| Code | Cause |
|------|-------|
| `400` | Ancien mot de passe incorrect |

---

### PUT `/api/users/{id}/disable`
Désactiver le compte d'un utilisateur.

**Accès :** `ROLE_ADMIN`

**Annotation JAX-RS :** `@PUT @Path("/{id}/disable")`

**Exemple :**
```http
PUT /api/users/6073f6a9e5f3a12b3c4d5e6f/disable
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK :** `UserDTO` avec `isActive: false`

---

### PUT `/api/users/{id}/enable`
Activer le compte d'un utilisateur.

**Accès :** `ROLE_ADMIN`

**Annotation JAX-RS :** `@PUT @Path("/{id}/enable")`

**Réponse 200 OK :** `UserDTO` avec `isActive: true`

---

### DELETE `/api/users/{id}`
Supprimer définitivement un utilisateur.

**Accès :** `ROLE_ADMIN`

**Annotation JAX-RS :** `@DELETE @Path("/{id}")`

**Réponse 200 OK :**
```json
{
  "message": "Utilisateur supprimé avec succès"
}
```

---

## 3. Propriétés — `/api/properties`

### GET `/api/properties/public`
Lister toutes les propriétés publiées et disponibles (paginé).

**Accès :** Public (sans token)

**Annotation JAX-RS :** `@GET @Path("/public")`

**Paramètres (Query) :**
| Paramètre | Annotation JAX-RS | Type | Défaut | Description |
|-----------|-------------------|------|--------|-------------|
| `page` | `@QueryParam("page") @DefaultValue("0")` | Integer | `0` | Numéro de page |
| `size` | `@QueryParam("size") @DefaultValue("10")` | Integer | `10` | Taille de la page |

**Exemple :**
```http
GET /api/properties/public?page=0&size=10
```

**Réponse 200 OK — `PageResponse<PropertyDTO>` :**
```json
{
  "content": [
    {
      "id": "6073f6a9e5f3a12b3c4d5e6f",
      "title": "Belle Villa à Bastos",
      "description": "Magnifique villa avec piscine",
      "price": 150000000.0,
      "area": 350.0,
      "bedrooms": 5,
      "bathrooms": 3,
      "propertyType": "VILLA",
      "city": "Yaoundé",
      "neighborhood": "Bastos",
      "address": "Rue des Ambassades",
      "latitude": 3.8667,
      "longitude": 11.5167,
      "status": "AVAILABLE",
      "ownerId": "abc123",
      "ownerName": "Jean Dupont",
      "agentId": null,
      "agentName": null,
      "createdAt": "2026-04-09T10:00:00",
      "updatedAt": "2026-04-09T10:00:00",
      "isPublished": true,
      "features": ["Piscine", "Jardin", "Garage"]
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10,
  "first": true,
  "last": true
}
```

---

### GET `/api/properties/search`
Rechercher des propriétés avec filtres dynamiques.

**Accès :** Public (sans token)

**Annotation JAX-RS :** `@GET @Path("/search")`

> La recherche est implémentée avec `MongoTemplate` et des `Criteria` dynamiques.
> Tous les paramètres sont optionnels.

**Paramètres (Query) :**
| Paramètre | Annotation JAX-RS | Type | Requis | Description |
|-----------|-------------------|------|--------|-------------|
| `city` | `@QueryParam("city")` | String | ❌ | Filtrer par ville |
| `minPrice` | `@QueryParam("minPrice")` | Double | ❌ | Prix minimum (FCFA) |
| `maxPrice` | `@QueryParam("maxPrice")` | Double | ❌ | Prix maximum (FCFA) |
| `bedrooms` | `@QueryParam("bedrooms")` | Integer | ❌ | Nombre minimum de chambres |
| `page` | `@QueryParam("page") @DefaultValue("0")` | Integer | ❌ | Numéro de page |
| `size` | `@QueryParam("size") @DefaultValue("10")` | Integer | ❌ | Taille de page |

**Exemple :**
```http
GET /api/properties/search?city=Douala&minPrice=5000000&maxPrice=50000000&bedrooms=3&page=0&size=5
```

**Réponse 200 OK :** `PageResponse<PropertyDTO>` (même format que `/public`)

---

### GET `/api/properties/{id}`
Récupérer le détail d'une propriété par son ID.

**Accès :** Public (sans token)

**Annotation JAX-RS :** `@GET @Path("/{id}")`

**Paramètre (Path) :**
| Paramètre | Annotation JAX-RS | Type | Description |
|-----------|-------------------|------|-------------|
| `id` | `@PathParam("id")` | String | ID MongoDB de la propriété |

**Exemple :**
```http
GET /api/properties/6073f6a9e5f3a12b3c4d5e6f
```

**Réponse 200 OK :** `PropertyDTO`

**Erreurs :**
| Code | Cause |
|------|-------|
| `404` | Propriété non trouvée |

---

### POST `/api/properties`
Créer une nouvelle propriété.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@POST @Consumes(APPLICATION_JSON)`

**Headers :**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Body (JSON) :**
```json
{
  "title": "Appartement moderne à Akwa",
  "description": "Bel appartement au centre-ville avec vue panoramique",
  "price": 25000000.0,
  "area": 120.0,
  "bedrooms": 3,
  "bathrooms": 2,
  "propertyType": "APARTMENT",
  "city": "Douala",
  "neighborhood": "Akwa",
  "address": "Avenue de Gaulle, Immeuble Le Palmier",
  "latitude": 4.0511,
  "longitude": 9.7679,
  "isPublished": true,
  "features": ["Climatisation", "Ascenseur", "Gardiennage", "Parking"]
}
```

**Types de propriété (`propertyType`) :**
`APARTMENT` | `HOUSE` | `LAND` | `COMMERCIAL` | `OFFICE` | `VILLA`

**Réponse 201 Created :** `PropertyDTO` créé

**Erreurs :**
| Code | Cause |
|------|-------|
| `400` | Champs requis manquants ou invalides (`ConstraintViolationException`) |
| `401` | Token manquant ou invalide |
| `403` | Rôle insuffisant (`AccessDeniedException`) |

---

### PUT `/api/properties/{id}`
Mettre à jour une propriété existante (propriétaire uniquement).

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@PUT @Path("/{id}") @Consumes(APPLICATION_JSON)`

**Paramètre (Path) :** `@PathParam("id")` — ID MongoDB de la propriété

**Body (JSON) :** Même format que la création (POST)

**Réponse 200 OK :** `PropertyDTO` mis à jour

**Erreurs :**
| Code | Cause |
|------|-------|
| `400` | Vous n'êtes pas propriétaire de cette propriété |
| `404` | Propriété non trouvée |

---

### DELETE `/api/properties/{id}`
Supprimer une propriété (propriétaire uniquement).

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@DELETE @Path("/{id}")`

**Exemple :**
```http
DELETE /api/properties/6073f6a9e5f3a12b3c4d5e6f
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK :**
```json
{
  "message": "Propriété supprimée avec succès"
}
```

---

### GET `/api/properties/owner/list`
Récupérer toutes les propriétés appartenant à l'utilisateur connecté.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Annotation JAX-RS :** `@GET @Path("/owner/list")`

**Réponse 200 OK :** Liste de `PropertyDTO`

---

### PUT `/api/properties/{id}/status`
Changer le statut d'une propriété.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN` (propriétaire uniquement)

**Annotation JAX-RS :** `@PUT @Path("/{id}/status")`

**Paramètres (Query) :**
| Paramètre | Annotation JAX-RS | Type | Valeurs possibles |
|-----------|-------------------|------|------------------|
| `status` | `@QueryParam("status")` | String | `AVAILABLE` \| `RESERVED` \| `SOLD` \| `RENT` |

**Exemple :**
```http
PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/status?status=SOLD
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK :** `PropertyDTO` avec le nouveau statut

---

### PUT `/api/properties/{id}/assign-agent`
Assigner un agent immobilier à une propriété.

**Accès :** `ROLE_SELLER`, `ROLE_ADMIN` (propriétaire uniquement)

**Annotation JAX-RS :** `@PUT @Path("/{id}/assign-agent")`

**Paramètres (Query) :**
| Paramètre | Annotation JAX-RS | Type | Requis | Description |
|-----------|-------------------|------|--------|-------------|
| `agentId` | `@QueryParam("agentId")` | String | ✅ | ID MongoDB de l'agent |

**Exemple :**
```http
PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/assign-agent?agentId=abc123def456
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK :** `PropertyDTO` avec l'agent assigné

**Erreurs :**
| Code | Cause |
|------|-------|
| `404` | Agent non trouvé ou n'a pas le rôle `ROLE_AGENT` |

---

### PUT `/api/properties/{id}/publish`
Publier ou dépublier une propriété.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN` (propriétaire uniquement)

**Annotation JAX-RS :** `@PUT @Path("/{id}/publish")`

**Paramètres (Query) :**
| Paramètre | Annotation JAX-RS | Type | Description |
|-----------|-------------------|------|-------------|
| `publish` | `@QueryParam("publish")` | Boolean | `true` pour publier, `false` pour dépublier |

**Exemple :**
```http
PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/publish?publish=false
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK :** `PropertyDTO` avec `isPublished` mis à jour

---

## Flux d'utilisation typique

```
1. S'enregistrer        → POST /api/auth/register?username=...&email=...&password=...&fullName=...
2. Se connecter         → POST /api/auth/login         { "email": "...", "password": "..." }
3. Consulter les biens  → GET  /api/properties/public?page=0&size=10
4. Rechercher           → GET  /api/properties/search?city=Douala&minPrice=5000000
5. Créer une annonce    → POST /api/properties          (token SELLER/AGENT/ADMIN requis)
6. Changer le statut    → PUT  /api/properties/{id}/status?status=SOLD
7. Gérer son profil     → GET  /api/users/profile
8. Rafraîchir le token  → POST /api/auth/refresh?refreshToken=...
```

---

## Architecture JAX-RS — Flux d'une requête

```
Client HTTP
    │
    ▼
[Spring Security Filter Chain]
    │  ① Vérifie le token JWT (JwtAuthenticationFilter)
    │  ② Peuple SecurityContextHolder
    │  ③ Vérifie les règles antMatchers (public vs protégé)
    │
    ▼
[Jersey Servlet Filter]  ← spring.jersey.type=filter
    │
    ▼
[JAX-RS Resource  @Path("/api/...")]
    │  ① Route vers la bonne méthode (@GET, @POST, @PUT, @DELETE)
    │  ② @PreAuthorize vérifie le rôle (Spring AOP)
    │  ③ Désérialise le body JSON (@Consumes + Jackson)
    │
    ▼
[Service Layer]
    │
    ▼
[MongoDB via Spring Data]
    │
    ▼
[JAX-RS ExceptionMapper]  ← si une exception est levée
    │
    ▼
Response JSON (@Produces(APPLICATION_JSON))
```

---

## Résumé des endpoints

| Méthode | Endpoint | Annotation JAX-RS | Auth | Description |
|---------|----------|-------------------|------|-------------|
| `POST` | `/api/auth/register` | `@POST @Path("/register")` | ❌ | Créer un compte |
| `POST` | `/api/auth/login` | `@POST @Path("/login")` | ❌ | Se connecter |
| `POST` | `/api/auth/refresh` | `@POST @Path("/refresh")` | ❌ | Rafraîchir le token |
| `GET` | `/api/auth/validate` | `@GET @Path("/validate")` | ❌ | Valider un token |
| `GET` | `/api/auth/health` | `@GET @Path("/health")` | ❌ | Santé de l'API |
| `GET` | `/api/users/profile` | `@GET @Path("/profile")` | ✅ Tous | Mon profil |
| `PUT` | `/api/users/profile` | `@PUT @Path("/profile")` | ✅ Tous | Modifier mon profil |
| `GET` | `/api/users/{id}` | `@GET @Path("/{id}")` | ✅ Tous | Profil par ID |
| `GET` | `/api/users` | `@GET` | ✅ ADMIN | Tous les utilisateurs |
| `PUT` | `/api/users/{id}` | `@PUT @Path("/{id}")` | ✅ ADMIN | Modifier un user |
| `POST` | `/api/users/change-password` | `@POST @Path("/change-password")` | ✅ Tous | Changer mot de passe |
| `PUT` | `/api/users/{id}/disable` | `@PUT @Path("/{id}/disable")` | ✅ ADMIN | Désactiver un compte |
| `PUT` | `/api/users/{id}/enable` | `@PUT @Path("/{id}/enable")` | ✅ ADMIN | Activer un compte |
| `DELETE` | `/api/users/{id}` | `@DELETE @Path("/{id}")` | ✅ ADMIN | Supprimer un user |
| `GET` | `/api/properties/public` | `@GET @Path("/public")` | ❌ | Propriétés publiées |
| `GET` | `/api/properties/search` | `@GET @Path("/search")` | ❌ | Recherche avancée |
| `GET` | `/api/properties/{id}` | `@GET @Path("/{id}")` | ❌ | Détail propriété |
| `POST` | `/api/properties` | `@POST` | ✅ SELLER/AGENT/ADMIN | Créer une propriété |
| `PUT` | `/api/properties/{id}` | `@PUT @Path("/{id}")` | ✅ SELLER/AGENT/ADMIN | Modifier une propriété |
| `DELETE` | `/api/properties/{id}` | `@DELETE @Path("/{id}")` | ✅ SELLER/AGENT/ADMIN | Supprimer une propriété |
| `GET` | `/api/properties/owner/list` | `@GET @Path("/owner/list")` | ✅ SELLER/AGENT/ADMIN | Mes propriétés |
| `PUT` | `/api/properties/{id}/status` | `@PUT @Path("/{id}/status")` | ✅ SELLER/AGENT/ADMIN | Changer le statut |
| `PUT` | `/api/properties/{id}/assign-agent` | `@PUT @Path("/{id}/assign-agent")` | ✅ SELLER/ADMIN | Assigner un agent |
| `PUT` | `/api/properties/{id}/publish` | `@PUT @Path("/{id}/publish")` | ✅ SELLER/AGENT/ADMIN | Publier/Dépublier |
