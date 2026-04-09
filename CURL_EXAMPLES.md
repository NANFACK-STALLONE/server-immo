# 📡 Guide Pratique - Exemples cURL pour l'API Immobilier

## Avant de Commencer

1. Démarrez l'application:
   ```bash
   mvn spring-boot:run
   ```

2. L'API sera disponible à: `http://localhost:8080/api`

3. Gardez un terminal séparé pour exécuter les commandes cURL

## 🔐 Authentification

### 1️⃣ Vérifier la santé de l'API
```bash
curl -X GET "http://localhost:8080/api/auth/health"
```

**Réponse attendue:**
```json
{
  "status": "UP",
  "message": "API Immobilier est en ligne",
  "timestamp": 1705325400000
}
```

### 2️⃣ Enregistrer un nouvel utilisateur
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '?username=alice&email=alice@example.com&password=SecurePass123&fullName=Alice Johnson'
```

**Commande plus lisible:**
```bash
curl -X POST "http://localhost:8080/api/auth/register" \
  --data-urlencode "username=alice" \
  --data-urlencode "email=alice@example.com" \
  --data-urlencode "password=SecurePass123" \
  --data-urlencode "fullName=Alice Johnson"
```

### 3️⃣ Se connecter (Login)
```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123"
  }'
```

**Réponse:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "id": 1,
    "username": "alice",
    "email": "alice@example.com",
    "fullName": "Alice Johnson",
    "role": "ROLE_USER"
  }
}
```

**⚠️ Important**: Sauvegardez le token `accessToken` pour les requêtes suivantes.

### 4️⃣ Utiliser le token pour les requêtes authentifiées

Remplacez `YOUR_ACCESS_TOKEN` par le token reçu ci-dessus:

```bash
# Sauvegarder le token dans une variable
TOKEN="YOUR_ACCESS_TOKEN"

# Vérifier que le token est valide
curl -X GET "http://localhost:8080/api/auth/validate" \
  -H "Authorization: Bearer $TOKEN"
```

### 5️⃣ Rafraîchir le token d'accès
```bash
REFRESH_TOKEN="YOUR_REFRESH_TOKEN"

curl -X POST "http://localhost:8080/api/auth/refresh?refreshToken=$REFRESH_TOKEN" \
  -H "Content-Type: application/json"
```

## 👤 Gestion des Utilisateurs

### 1️⃣ Obtenir le profil actuel
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X GET "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer $TOKEN"
```

### 2️⃣ Mettre à jour le profil
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Alice Marie Johnson",
    "phone": "+237691234567",
    "address": "123 rue Principale, Bamenda"
  }'
```

### 3️⃣ Changer le mot de passe
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X POST "http://localhost:8080/api/users/change-password" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d 'oldPassword=SecurePass123&newPassword=NewPass456' \
  --data-urlencode "oldPassword=SecurePass123" \
  --data-urlencode "newPassword=NewPass456"
```

### 4️⃣ Obtenir un utilisateur par ID
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X GET "http://localhost:8080/api/users/1" \
  -H "Authorization: Bearer $TOKEN"
```

### 5️⃣ Obtenir tous les utilisateurs (ADMIN uniquement)
```bash
# D'abord, enregistrer un admin
curl -X POST "http://localhost:8080/api/auth/register" \
  --data-urlencode "username=admin" \
  --data-urlencode "email=admin@example.com" \
  --data-urlencode "password=AdminPass123" \
  --data-urlencode "fullName=Admin User"

# Puis se connecter en tant qu'admin
# (Note: L'utilisateur créé par défaut a le rôle ROLE_USER, 
# vous devez modifier la base de données pour l'adapter)

curl -X GET "http://localhost:8080/api/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## 🏠 Gestion des Propriétés

### 1️⃣ Obtenir les propriétés publiées (Public - Pas d'authentification requise)
```bash
# Page 0, 10 éléments par page
curl -X GET "http://localhost:8080/api/properties/public?page=0&size=10"

# Avec pagination
curl -X GET "http://localhost:8080/api/properties/public?page=1&size=5"
```

### 2️⃣ Rechercher des propriétés
```bash
# Rechercher à Bamenda, prix entre 300k et 600k, avec 4 chambres
curl -X GET "http://localhost:8080/api/properties/search?city=Bamenda&minPrice=300000&maxPrice=600000&bedrooms=4&page=0&size=10"

# Rechercher uniquement par prix
curl -X GET "http://localhost:8080/api/properties/search?minPrice=400000&maxPrice=700000&page=0&size=10"

# Rechercher uniquement par ville
curl -X GET "http://localhost:8080/api/properties/search?city=Douala&page=0&size=10"
```

### 3️⃣ Obtenir une propriété par ID
```bash
curl -X GET "http://localhost:8080/api/properties/1"
```

### 4️⃣ Créer une propriété (Authentifié - ROLE_SELLER/AGENT/ADMIN)
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X POST "http://localhost:8080/api/properties" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Magnifique Maison Avec Vue",
    "description": "Maison spacieuse avec piscine et jardin, idéale pour la famille",
    "price": 450000.0,
    "area": 250.0,
    "bedrooms": 5,
    "bathrooms": 3,
    "propertyType": "HOUSE",
    "city": "Bamenda",
    "neighborhood": "Quartier Résidentiel",
    "address": "456 Avenue des Villas, Bamenda",
    "latitude": 3.8667,
    "longitude": 10.1567,
    "isPublished": true,
    "features": [
      "Piscine",
      "Jardin",
      "Garage",
      "Climatisation",
      "Vue Panoramique"
    ]
  }'
```

