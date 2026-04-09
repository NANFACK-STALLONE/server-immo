const PDFDocument = require('pdfkit');
const fs = require('fs');
const path = require('path');

const outputPath = path.join(__dirname, 'API_DOCUMENTATION.pdf');
const doc = new PDFDocument({ margin: 50, size: 'A4', bufferPages: true });
const stream = fs.createWriteStream(outputPath);
doc.pipe(stream);

// ─── Colors ───────────────────────────────────────────────────────────────────
const COLOR_TITLE     = '#1a237e';   // dark navy
const COLOR_H1        = '#1565c0';   // blue
const COLOR_H2        = '#1976d2';   // medium blue
const COLOR_H3        = '#2196f3';   // lighter blue
const COLOR_CODE_BG   = '#f5f5f5';
const COLOR_CODE_TEXT = '#37474f';
const COLOR_TEXT      = '#212121';
const COLOR_SUBTLE    = '#757575';
const COLOR_BORDER    = '#e0e0e0';
const COLOR_METHOD_GET    = '#1976d2';
const COLOR_METHOD_POST   = '#388e3c';
const COLOR_METHOD_PUT    = '#f57c00';
const COLOR_METHOD_DELETE = '#d32f2f';
const COLOR_PUBLIC    = '#6a1b9a';
const COLOR_AUTH      = '#e65100';

// ─── Helpers ──────────────────────────────────────────────────────────────────
function methodColor(m) {
  if (m === 'GET')    return COLOR_METHOD_GET;
  if (m === 'POST')   return COLOR_METHOD_POST;
  if (m === 'PUT')    return COLOR_METHOD_PUT;
  if (m === 'DELETE') return COLOR_METHOD_DELETE;
  return '#333';
}

function line(doc, y, color = COLOR_BORDER) {
  doc.moveTo(50, y).lineTo(545, y).strokeColor(color).lineWidth(0.5).stroke();
}

function codeBlock(doc, code) {
  const lines = code.split('\n');
  const blockHeight = lines.length * 12 + 16;
  const y0 = doc.y;
  doc.rect(50, y0, 495, blockHeight).fill(COLOR_CODE_BG);
  doc.font('Courier').fontSize(8).fillColor(COLOR_CODE_TEXT);
  let cy = y0 + 8;
  lines.forEach(l => {
    doc.text(l, 62, cy, { lineBreak: false });
    cy += 12;
  });
  doc.y = y0 + blockHeight + 4;
  doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
}

function tableHeader(doc, cols) {
  // cols: [{text, width}]
  const y0 = doc.y;
  let x = 50;
  doc.rect(50, y0, 495, 18).fill('#e3f2fd');
  doc.font('Helvetica-Bold').fontSize(8.5).fillColor(COLOR_H1);
  cols.forEach(c => {
    doc.text(c.text, x + 4, y0 + 4, { width: c.width - 8, lineBreak: false });
    x += c.width;
  });
  doc.y = y0 + 18;
  doc.font('Helvetica').fontSize(8.5).fillColor(COLOR_TEXT);
}

function tableRow(doc, cols, values, even) {
  const y0 = doc.y;
  // calc height from longest text
  let maxH = 14;
  let x = 50;
  values.forEach((v, i) => {
    const h = doc.heightOfString(v, { width: cols[i].width - 8, fontSize: 8.5 });
    if (h + 6 > maxH) maxH = h + 6;
  });
  if (even) doc.rect(50, y0, 495, maxH).fill('#fafafa');
  doc.rect(50, y0, 495, maxH).strokeColor(COLOR_BORDER).lineWidth(0.3).stroke();
  x = 50;
  doc.font('Helvetica').fontSize(8.5).fillColor(COLOR_TEXT);
  values.forEach((v, i) => {
    doc.text(v, x + 4, y0 + 3, { width: cols[i].width - 8 });
    x += cols[i].width;
  });
  doc.y = y0 + maxH;
}

function ensureSpace(doc, needed) {
  if (doc.y + needed > 770) doc.addPage();
}

// ─── Cover Page ───────────────────────────────────────────────────────────────
doc.rect(0, 0, 595, 297).fill('#1a237e');
doc.rect(0, 297, 595, 545).fill('#ffffff');

// logo area
doc.font('Helvetica-Bold').fontSize(36).fillColor('#ffffff');
doc.text('🏠', 230, 80, { lineBreak: false });

doc.font('Helvetica-Bold').fontSize(28).fillColor('#ffffff');
doc.text('API REST Immobilier', 50, 130, { align: 'center', width: 495 });

doc.font('Helvetica').fontSize(14).fillColor('#bbdefb');
doc.text('Documentation Technique Complète', 50, 170, { align: 'center', width: 495 });

doc.font('Helvetica').fontSize(11).fillColor('#90caf9');
doc.text('Version 1.0.0  •  Spring Boot + MongoDB', 50, 200, { align: 'center', width: 495 });

