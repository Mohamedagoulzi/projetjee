# Plan de Synchronisation Backend-Frontend

## 📋 Analyse des Différences

### 1. Endpoints Manquants dans le Backend

#### Endpoints Vendeur (`/api/vendeur/*`)
- ❌ `GET /api/vendeur/produits` - Liste des produits du vendeur
- ❌ `GET /api/vendeur/produits/{id}` - Détails d'un produit
- ❌ `POST /api/vendeur/produits` - Créer un produit
- ❌ `PUT /api/vendeur/produits/{id}` - Modifier un produit
- ❌ `DELETE /api/vendeur/produits/{id}` - Supprimer un produit
- ❌ `GET /api/vendeur/produits/{id}/stats` - Statistiques d'un produit
- ❌ `GET /api/vendeur/produits/{id}/reviews` - Avis d'un produit
- ❌ `GET /api/vendeur/dashboard` - Dashboard overview
- ❌ `GET /api/vendeur/ventes/stats` - Stats de ventes par période
- ❌ `GET /api/vendeur/ventes` - Liste des ventes
- ❌ `GET /api/vendeur/ventes/{orderId}` - Détails d'une vente
- ❌ `GET /api/vendeur/dashboard/revenue-by-period` - Revenus par période
- ❌ `GET /api/vendeur/dashboard/low-stock` - Produits en rupture
- ❌ `GET /api/vendeur/dashboard/top-customers` - Top clients
- ❌ `GET /api/vendeur/dashboard/top-products-by-revenue` - Top produits par revenus

#### Endpoints Panier (`/api/panier/*`)
- ❌ `GET /api/panier` - Liste des articles du panier
- ❌ `POST /api/panier/add/{productId}?quantity=X` - Ajouter au panier
- ❌ `PUT /api/panier/{cartItemId}?quantity=X` - Modifier quantité
- ❌ `DELETE /api/panier/{cartItemId}` - Retirer du panier

#### Endpoints Produits (`/api/produits/*`)
- ✅ `GET /api/produits` - Existe
- ✅ `GET /api/produits/{id}` - Existe
- ✅ `POST /api/produits` - Existe (mais pour admin)
- ✅ `PUT /api/produits/{id}` - Existe (mais pour admin)
- ✅ `DELETE /api/produits/{id}` - Existe (mais pour admin)
- ❌ `GET /api/produits/{id}/reviews` - Avis d'un produit (acheteur)
- ❌ `POST /api/produits/{id}/reviews` - Créer un avis
- ❌ `DELETE /api/produits/reviews/{reviewId}` - Supprimer un avis
- ✅ `GET /api/produits/filter` - Existe (mais utilise `/search`)

### 2. Différences de Nommage dans les Modèles

#### Product Model
**Backend actuel:**
- `title` ✅
- `price` ✅
- `description` ✅
- `imageUrl` (String) ❌ Frontend attend `imageUrls` (Array)
- `rating` ✅
- `ratingCount` ✅
- ❌ `quantityAvailable` - MANQUANT (utilisé partout dans le frontend)

**Frontend attend:**
- `title` ✅
- `price` ✅
- `description` ✅
- `imageUrls` (Array) ou `images` (Array avec `{imageUrl}`)
- `quantityAvailable` ❌ MANQUANT
- `categorieId` ou `categorie.id`

### 3. Structure à Créer

## 🏗️ Ordre d'Implémentation Recommandé

### Phase 1: Modèle Product (PRIORITÉ 1)
1. ✅ Ajouter champ `quantityAvailable` au modèle `Product`
2. ✅ Créer migration SQL ou mettre à jour la table
3. ✅ Gérer `imageUrls` comme array (nouvelle table `product_images` ou JSON)

### Phase 2: VendeurController (PRIORITÉ 2)
1. ✅ Créer `VendeurController` avec base path `/api/vendeur`
2. ✅ Implémenter CRUD produits (`/produits`)
3. ✅ Filtrer par utilisateur connecté (vendeur)
4. ✅ Vérifier que le produit appartient au vendeur

### Phase 3: Endpoints Stats/Reviews (PRIORITÉ 3)
1. ✅ Créer `GET /api/vendeur/produits/{id}/stats`
2. ✅ Créer `GET /api/vendeur/produits/{id}/reviews`
3. ✅ Créer service `VendeurProduitStatsService`

### Phase 4: Dashboard Vendeur (PRIORITÉ 4)
1. ✅ Créer `VendeurDashboardController`
2. ✅ Implémenter tous les endpoints dashboard
3. ✅ Créer service `VendeurDashboardService`

### Phase 5: PanierController (PRIORITÉ 5)
1. ✅ Créer `PanierController` avec base path `/api/panier`
2. ✅ Implémenter CRUD panier
3. ✅ Filtrer par utilisateur connecté

### Phase 6: Endpoints Produits Acheteur (PRIORITÉ 6)
1. ✅ Ajouter `GET /api/produits/{id}/reviews` dans `ProduitController`
2. ✅ Ajouter `POST /api/produits/{id}/reviews`
3. ✅ Ajouter `DELETE /api/produits/reviews/{reviewId}`

## 📝 Notes Importantes

- **Sécurité**: Tous les endpoints vendeur doivent vérifier que l'utilisateur est bien un VENDEUR
- **Filtrage**: Les produits doivent être filtrés par `utilisateur_id` du vendeur connecté
- **Nommage**: Le backend utilise `Product`, le frontend attend parfois `produit` - adapter les DTOs
- **Images**: Décider si on utilise une table séparée ou un champ JSON pour les images multiples