### 5️⃣ Mettre à jour une propriété
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X PUT "http://localhost:8080/api/properties/1" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Maison Luxe - Prix Réduit",
    "description": "Maison spacieuse avec piscine, jardin - RÉDUIT",
    "price": 420000.0,
    "area": 250.0,
    "bedrooms": 5,
    "bathrooms": 3,
    "propertyType": "HOUSE",
    "city": "Bamenda",
    "neighborhood": "Quartier Résidentiel",
    "address": "456 Avenue des Villas, Bamenda",
    "latitude": 3.8667,
    "longitude": 10.1567,
    "isPublished": true,
    "features": ["Piscine", "Jardin", "Garage", "Climatisation", "Vue Panoramique"]
  }'
```

### 6️⃣ Obtenir mes propriétés
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X GET "http://localhost:8080/api/properties/owner/list" \
  -H "Authorization: Bearer $TOKEN"
```

### 7️⃣ Changer le statut d'une propriété
```bash
TOKEN="YOUR_ACCESS_TOKEN"

# Marquer la propriété comme SOLD (Vendue)
curl -X PUT "http://localhost:8080/api/properties/1/status?status=SOLD" \
  -H "Authorization: Bearer $TOKEN"

# Options: AVAILABLE, RESERVED, SOLD, RENT
```

### 8️⃣ Assigner un agent à une propriété
```bash
TOKEN="YOUR_ACCESS_TOKEN"

# Assigner l'agent avec ID 2 à la propriété 1
curl -X PUT "http://localhost:8080/api/properties/1/assign-agent?agentId=2" \
  -H "Authorization: Bearer $TOKEN"
```

### 9️⃣ Publier/Dépublier une propriété
```bash
TOKEN="YOUR_ACCESS_TOKEN"

# Publier la propriété
curl -X PUT "http://localhost:8080/api/properties/1/publish?publish=true" \
  -H "Authorization: Bearer $TOKEN"

# Dépublier la propriété
curl -X PUT "http://localhost:8080/api/properties/1/publish?publish=false" \
  -H "Authorization: Bearer $TOKEN"
```

### 🔟 Supprimer une propriété
```bash
TOKEN="YOUR_ACCESS_TOKEN"

curl -X DELETE "http://localhost:8080/api/properties/1" \
  -H "Authorization: Bearer $TOKEN"
```

## 📊 Scénario d'Utilisation Complet

### Scénario: Alice vend une maison

```bash
#!/bin/bash

# 1. Alice s'enregistre
curl -X POST "http://localhost:8080/api/auth/register" \
  --data-urlencode "username=alice" \
  --data-urlencode "email=alice@example.com" \
  --data-urlencode "password=AlicePass123" \
  --data-urlencode "fullName=Alice Johnson"

echo "✓ Alice enregistrée"

# 2. Alice se connecte
LOGIN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice@example.com",
    "password": "AlicePass123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
echo "✓ Alice connectée"
echo "Token: $TOKEN"

# 3. Alice met à jour son profil
curl -s -X PUT "http://localhost:8080/api/users/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+237691111111",
    "address": "Bamenda, Cameroun"
  }'
echo "✓ Profil d'Alice mis à jour"

# 4. Alice crée une annonce de propriété
PROPERTY=$(curl -s -X POST "http://localhost:8080/api/properties" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Belle Maison à Vendre",
    "description": "Maison moderne avec 4 chambres",
    "price": 500000.0,
    "area": 200.0,
    "bedrooms": 4,
    "bathrooms": 2,
    "propertyType": "HOUSE",
    "city": "Bamenda",
    "neighborhood": "Downtown",
    "address": "123 Main St",
    "isPublished": true,
    "features": ["Piscine", "Garage"]
  }')

PROPERTY_ID=$(echo $PROPERTY | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
echo "✓ Propriété créée avec ID: $PROPERTY_ID"

# 5. Alice peut voir sa propriété
curl -s -X GET "http://localhost:8080/api/properties/$PROPERTY_ID" \
  -H "Authorization: Bearer $TOKEN"
echo "✓ Propriété trouvée"

# 6. Tout le monde peut voir la propriété publiée
curl -s -X GET "http://localhost:8080/api/properties/public?page=0&size=10"
echo "✓ Propriété visible publiquement"
```

## 🐛 Débogage

### Activer les logs détaillés
Modifiez `application.properties`:
```properties
logging.level.com.immobilier=DEBUG
logging.level.org.springframework.security=DEBUG
```

### Voir les requêtes SQL
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Consulter la base de données H2
```
URL: http://localhost:8080/api/h2-console
JDBC URL: jdbc:h2:mem:immobilierdb
User: sa
Password: (vide)
```

## 🔗 Liens Utiles

- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v3/api-docs
- **H2 Console**: http://localhost:8080/api/h2-console

## 💡 Conseils

1. **Sauvegarder les tokens**: Utilisez des variables d'environnement ou un fichier `.env`
2. **Tester avec Postman**: Importez le swagger pour des tests interactifs
3. **Vérifier les erreurs**: Consultez les logs de l'application
4. **Respecter les formats**: Les emails doivent être valides, les mots de passe sécurisés

## 📝 Notes

- Les tokens JWT expirent après 24 heures
- Les refresh tokens expirent après 7 jours
- Les mots de passe sont encodés avec BCrypt
- Les propriétés publiées sont visibles sans authentification
- Seuls les propriétaires peuvent modifier/supprimer leurs propriétés