// Info box
doc.rect(100, 320, 395, 130).fill('#f8f9fa').strokeColor('#e3f2fd').lineWidth(1).stroke();
const infoItems = [
  ['Base URL', 'http://localhost:8080/api'],
  ['Authentification', 'Bearer JWT Token'],
  ['Base de données', 'MongoDB'],
  ['Endpoints', '24 endpoints documentés'],
  ['Date', new Date().toLocaleDateString('fr-FR', { year:'numeric', month:'long', day:'numeric' })],
];
let iy = 330;
doc.font('Helvetica-Bold').fontSize(9).fillColor(COLOR_H1);
infoItems.forEach(([k, v]) => {
  doc.text(k + ' :', 115, iy, { continued: false, lineBreak: false });
  doc.font('Helvetica').fillColor(COLOR_TEXT).text('  ' + v, 195, iy, { lineBreak: false });
  doc.font('Helvetica-Bold').fillColor(COLOR_H1);
  iy += 18;
});

// footer line
doc.font('Helvetica').fontSize(9).fillColor(COLOR_SUBTLE);
doc.text('Généré automatiquement · API Immobilier · Cameroun', 50, 480, { align: 'center', width: 495 });

// ─── New page: Table of contents ──────────────────────────────────────────────
doc.addPage();
doc.font('Helvetica-Bold').fontSize(20).fillColor(COLOR_TITLE);
doc.text('Table des matières', 50, 50);
line(doc, doc.y + 4, COLOR_H1);
doc.moveDown(1);

const toc = [
  ['1.', 'Rôles & Permissions', 3],
  ['2.', 'Format des erreurs', 3],
  ['3.', 'Authentification  (/api/auth)', 4],
  ['  3.1', 'POST /api/auth/register', 4],
  ['  3.2', 'POST /api/auth/login', 4],
  ['  3.3', 'POST /api/auth/refresh', 5],
  ['  3.4', 'GET /api/auth/validate', 5],
  ['  3.5', 'GET /api/auth/health', 5],
  ['4.', 'Utilisateurs  (/api/users)', 6],
  ['  4.1', 'GET /api/users/profile', 6],
  ['  4.2', 'PUT /api/users/profile', 6],
  ['  4.3', 'GET /api/users/{id}', 6],
  ['  4.4', 'GET /api/users', 7],
  ['  4.5', 'PUT /api/users/{id}', 7],
  ['  4.6', 'POST /api/users/change-password', 7],
  ['  4.7', 'PUT /api/users/{id}/disable', 7],
  ['  4.8', 'PUT /api/users/{id}/enable', 7],
  ['  4.9', 'DELETE /api/users/{id}', 8],
  ['5.', 'Propriétés  (/api/properties)', 8],
  ['  5.1', 'GET /api/properties/public', 8],
  ['  5.2', 'GET /api/properties/search', 9],
  ['  5.3', 'GET /api/properties/{id}', 9],
  ['  5.4', 'POST /api/properties', 9],
  ['  5.5', 'PUT /api/properties/{id}', 10],
  ['  5.6', 'DELETE /api/properties/{id}', 10],
  ['  5.7', 'GET /api/properties/owner/list', 10],
  ['  5.8', 'PUT /api/properties/{id}/status', 10],
  ['  5.9', 'PUT /api/properties/{id}/assign-agent', 11],
  ['  5.10', 'PUT /api/properties/{id}/publish', 11],
  ['6.', 'Résumé des endpoints', 11],
  ['7.', 'Flux d\'utilisation typique', 12],
];
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
toc.forEach(([num, title, pg]) => {
  const isMain = !num.startsWith('  ');
  if (isMain) doc.font('Helvetica-Bold').fillColor(COLOR_H1);
  else doc.font('Helvetica').fillColor(COLOR_TEXT);
  doc.text(num, 60, doc.y, { continued: true, width: 50 });
  doc.text(title, { continued: true, width: 340 });
  doc.text('', { continued: false });
});

// ─── Page 3+: Content ──────────────────────────────────────────────────────────
doc.addPage();

// ── Section 1: Roles ──
doc.font('Helvetica-Bold').fontSize(18).fillColor(COLOR_H1);
doc.text('1. Rôles & Permissions', 50, 50);
line(doc, doc.y + 3, COLOR_H1);
doc.moveDown(0.8);

const roleCols = [{text:'Rôle', width:130}, {text:'Description', width:175}, {text:'Accès', width:190}];
tableHeader(doc, roleCols);
const roles = [
  ['ROLE_ADMIN', 'Administrateur', 'Tous les droits'],
  ['ROLE_AGENT', 'Agent immobilier', 'Gestion propriétés + consultation users'],
  ['ROLE_SELLER', 'Vendeur', 'Créer/modifier ses propriétés'],
  ['ROLE_BUYER', 'Acheteur', 'Consultation propriétés'],
  ['ROLE_USER', 'Utilisateur standard', 'Consultation propriétés publiques'],
];
roles.forEach((r, i) => tableRow(doc, roleCols, r, i % 2 === 0));
doc.moveDown(1.5);

