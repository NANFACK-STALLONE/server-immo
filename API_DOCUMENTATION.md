# Documentation API REST Immobilier

**Version:** 1.0.0
**Base URL:** `http://localhost:8080/api`
**Auth:** Bearer JWT Token

---

## Rôles & Permissions

| Rôle | Description | Accès |
|------|-------------|-------|
| `ROLE_ADMIN` | Administrateur | Tous les droits |
| `ROLE_AGENT` | Agent immobilier | Gestion propriétés + consultation users |
| `ROLE_SELLER` | Vendeur | Créer/modifier ses propriétés |
| `ROLE_BUYER` | Acheteur | Consultation propriétés |
| `ROLE_USER` | Utilisateur standard | Consultation propriétés publiques |

---

## Format des erreurs

Toutes les erreurs retournent le format suivant :

```json
{
  "status": 404,
  "error": "Ressource non trouvée",
  "message": "Propriété non trouvée avec l'ID: xxx",
  "path": "/api/properties/xxx",
  "timestamp": "2026-04-09T10:00:00",
  "details": null
}
```

---

## 1. Authentification — `/api/auth`

### POST `/api/auth/register`
Créer un nouveau compte utilisateur.

**Accès :** Public

**Paramètres (Query):**
| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `username` | String | ✅ | Nom d'utilisateur unique |
| `email` | String | ✅ | Email unique |
| `password` | String | ✅ | Mot de passe |
| `fullName` | String | ✅ | Nom complet |

**Exemple de requête:**
```http
POST /api/auth/register?username=john&email=john@example.com&password=Pass123&fullName=John Doe
```

**Réponse 201 Created:**
```json
{
  "message": "Enregistrement réussi",
  "userId": "6073f6a9e5f3a12b3c4d5e6f",
  "username": "john",
  "email": "john@example.com"
}
```

**Erreurs:**
| Code | Cause |
|------|-------|
| `400` | Username ou email déjà utilisé |

---

### POST `/api/auth/login`
Authentifier un utilisateur et obtenir les tokens JWT.

**Accès :** Public

**Body (JSON):**
```json
{
  "email": "john@example.com",
  "password": "Pass123"
}
```

**Réponse 200 OK:**
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

**Erreurs:**
| Code | Cause |
|------|-------|
| `401` | Email ou mot de passe incorrect |
| `400` | Compte désactivé |

---

### POST `/api/auth/refresh`
Obtenir un nouvel access token avec un refresh token valide.

**Accès :** Public

**Paramètres (Query):**
| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `refreshToken` | String | ✅ | Refresh token JWT |

**Exemple de requête:**
```http
POST /api/auth/refresh?refreshToken=eyJhbGciOiJIUzUxMiJ9...
```

**Réponse 200 OK:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}
```

**Erreurs:**
| Code | Cause |
|------|-------|
| `400` | Refresh token invalide ou expiré |

---

### GET `/api/auth/validate`
Vérifier la validité d'un access token.

**Accès :** Public

**Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Réponse 200 OK:**
```json
{
  "valid": true,
  "message": "Token valide"
}
```

**Réponse 401 Unauthorized:**
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

**Réponse 200 OK:**
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

### GET `/api/users/profile`
Récupérer le profil de l'utilisateur connecté.

**Accès :** `ROLE_USER`, `ROLE_BUYER`, `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
```

**Réponse 200 OK:**
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

**Body (JSON):**
```json
{
  "fullName": "John Updated",
  "phone": "655999888",
  "address": "456 Nouvelle Avenue"
}
```

**Réponse 200 OK:** `UserDTO` mis à jour

---

### GET `/api/users/{id}`
Récupérer un utilisateur par son ID.

**Accès :** `ROLE_USER`, `ROLE_BUYER`, `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Paramètre (Path):**
| Paramètre | Type | Description |
|-----------|------|-------------|
| `id` | String | ID MongoDB de l'utilisateur |

**Exemple:**
```http
GET /api/users/6073f6a9e5f3a12b3c4d5e6f
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK:** `UserDTO`

**Erreurs:**
| Code | Cause |
|------|-------|
| `404` | Utilisateur non trouvé |

---

### GET `/api/users`
Récupérer la liste de tous les utilisateurs.

**Accès :** `ROLE_ADMIN` uniquement

**Réponse 200 OK:**
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

**Paramètre (Path):** `id` — ID de l'utilisateur

**Body (JSON):**
```json
{
  "fullName": "Nom Modifié",
  "phone": "655000111",
  "address": "Nouvelle adresse"
}
```

**Réponse 200 OK:** `UserDTO` mis à jour

---

### POST `/api/users/change-password`
Changer le mot de passe de l'utilisateur connecté.

**Accès :** `ROLE_USER`, `ROLE_BUYER`, `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Paramètres (Query):**
| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `oldPassword` | String | ✅ | Ancien mot de passe |
| `newPassword` | String | ✅ | Nouveau mot de passe |

