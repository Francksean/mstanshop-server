# MSTAN Shop — Backend E-commerce MVP

Backend REST pour une boutique en ligne, construit avec **Java 17**, **Spring Boot 3**, **Spring Data JPA**, **Spring Security (JWT + OAuth2 Google)**, **MapStruct** et **Spring Mail**.

## Sommaire

- [Architecture](#architecture)
- [Prérequis](#prérequis)
- [Configuration](#configuration)
- [Lancement](#lancement)
- [Documentation API (Swagger)](#documentation-api-swagger)
- [Comptes de test](#comptes-de-test)
- [Authentification](#authentification)

## Architecture

Le projet suit une **architecture feature-based** : chaque domaine métier possède son propre package avec sa mini architecture en couches (`controller`, `service`, `repository`, `entity`, `dto`, `mapper`).

```
com.mstan.shop
├── common/       config, sécurité JWT, gestion globale des exceptions, email, utilitaires
├── auth/         inscription, connexion, refresh token, reset password, OAuth2 Google
├── user/         profil utilisateur
├── category/     catégories de produits (CRUD admin + lecture publique)
├── product/      catalogue produits, upload d'images, partage Open Graph
├── cart/         panier persistant
└── order/        commandes, paiement simulé, gestion du stock, historique
```

## Prérequis

- Java 17+
- PostgreSQL 14+ (requis aussi bien en `dev` qu'en `prod` — seules les variables de connexion changent)
- Un compte SMTP (ex. [Mailtrap](https://mailtrap.io) en dev) pour tester l'envoi d'emails
- Des identifiants OAuth2 Google (optionnel, uniquement pour tester le login social)

Le projet embarque le **Maven Wrapper** (`mvnw` / `mvnw.cmd`) : aucune installation Maven globale n'est nécessaire, utilisez `./mvnw.cmd` (Windows) ou `./mvnw` (Linux/Mac) à la place de `mvn`.

## Configuration

La configuration est répartie entre `application.yml` (commun) et les profils `application-dev.yml` / `application-prod.yml`. Le profil actif est contrôlé par `SPRING_PROFILES_ACTIVE` (défaut : `dev`). Les deux profils utilisent PostgreSQL — seules les valeurs des variables changent entre dev et prod (base locale vs base de production, credentials différents, etc.).

### Chargement des variables via `.env`

Le projet utilise [`spring-dotenv`](https://github.com/paulschwarz/spring-dotenv) : au démarrage, Spring Boot charge automatiquement un fichier `.env` placé à la racine du projet (à côté de `pom.xml`), exactement comme s'il s'agissait de variables d'environnement.

Deux templates versionnés sont fournis :
- **`.env.dev`** — valeurs pour le développement local
- **`.env.prod`** — squelette pour la production (valeurs `change-me` à remplacer)

Pour lancer l'application, copier le template correspondant en `.env` (fichier ignoré par git, jamais commité) :

```bash
# Développement
cp .env.dev .env

# Production (à adapter avec les vraies valeurs avant de lancer)
cp .env.prod .env
```

> En production réelle, préférer l'injection des variables par la plateforme d'hébergement (secrets manager, variables d'environnement du conteneur) plutôt qu'un fichier `.env` sur le serveur.

### Variables d'environnement principales

| Variable | Description | Exemple (dev) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | Profil actif (`dev` ou `prod`) | `dev` |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | Connexion PostgreSQL | `jdbc:postgresql://localhost:5432/mstanshop_dev` |
| `JWT_SECRET` | Clé secrète de signature des JWT (256 bits min) | valeur de dev fournie — **à changer en prod** |
| `JWT_ACCESS_EXPIRATION` | Durée de vie de l'access token (ms) | 900000 (15 min) |
| `JWT_REFRESH_EXPIRATION` | Durée de vie du refresh token (ms) | 604800000 (7 jours) |
| `UPLOAD_DIR` | Répertoire de stockage des images produits uploadées | `uploads` |
| `FRONTEND_BASE_URL` | URL de base du frontend (liens email, partage produit) | `http://localhost:5173` |
| `FRONTEND_OAUTH_REDIRECT_URL` | URL de redirection après login OAuth2 réussi | `http://localhost:5173/oauth2/redirect` |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | Paramètres du serveur SMTP | Mailtrap sandbox |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | Identifiants OAuth2 Google | vide (login Google désactivé si absent) |
| `SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD` | Identifiants du compte admin créé au démarrage | `admin@mstan-shop.com` / `Admin123!` |

### Obtenir des identifiants OAuth2 Google

1. Aller sur [Google Cloud Console](https://console.cloud.google.com/) → APIs & Services → Credentials
2. Créer un identifiant OAuth 2.0 (type "Application Web")
3. Ajouter comme URI de redirection autorisée : `http://localhost:8080/login/oauth2/code/google`
4. Renseigner `GOOGLE_CLIENT_ID` et `GOOGLE_CLIENT_SECRET`

## Lancement

```bash
# Créer la base PostgreSQL locale (une seule fois)
createdb mstanshop_dev

# Copier le template d'environnement dev
cp .env.dev .env

# Compiler le projet
./mvnw.cmd clean compile

# Lancer l'application (lit .env automatiquement)
./mvnw.cmd spring-boot:run
```

Pour la production, remplacer `.env.dev` par `.env.prod` (après y avoir renseigné les vraies valeurs) et relancer les mêmes commandes.

L'application démarre sur `http://localhost:8080` (port configurable via `SERVER_PORT`).

Au démarrage, un `CommandLineRunner` crée automatiquement :
- les rôles `ROLE_USER` et `ROLE_ADMIN`
- un compte administrateur (`SEED_ADMIN_EMAIL` / `SEED_ADMIN_PASSWORD`)
- deux catégories et trois produits de démonstration

## Base de données et migrations (Flyway)

Le schéma de base de données n'est plus généré automatiquement par Hibernate
(`spring.jpa.hibernate.ddl-auto` est à `validate` dans tous les profils : Hibernate vérifie que
le schéma correspond aux entités, mais ne le modifie jamais). C'est [Flyway](https://flywaydb.org/)
qui crée et fait évoluer le schéma, via des scripts SQL versionnés dans
`src/main/resources/db/migration/`, appliqués automatiquement au démarrage de l'application
(avant même l'initialisation de Hibernate).

- `V1__baseline_schema.sql` : schéma de référence, à ne jamais modifier une fois appliqué.
- Pour toute évolution future (nouvelle table, nouvelle colonne...), créer un nouveau fichier
  `Vn__description.sql` (n = numéro suivant), jamais modifier un script déjà appliqué en base.
- La base doit exister au préalable (`createdb mstanshop_dev`) : Flyway crée les tables dans une
  base vide, mais ne crée pas la base elle-même.
- Flyway conserve son historique dans la table `flyway_schema_history` — c'est elle qui lui
  permet de savoir quelles migrations ont déjà été appliquées et de ne jamais les rejouer.

## Documentation API (Swagger)

Une fois l'application démarrée :

- Swagger UI : [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON : [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Utiliser le bouton **Authorize** dans Swagger UI avec un access token JWT (`Bearer <token>`) pour tester les endpoints protégés.

## Comptes de test

| Rôle | Email | Mot de passe |
|---|---|---|
| Admin | `admin@mstan-shop.com` (ou `SEED_ADMIN_EMAIL`) | `Admin123!` (ou `SEED_ADMIN_PASSWORD`) |
| Utilisateur | à créer via `POST /api/auth/register` | — |

## Authentification

### Inscription / connexion classique

```
POST /api/auth/register   { email, password, firstName, lastName, cartItems? }
POST /api/auth/login      { email, password, cartItems? }
POST /api/auth/refresh    { refreshToken }
POST /api/auth/logout     (authentifié — révoque tous les refresh tokens de l'utilisateur)
POST /api/auth/forgot-password  { email }
POST /api/auth/reset-password   { token, newPassword }
```

`cartItems` (optionnel, sur `register` et `login`) permet de fusionner un panier local/invité
(constitué côté frontend avant connexion, ex. dans le `localStorage`) dans le panier serveur du
compte au moment de l'inscription/connexion :
```json
"cartItems": [
  { "productId": "uuid", "variantId": "uuid-ou-null", "quantity": 2 }
]
```
La fusion est en "best effort" : les quantités s'additionnent si un produit/variante est déjà
dans le panier serveur, et une ligne invalide (produit/variante introuvable, stock insuffisant)
est simplement ignorée, sans faire échouer l'inscription/connexion. Le frontend peut ensuite
récupérer le panier fusionné via `GET /api/cart`.

Chaque réponse de connexion/inscription/refresh retourne :
```json
{ "accessToken": "...", "refreshToken": "...", "tokenType": "Bearer" }
```

L'access token doit être envoyé dans l'en-tête `Authorization: Bearer <accessToken>` pour les routes protégées.

### Connexion via Google

Rediriger le navigateur vers `GET /oauth2/authorization/google`. Après authentification réussie, le backend redirige vers `FRONTEND_OAUTH_REDIRECT_URL?token=<accessToken>&refresh=<refreshToken>`.

### Endpoints et rôles

| Route | Accès |
|---|---|
| `/api/auth/**` | Public |
| `GET /api/products/**`, `GET /api/categories/**` | Public |
| `/api/cart/**`, `/api/orders/**`, `/api/users/**` | Authentifié (`ROLE_USER` ou `ROLE_ADMIN`) |
| `/api/admin/**` | `ROLE_ADMIN` uniquement |

## Tester avec Postman/Swagger

1. `POST /api/auth/register` pour créer un compte, ou se connecter avec le compte admin de seed
2. Copier l'`accessToken` reçu et l'utiliser dans l'en-tête `Authorization: Bearer <token>`
3. Parcourir les produits (`GET /api/products`), ajouter au panier (`POST /api/cart/items`), passer commande (`POST /api/orders`)
4. En tant qu'admin, créer des produits/catégories et gérer le statut des commandes (`PATCH /api/admin/orders/{id}/status`)