// ── Section 2: Error format ──
ensureSpace(doc, 120);
doc.font('Helvetica-Bold').fontSize(18).fillColor(COLOR_H1);
doc.text('2. Format des erreurs', 50, doc.y);
line(doc, doc.y + 3, COLOR_H1);
doc.moveDown(0.8);
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Toutes les erreurs retournent le format JSON suivant :', 50, doc.y);
doc.moveDown(0.5);
codeBlock(doc, `{
  "status": 404,
  "error": "Ressource non trouvée",
  "message": "Propriété non trouvée avec l'ID: xxx",
  "path": "/api/properties/xxx",
  "timestamp": "2026-04-09T10:00:00",
  "details": null
}`);
doc.moveDown(1.5);

// ─── Helper to print an endpoint ──────────────────────────────────────────────
function endpointHeader(method, path2, access, desc) {
  ensureSpace(doc, 80);
  // Method badge
  const mx = 50;
  const my = doc.y;
  const mw = method.length * 7 + 10;
  doc.rect(mx, my, mw, 18).fill(methodColor(method));
  doc.font('Helvetica-Bold').fontSize(9).fillColor('#fff');
  doc.text(method, mx + 4, my + 4, { lineBreak: false });

  // Path
  doc.font('Courier-Bold').fontSize(12).fillColor(COLOR_TITLE);
  doc.text(path2, mx + mw + 8, my + 2, { lineBreak: false });

  // Access badge
  const ax = 545 - (access.length * 6 + 10);
  const ac = access.includes('Public') ? COLOR_PUBLIC : COLOR_AUTH;
  doc.rect(ax, my, access.length * 6 + 10, 16).fill(ac);
  doc.font('Helvetica-Bold').fontSize(8).fillColor('#fff');
  doc.text(access, ax + 4, my + 4, { lineBreak: false });

  doc.y = my + 22;
  doc.font('Helvetica').fontSize(10).fillColor(COLOR_SUBTLE);
  doc.text(desc, 50, doc.y);
  doc.moveDown(0.3);
  line(doc, doc.y, COLOR_BORDER);
  doc.moveDown(0.3);
  doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
}

function sectionTitle(num, title) {
  ensureSpace(doc, 60);
  doc.font('Helvetica-Bold').fontSize(18).fillColor(COLOR_H1);
  doc.text(num + '. ' + title, 50, doc.y);
  line(doc, doc.y + 3, COLOR_H1);
  doc.moveDown(0.5);
  doc.font('Helvetica').fontSize(10).fillColor(COLOR_SUBTLE);
  doc.text('Tous les endpoints de cette section nécessitent un Bearer Token JWT sauf mention contraire.', 50, doc.y);
  doc.moveDown(0.8);
}

function subSection(num, title) {
  ensureSpace(doc, 40);
  doc.font('Helvetica-Bold').fontSize(13).fillColor(COLOR_H2);
  doc.text(num + ' ' + title, 50, doc.y);
  doc.moveDown(0.4);
}

function label(txt) {
  doc.font('Helvetica-Bold').fontSize(10).fillColor(COLOR_H3);
  doc.text(txt, 50, doc.y);
  doc.moveDown(0.3);
}

function paramTable(cols, rows) {
  ensureSpace(doc, 30 + rows.length * 16);
  tableHeader(doc, cols);
  rows.forEach((r, i) => tableRow(doc, cols, r, i % 2 === 0));
  doc.moveDown(0.5);
}

// ─── Section 3: Auth ─────────────────────────────────────────────────────────
sectionTitle('3', 'Authentification — /api/auth');

// 3.1 register
subSection('3.1', 'POST /api/auth/register');
endpointHeader('POST', '/api/auth/register', 'Public', 'Créer un nouveau compte utilisateur.');
label('Paramètres (Query) :');
paramTable(
  [{text:'Paramètre',width:120},{text:'Type',width:80},{text:'Requis',width:70},{text:'Description',width:225}],
  [
    ['username','String','✅ Oui','Nom d\'utilisateur unique'],
    ['email','String','✅ Oui','Adresse email unique'],
    ['password','String','✅ Oui','Mot de passe'],
    ['fullName','String','✅ Oui','Nom complet'],
  ]
);
label('Exemple de requête :');
codeBlock(doc, 'POST /api/auth/register?username=john&email=john@example.com&password=Pass123&fullName=John Doe');
label('Réponse 201 Created :');
codeBlock(doc, `{
  "message": "Enregistrement réussi",
  "userId": "6073f6a9e5f3a12b3c4d5e6f",
  "username": "john",
  "email": "john@example.com"
}`);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [['400 Bad Request','Username ou email déjà utilisé']]
);
doc.moveDown(0.5);