**Exemple:**
```http
POST /api/users/change-password?oldPassword=Pass123&newPassword=NewPass456
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK:**
```json
{
  "message": "Mot de passe changé avec succès"
}
```

**Erreurs:**
| Code | Cause |
|------|-------|
| `400` | Ancien mot de passe incorrect |

---

### PUT `/api/users/{id}/disable`
Désactiver le compte d'un utilisateur.

**Accès :** `ROLE_ADMIN`

**Exemple:**
```http
PUT /api/users/6073f6a9e5f3a12b3c4d5e6f/disable
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK:** `UserDTO` avec `isActive: false`

---

### PUT `/api/users/{id}/enable`
Activer le compte d'un utilisateur.

**Accès :** `ROLE_ADMIN`

**Réponse 200 OK:** `UserDTO` avec `isActive: true`

---

### DELETE `/api/users/{id}`
Supprimer un utilisateur.

**Accès :** `ROLE_ADMIN`

**Réponse 200 OK:**
```json
{
  "message": "Utilisateur supprimé avec succès"
}
```

---

## 3. Propriétés — `/api/properties`

### GET `/api/properties/public`
Lister toutes les propriétés publiées et disponibles.

**Accès :** Public (sans token)

**Paramètres (Query):**
| Paramètre | Type | Défaut | Description |
|-----------|------|--------|-------------|
| `page` | Integer | `0` | Numéro de page |
| `size` | Integer | `10` | Taille de la page |

**Exemple:**
```http
GET /api/properties/public?page=0&size=10
```

**Réponse 200 OK:**
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
  "size": 10
}
```

---

### GET `/api/properties/search`
Rechercher des propriétés avec filtres.

**Accès :** Public (sans token)

**Paramètres (Query):**
| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `city` | String | ❌ | Filtrer par ville |
| `minPrice` | Double | ❌ | Prix minimum |
| `maxPrice` | Double | ❌ | Prix maximum |
| `bedrooms` | Integer | ❌ | Nombre minimum de chambres |
| `page` | Integer | ❌ | Numéro de page (défaut: 0) |
| `size` | Integer | ❌ | Taille de page (défaut: 10) |

**Exemple:**
```http
GET /api/properties/search?city=Douala&minPrice=5000000&maxPrice=50000000&bedrooms=3&page=0&size=5
```

**Réponse 200 OK:** Page de `PropertyDTO` (même format que `/public`)

---

### GET `/api/properties/{id}`
Récupérer une propriété par son ID.

**Accès :** Public (sans token)

**Paramètre (Path):**
| Paramètre | Type | Description |
|-----------|------|-------------|
| `id` | String | ID MongoDB de la propriété |

**Exemple:**
```http
GET /api/properties/6073f6a9e5f3a12b3c4d5e6f
```

**Réponse 200 OK:** `PropertyDTO`

**Erreurs:**
| Code | Cause |
|------|-------|
| `404` | Propriété non trouvée |

---

### POST `/api/properties`
Créer une nouvelle propriété.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Headers:**
```
Authorization: Bearer <ACCESS_TOKEN>
Content-Type: application/json
```

**Body (JSON):**
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

**Types de propriété (`propertyType`):**
`APARTMENT` | `HOUSE` | `LAND` | `COMMERCIAL` | `OFFICE` | `VILLA`

**Réponse 201 Created:** `PropertyDTO` créé

**Erreurs:**
| Code | Cause |
|------|-------|
| `400` | Champs requis manquants ou invalides |
| `401` | Token manquant ou invalide |
| `403` | Rôle insuffisant |

---

### PUT `/api/properties/{id}`
Mettre à jour une propriété existante.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN` (propriétaire uniquement)

**Paramètre (Path):** `id` — ID de la propriété

**Body (JSON):** Même format que la création

**Réponse 200 OK:** `PropertyDTO` mis à jour

**Erreurs:**
| Code | Cause |
|------|-------|
| `400` | Pas propriétaire de cette propriété |
| `404` | Propriété non trouvée |

---

### DELETE `/api/properties/{id}`
Supprimer une propriété.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN` (propriétaire uniquement)

**Exemple:**
```http
DELETE /api/properties/6073f6a9e5f3a12b3c4d5e6f
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK:**
```json
{
  "message": "Propriété supprimée avec succès"
}
```