// 3.2 login
subSection('3.2', 'POST /api/auth/login');
endpointHeader('POST', '/api/auth/login', 'Public', 'Authentifier un utilisateur et obtenir les tokens JWT.');
label('Body (JSON) :');
codeBlock(doc, `{
  "email": "john@example.com",
  "password": "Pass123"
}`);
label('Réponse 200 OK :');
codeBlock(doc, `{
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
}`);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [
    ['401 Unauthorized','Email ou mot de passe incorrect'],
    ['400 Bad Request','Compte désactivé'],
  ]
);
doc.moveDown(0.5);

// 3.3 refresh
subSection('3.3', 'POST /api/auth/refresh');
endpointHeader('POST', '/api/auth/refresh', 'Public', 'Obtenir un nouvel access token avec un refresh token valide.');
label('Paramètre (Query) :');
paramTable(
  [{text:'Paramètre',width:120},{text:'Type',width:80},{text:'Requis',width:70},{text:'Description',width:225}],
  [['refreshToken','String','✅ Oui','Refresh token JWT']]
);
label('Exemple :');
codeBlock(doc, 'POST /api/auth/refresh?refreshToken=eyJhbGciOiJIUzUxMiJ9...');
label('Réponse 200 OK :');
codeBlock(doc, `{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000
}`);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [['400 Bad Request','Refresh token invalide ou expiré']]
);
doc.moveDown(0.5);

// 3.4 validate
subSection('3.4', 'GET /api/auth/validate');
endpointHeader('GET', '/api/auth/validate', 'Public', 'Vérifier la validité d\'un access token.');
label('Headers requis :');
codeBlock(doc, 'Authorization: Bearer <ACCESS_TOKEN>');
label('Réponse 200 OK :');
codeBlock(doc, '{ "valid": true, "message": "Token valide" }');
label('Réponse 401 Unauthorized :');
codeBlock(doc, '{ "valid": false, "message": "Token invalide ou expiré" }');
doc.moveDown(0.5);

// 3.5 health
subSection('3.5', 'GET /api/auth/health');
endpointHeader('GET', '/api/auth/health', 'Public', 'Vérifier que l\'API est en ligne (health check).');
label('Réponse 200 OK :');
codeBlock(doc, `{
  "status": "UP",
  "message": "API Immobilier est en ligne",
  "timestamp": 1744185600000
}`);
doc.moveDown(1);

// ─── Section 4: Users ────────────────────────────────────────────────────────
doc.addPage();
sectionTitle('4', 'Utilisateurs — /api/users');

// 4.1
subSection('4.1', 'GET /api/users/profile');
endpointHeader('GET', '/api/users/profile', 'Authentifié', 'Récupérer le profil de l\'utilisateur connecté.');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Accès : ROLE_USER, ROLE_BUYER, ROLE_SELLER, ROLE_AGENT, ROLE_ADMIN', 50, doc.y);
doc.moveDown(0.3);
label('Headers :');
codeBlock(doc, 'Authorization: Bearer <ACCESS_TOKEN>');
label('Réponse 200 OK :');
codeBlock(doc, `{
  "id": "6073f6a9e5f3a12b3c4d5e6f",
  "username": "john",
  "email": "john@example.com",
  "fullName": "John Doe",
  "phone": "655123456",
  "address": "123 Rue Principale, Yaoundé",
  "role": "ROLE_USER",
  "createdAt": "2026-04-09T10:00:00",
  "isActive": true
}`);
doc.moveDown(0.5);

// 4.2
subSection('4.2', 'PUT /api/users/profile');
endpointHeader('PUT', '/api/users/profile', 'Authentifié', 'Mettre à jour le profil de l\'utilisateur connecté.');
label('Body (JSON) :');
codeBlock(doc, `{
  "fullName": "John Updated",
  "phone": "655999888",
  "address": "456 Nouvelle Avenue"
}`);
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : UserDTO mis à jour (même format que GET /profile)', 50, doc.y);
doc.moveDown(0.8);

// 4.3
subSection('4.3', 'GET /api/users/{id}');
endpointHeader('GET', '/api/users/{id}', 'Authentifié', 'Récupérer un utilisateur par son ID MongoDB.');
label('Paramètre (Path) :');
paramTable(
  [{text:'Paramètre',width:120},{text:'Type',width:80},{text:'Description',width:295}],
  [['id','String','ID MongoDB de l\'utilisateur']]
);
label('Exemple :');
codeBlock(doc, 'GET /api/users/6073f6a9e5f3a12b3c4d5e6f\nAuthorization: Bearer <TOKEN>');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : UserDTO', 50, doc.y);
doc.moveDown(0.3);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [['404 Not Found','Utilisateur non trouvé']]
);
doc.moveDown(0.5);

// 4.4
subSection('4.4', 'GET /api/users');
endpointHeader('GET', '/api/users', 'ROLE_ADMIN', 'Récupérer la liste de tous les utilisateurs.');
label('Réponse 200 OK :');
codeBlock(doc, `[
  {
    "id": "6073f6a9e5f3a12b3c4d5e6f",
    "username": "john",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "ROLE_USER",
    "isActive": true
  }
]`);
doc.moveDown(0.5);

// 4.5
subSection('4.5', 'PUT /api/users/{id}');
endpointHeader('PUT', '/api/users/{id}', 'ROLE_ADMIN', 'Mettre à jour un utilisateur (admin seulement).');
label('Body (JSON) :');
codeBlock(doc, `{
  "fullName": "Nom Modifié",
  "phone": "655000111",
  "address": "Nouvelle adresse"
}`);
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : UserDTO mis à jour', 50, doc.y);
doc.moveDown(0.8);

// 4.6
subSection('4.6', 'POST /api/users/change-password');
endpointHeader('POST', '/api/users/change-password', 'Authentifié', 'Changer le mot de passe de l\'utilisateur connecté.');
label('Paramètres (Query) :');
paramTable(
  [{text:'Paramètre',width:120},{text:'Type',width:80},{text:'Requis',width:70},{text:'Description',width:225}],
  [
    ['oldPassword','String','✅ Oui','Ancien mot de passe'],
    ['newPassword','String','✅ Oui','Nouveau mot de passe'],
  ]
);
label('Exemple :');
codeBlock(doc, 'POST /api/users/change-password?oldPassword=Pass123&newPassword=NewPass456\nAuthorization: Bearer <TOKEN>');
label('Réponse 200 OK :');
codeBlock(doc, '{ "message": "Mot de passe changé avec succès" }');
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [['400 Bad Request','Ancien mot de passe incorrect']]
);
doc.moveDown(0.5);

// 4.7
subSection('4.7', 'PUT /api/users/{id}/disable');
endpointHeader('PUT', '/api/users/{id}/disable', 'ROLE_ADMIN', 'Désactiver le compte d\'un utilisateur.');
label('Exemple :');
codeBlock(doc, 'PUT /api/users/6073f6a9e5f3a12b3c4d5e6f/disable\nAuthorization: Bearer <TOKEN>');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : UserDTO avec isActive: false', 50, doc.y);
doc.moveDown(0.8);

// 4.8
subSection('4.8', 'PUT /api/users/{id}/enable');
endpointHeader('PUT', '/api/users/{id}/enable', 'ROLE_ADMIN', 'Activer le compte d\'un utilisateur.');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : UserDTO avec isActive: true', 50, doc.y);
doc.moveDown(0.8);

// 4.9
subSection('4.9', 'DELETE /api/users/{id}');
endpointHeader('DELETE', '/api/users/{id}', 'ROLE_ADMIN', 'Supprimer définitivement un utilisateur.');
label('Réponse 200 OK :');
codeBlock(doc, '{ "message": "Utilisateur supprimé avec succès" }');
doc.moveDown(1);

// ─── Section 5: Properties ───────────────────────────────────────────────────
doc.addPage();
sectionTitle('5', 'Propriétés — /api/properties');

// 5.1
subSection('5.1', 'GET /api/properties/public');
endpointHeader('GET', '/api/properties/public', 'Public', 'Lister toutes les propriétés publiées et disponibles (paginé).');
label('Paramètres (Query) :');
paramTable(
  [{text:'Paramètre',width:100},{text:'Type',width:80},{text:'Défaut',width:80},{text:'Description',width:235}],
  [
    ['page','Integer','0','Numéro de page (commence à 0)'],
    ['size','Integer','10','Nombre d\'éléments par page'],
  ]
);
label('Exemple :');
codeBlock(doc, 'GET /api/properties/public?page=0&size=10');
label('Réponse 200 OK :');
codeBlock(doc, `{
  "content": [
    {
      "id": "6073f6a9e5f3a12b3c4d5e6f",
      "title": "Belle Villa à Bastos",
      "price": 150000000.0,
      "area": 350.0,
      "bedrooms": 5,
      "bathrooms": 3,
      "propertyType": "VILLA",
      "city": "Yaoundé",
      "neighborhood": "Bastos",
      "status": "AVAILABLE",
      "ownerId": "abc123",
      "ownerName": "Jean Dupont",
      "isPublished": true,
      "features": ["Piscine", "Jardin", "Garage"]
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "number": 0,
  "size": 10
}`);
doc.moveDown(0.5);