---

### GET `/api/properties/owner/list`
Récupérer toutes les propriétés de l'utilisateur connecté.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN`

**Réponse 200 OK:** Liste de `PropertyDTO`

---

### PUT `/api/properties/{id}/status`
Changer le statut d'une propriété.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN` (propriétaire uniquement)

**Paramètres (Query):**
| Paramètre | Type | Valeurs possibles |
|-----------|------|------------------|
| `status` | String | `AVAILABLE` \| `RESERVED` \| `SOLD` \| `RENT` |

**Exemple:**
```http
PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/status?status=SOLD
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK:** `PropertyDTO` avec nouveau statut

---

### PUT `/api/properties/{id}/assign-agent`
Assigner un agent à une propriété.

**Accès :** `ROLE_SELLER`, `ROLE_ADMIN` (propriétaire uniquement)

**Paramètres (Query):**
| Paramètre | Type | Requis | Description |
|-----------|------|--------|-------------|
| `agentId` | String | ✅ | ID de l'agent à assigner |

**Exemple:**
```http
PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/assign-agent?agentId=abc123def456
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK:** `PropertyDTO` avec agent assigné

**Erreurs:**
| Code | Cause |
|------|-------|
| `404` | Agent non trouvé |

---

### PUT `/api/properties/{id}/publish`
Publier ou dépublier une propriété.

**Accès :** `ROLE_SELLER`, `ROLE_AGENT`, `ROLE_ADMIN` (propriétaire uniquement)

**Paramètres (Query):**
| Paramètre | Type | Description |
|-----------|------|-------------|
| `publish` | Boolean | `true` pour publier, `false` pour dépublier |

**Exemple:**
```http
PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/publish?publish=false
Authorization: Bearer <TOKEN>
```

**Réponse 200 OK:** `PropertyDTO` avec `isPublished` mis à jour

---

## Flux d'utilisation typique

```
1. S'enregistrer        → POST /api/auth/register
2. Se connecter         → POST /api/auth/login  (récupérer le token)
3. Consulter les biens  → GET  /api/properties/public
4. Rechercher           → GET  /api/properties/search?city=Douala
5. Créer une annonce    → POST /api/properties  (token SELLER/AGENT requis)
6. Gérer son profil     → GET  /api/users/profile
7. Rafraîchir le token  → POST /api/auth/refresh
```

---

## Résumé des endpoints

| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| `POST` | `/api/auth/register` | ❌ | Créer un compte |
| `POST` | `/api/auth/login` | ❌ | Se connecter |
| `POST` | `/api/auth/refresh` | ❌ | Rafraîchir le token |
| `GET` | `/api/auth/validate` | ❌ | Valider un token |
| `GET` | `/api/auth/health` | ❌ | Santé de l'API |
| `GET` | `/api/users/profile` | ✅ | Mon profil |
| `PUT` | `/api/users/profile` | ✅ | Modifier mon profil |
| `GET` | `/api/users/{id}` | ✅ | Profil par ID |
| `GET` | `/api/users` | ✅ ADMIN | Tous les utilisateurs |
| `PUT` | `/api/users/{id}` | ✅ ADMIN | Modifier un user |
| `POST` | `/api/users/change-password` | ✅ | Changer mot de passe |
| `PUT` | `/api/users/{id}/disable` | ✅ ADMIN | Désactiver un compte |
| `PUT` | `/api/users/{id}/enable` | ✅ ADMIN | Activer un compte |
| `DELETE` | `/api/users/{id}` | ✅ ADMIN | Supprimer un user |
| `GET` | `/api/properties/public` | ❌ | Propriétés publiées |
| `GET` | `/api/properties/search` | ❌ | Recherche avancée |
| `GET` | `/api/properties/{id}` | ❌ | Détail propriété |
| `POST` | `/api/properties` | ✅ SELLER/AGENT/ADMIN | Créer une propriété |
| `PUT` | `/api/properties/{id}` | ✅ SELLER/AGENT/ADMIN | Modifier une propriété |
| `DELETE` | `/api/properties/{id}` | ✅ SELLER/AGENT/ADMIN | Supprimer une propriété |
| `GET` | `/api/properties/owner/list` | ✅ SELLER/AGENT/ADMIN | Mes propriétés |
| `PUT` | `/api/properties/{id}/status` | ✅ SELLER/AGENT/ADMIN | Changer le statut |
| `PUT` | `/api/properties/{id}/assign-agent` | ✅ SELLER/ADMIN | Assigner un agent |
| `PUT` | `/api/properties/{id}/publish` | ✅ SELLER/AGENT/ADMIN | Publier/Dépublier |