// 5.2
subSection('5.2', 'GET /api/properties/search');
endpointHeader('GET', '/api/properties/search', 'Public', 'Rechercher des propriétés avec filtres dynamiques.');
label('Paramètres (Query) :');
paramTable(
  [{text:'Paramètre',width:100},{text:'Type',width:80},{text:'Requis',width:70},{text:'Description',width:245}],
  [
    ['city','String','❌ Non','Filtrer par ville (ex: Douala)'],
    ['minPrice','Double','❌ Non','Prix minimum en FCFA'],
    ['maxPrice','Double','❌ Non','Prix maximum en FCFA'],
    ['bedrooms','Integer','❌ Non','Nombre minimum de chambres'],
    ['page','Integer','❌ Non','Page (défaut: 0)'],
    ['size','Integer','❌ Non','Taille de page (défaut: 10)'],
  ]
);
label('Exemple :');
codeBlock(doc, 'GET /api/properties/search?city=Douala&minPrice=5000000&maxPrice=50000000&bedrooms=3&page=0&size=5');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : Page de PropertyDTO (même format que /public)', 50, doc.y);
doc.moveDown(0.8);

// 5.3
subSection('5.3', 'GET /api/properties/{id}');
endpointHeader('GET', '/api/properties/{id}', 'Public', 'Récupérer le détail d\'une propriété par son ID.');
label('Paramètre (Path) :');
paramTable(
  [{text:'Paramètre',width:120},{text:'Type',width:80},{text:'Description',width:295}],
  [['id','String','ID MongoDB de la propriété']]
);
label('Exemple :');
codeBlock(doc, 'GET /api/properties/6073f6a9e5f3a12b3c4d5e6f');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : PropertyDTO', 50, doc.y);
doc.moveDown(0.3);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [['404 Not Found','Propriété non trouvée']]
);
doc.moveDown(0.5);

// 5.4
subSection('5.4', 'POST /api/properties');
endpointHeader('POST', '/api/properties', 'SELLER/AGENT/ADMIN', 'Créer une nouvelle propriété.');
label('Headers :');
codeBlock(doc, 'Authorization: Bearer <ACCESS_TOKEN>\nContent-Type: application/json');
label('Body (JSON) :');
codeBlock(doc, `{
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
}`);
label('Types de propriété (propertyType) :');
doc.font('Courier').fontSize(9).fillColor(COLOR_CODE_TEXT);
doc.text('APARTMENT  |  HOUSE  |  LAND  |  COMMERCIAL  |  OFFICE  |  VILLA', 62, doc.y);
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.moveDown(0.5);
doc.text('Réponse 201 Created : PropertyDTO créé', 50, doc.y);
doc.moveDown(0.3);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [
    ['400 Bad Request','Champs requis manquants ou invalides'],
    ['401 Unauthorized','Token manquant ou invalide'],
    ['403 Forbidden','Rôle insuffisant (pas SELLER/AGENT/ADMIN)'],
  ]
);
doc.moveDown(0.5);

// 5.5
subSection('5.5', 'PUT /api/properties/{id}');
endpointHeader('PUT', '/api/properties/{id}', 'SELLER/AGENT/ADMIN', 'Mettre à jour une propriété existante (propriétaire uniquement).');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Body (JSON) : même format que la création (POST /api/properties)', 50, doc.y);
doc.moveDown(0.3);
doc.text('Réponse 200 OK : PropertyDTO mis à jour', 50, doc.y);
doc.moveDown(0.3);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [
    ['400 Bad Request','Vous n\'êtes pas propriétaire de cette propriété'],
    ['404 Not Found','Propriété non trouvée'],
  ]
);
doc.moveDown(0.5);

// 5.6
subSection('5.6', 'DELETE /api/properties/{id}');
endpointHeader('DELETE', '/api/properties/{id}', 'SELLER/AGENT/ADMIN', 'Supprimer une propriété (propriétaire uniquement).');
label('Exemple :');
codeBlock(doc, 'DELETE /api/properties/6073f6a9e5f3a12b3c4d5e6f\nAuthorization: Bearer <TOKEN>');
label('Réponse 200 OK :');
codeBlock(doc, '{ "message": "Propriété supprimée avec succès" }');
doc.moveDown(0.5);

// 5.7
subSection('5.7', 'GET /api/properties/owner/list');
endpointHeader('GET', '/api/properties/owner/list', 'SELLER/AGENT/ADMIN', 'Récupérer toutes les propriétés de l\'utilisateur connecté.');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : Liste de PropertyDTO appartenant à l\'utilisateur authentifié.', 50, doc.y);
doc.moveDown(0.8);

// 5.8
subSection('5.8', 'PUT /api/properties/{id}/status');
endpointHeader('PUT', '/api/properties/{id}/status', 'SELLER/AGENT/ADMIN', 'Changer le statut d\'une propriété.');
label('Paramètre (Query) :');
paramTable(
  [{text:'Paramètre',width:100},{text:'Type',width:80},{text:'Valeurs possibles',width:315}],
  [['status','String','AVAILABLE | RESERVED | SOLD | RENT']]
);
label('Exemple :');
codeBlock(doc, 'PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/status?status=SOLD\nAuthorization: Bearer <TOKEN>');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : PropertyDTO avec le nouveau statut', 50, doc.y);
doc.moveDown(0.8);

// 5.9
subSection('5.9', 'PUT /api/properties/{id}/assign-agent');
endpointHeader('PUT', '/api/properties/{id}/assign-agent', 'SELLER/ADMIN', 'Assigner un agent immobilier à une propriété.');
label('Paramètre (Query) :');
paramTable(
  [{text:'Paramètre',width:120},{text:'Type',width:80},{text:'Requis',width:70},{text:'Description',width:225}],
  [['agentId','String','✅ Oui','ID MongoDB de l\'agent à assigner']]
);
label('Exemple :');
codeBlock(doc, 'PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/assign-agent?agentId=abc123def456\nAuthorization: Bearer <TOKEN>');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : PropertyDTO avec l\'agent assigné', 50, doc.y);
doc.moveDown(0.3);
label('Erreurs :');
paramTable(
  [{text:'Code HTTP',width:100},{text:'Cause',width:395}],
  [['404 Not Found','Agent non trouvé ou n\'a pas le rôle ROLE_AGENT']]
);
doc.moveDown(0.5);

// 5.10
subSection('5.10', 'PUT /api/properties/{id}/publish');
endpointHeader('PUT', '/api/properties/{id}/publish', 'SELLER/AGENT/ADMIN', 'Publier ou dépublier une propriété.');
label('Paramètre (Query) :');
paramTable(
  [{text:'Paramètre',width:100},{text:'Type',width:80},{text:'Description',width:315}],
  [['publish','Boolean','true pour publier, false pour dépublier']]
);
label('Exemple :');
codeBlock(doc, 'PUT /api/properties/6073f6a9e5f3a12b3c4d5e6f/publish?publish=false\nAuthorization: Bearer <TOKEN>');
doc.font('Helvetica').fontSize(10).fillColor(COLOR_TEXT);
doc.text('Réponse 200 OK : PropertyDTO avec isPublished mis à jour', 50, doc.y);
doc.moveDown(1);

// ─── Section 6: Summary table ────────────────────────────────────────────────
doc.addPage();
doc.font('Helvetica-Bold').fontSize(18).fillColor(COLOR_H1);
doc.text('6. Résumé des endpoints', 50, 50);
line(doc, doc.y + 3, COLOR_H1);
doc.moveDown(0.8);

const sumCols = [{text:'Méthode',width:65},{text:'Endpoint',width:195},{text:'Auth',width:80},{text:'Description',width:155}];
tableHeader(doc, sumCols);
const summary = [
  ['POST','/api/auth/register','❌ Public','Créer un compte'],
  ['POST','/api/auth/login','❌ Public','Se connecter'],
  ['POST','/api/auth/refresh','❌ Public','Rafraîchir le token'],
  ['GET','/api/auth/validate','❌ Public','Valider un token'],
  ['GET','/api/auth/health','❌ Public','Santé de l\'API'],
  ['GET','/api/users/profile','✅ Tous','Mon profil'],
  ['PUT','/api/users/profile','✅ Tous','Modifier mon profil'],
  ['GET','/api/users/{id}','✅ Tous','Profil par ID'],
  ['GET','/api/users','✅ ADMIN','Tous les utilisateurs'],
  ['PUT','/api/users/{id}','✅ ADMIN','Modifier un user'],
  ['POST','/api/users/change-password','✅ Tous','Changer mot de passe'],
  ['PUT','/api/users/{id}/disable','✅ ADMIN','Désactiver un compte'],
  ['PUT','/api/users/{id}/enable','✅ ADMIN','Activer un compte'],
  ['DELETE','/api/users/{id}','✅ ADMIN','Supprimer un user'],
  ['GET','/api/properties/public','❌ Public','Propriétés publiées'],
  ['GET','/api/properties/search','❌ Public','Recherche avancée'],
  ['GET','/api/properties/{id}','❌ Public','Détail propriété'],
  ['POST','/api/properties','✅ SELLER/AGENT','Créer une propriété'],
  ['PUT','/api/properties/{id}','✅ SELLER/AGENT','Modifier une propriété'],
  ['DELETE','/api/properties/{id}','✅ SELLER/AGENT','Supprimer une propriété'],
  ['GET','/api/properties/owner/list','✅ SELLER/AGENT','Mes propriétés'],
  ['PUT','/api/properties/{id}/status','✅ SELLER/AGENT','Changer le statut'],
  ['PUT','/api/properties/{id}/assign-agent','✅ SELLER/ADMIN','Assigner un agent'],
  ['PUT','/api/properties/{id}/publish','✅ SELLER/AGENT','Publier/Dépublier'],
];
summary.forEach((r, i) => {
  ensureSpace(doc, 20);
  const y0 = doc.y;
  const mh = 16;
  if (i % 2 === 0) doc.rect(50, y0, 495, mh).fill('#fafafa');
  doc.rect(50, y0, 495, mh).strokeColor(COLOR_BORDER).lineWidth(0.3).stroke();
  // Method colored
  const mc = methodColor(r[0]);
  doc.rect(52, y0 + 2, 58, 12).fill(mc);
  doc.font('Helvetica-Bold').fontSize(7.5).fillColor('#fff');
  doc.text(r[0], 54, y0 + 4, { width: 56, lineBreak: false });
  // rest of columns
  doc.font('Courier').fontSize(8).fillColor(COLOR_CODE_TEXT);
  doc.text(r[1], 117, y0 + 3, { width: 190, lineBreak: false });
  doc.font('Helvetica').fontSize(8).fillColor(r[2].includes('❌') ? COLOR_PUBLIC : COLOR_AUTH);
  doc.text(r[2], 313, y0 + 3, { width: 76, lineBreak: false });
  doc.font('Helvetica').fontSize(8).fillColor(COLOR_TEXT);
  doc.text(r[3], 391, y0 + 3, { width: 150, lineBreak: false });
  doc.y = y0 + mh;
});

doc.moveDown(1.5);

// ─── Section 7: Typical flow ─────────────────────────────────────────────────
ensureSpace(doc, 120);
doc.font('Helvetica-Bold').fontSize(18).fillColor(COLOR_H1);
doc.text('7. Flux d\'utilisation typique', 50, doc.y);
line(doc, doc.y + 3, COLOR_H1);
doc.moveDown(0.8);

const steps = [
  ['1', 'S\'enregistrer', 'POST /api/auth/register', 'Créer un compte avec username, email, password, fullName'],
  ['2', 'Se connecter', 'POST /api/auth/login', 'Récupérer accessToken (24h) et refreshToken (7 jours)'],
  ['3', 'Consulter les biens', 'GET /api/properties/public', 'Lister les propriétés sans authentification'],
  ['4', 'Rechercher', 'GET /api/properties/search?city=Douala', 'Filtrer par ville, prix, chambres'],
  ['5', 'Créer une annonce', 'POST /api/properties', 'Token SELLER/AGENT requis dans Authorization header'],
  ['6', 'Gérer son profil', 'GET /api/users/profile', 'Consulter ou modifier ses informations'],
  ['7', 'Rafraîchir le token', 'POST /api/auth/refresh', 'Obtenir un nouveau accessToken avant expiration'],
];

steps.forEach((s, i) => {
  ensureSpace(doc, 50);
  const y0 = doc.y;
  // Step circle
  doc.circle(65, y0 + 12, 12).fill(COLOR_H1);
  doc.font('Helvetica-Bold').fontSize(11).fillColor('#fff');
  doc.text(s[0], 59, y0 + 6, { lineBreak: false });
  // Content
  doc.rect(85, y0, 460, 38).fill('#f8f9fa').strokeColor('#e3f2fd').lineWidth(0.5).stroke();
  doc.font('Helvetica-Bold').fontSize(10).fillColor(COLOR_TITLE);
  doc.text(s[1], 92, y0 + 5, { lineBreak: false });
  doc.font('Courier').fontSize(9).fillColor(COLOR_H2);
  doc.text(s[2], 200, y0 + 5, { lineBreak: false });
  doc.font('Helvetica').fontSize(9).fillColor(COLOR_SUBTLE);
  doc.text(s[3], 92, y0 + 20, { width: 450 });
  doc.y = y0 + 44;
});

doc.moveDown(1.5);

// ─── Footer note ──────────────────────────────────────────────────────────────
ensureSpace(doc, 80);
doc.rect(50, doc.y, 495, 60).fill('#e8eaf6').strokeColor('#3f51b5').lineWidth(1).stroke();
doc.font('Helvetica-Bold').fontSize(11).fillColor(COLOR_TITLE);
doc.text('Notes importantes', 62, doc.y + 8);
doc.font('Helvetica').fontSize(9.5).fillColor(COLOR_TEXT);
doc.text(
  '• Tous les tokens Bearer doivent être inclus dans le header Authorization: Bearer <TOKEN>\n' +
  '• Les IDs sont des ObjectId MongoDB (chaînes hexadécimales de 24 caractères)\n' +
  '• Le serveur écoute sur http://localhost:8080 (ou configuré via SPRING_DATA_MONGODB_URI)\n' +
  '• En production, utiliser HTTPS et définir JWT_SECRET avec une clé d\'au moins 32 caractères',
  62, doc.y + 4, { width: 475 }
);

// ─── Page numbers ────────────────────────────────────────────────────────────
const pageCount = doc.bufferedPageRange().count;
for (let i = 0; i < pageCount; i++) {
  doc.switchToPage(i);
  doc.font('Helvetica').fontSize(8).fillColor(COLOR_SUBTLE);
  doc.text(
    `API REST Immobilier — Documentation v1.0.0${' '.repeat(60)}Page ${i + 1} / ${pageCount}`,
    50, 820, { width: 495, align: 'center' }
  );
}

doc.end();
stream.on('finish', () => console.log('PDF généré avec succès: ' + outputPath));
stream.on('error', err => console.error('Erreur:', err));
