# RestaurantGestion

<p align="center">
  <img src="https://img.shields.io/badge/Java-22-ED8B00?logo=openjdk&logoColor=white" alt="Java 22"/>
  <img src="https://img.shields.io/badge/JavaFX-21.0.6-007396?logo=openjdk&logoColor=white" alt="JavaFX 21"/>
  <img src="https://img.shields.io/badge/Maven-3.8.7-C71A36?logo=apachemaven&logoColor=white" alt="Maven 3.8"/>
  <img src="https://img.shields.io/badge/Hibernate-6.5.2-59666C?logo=hibernate&logoColor=white" alt="Hibernate 6.5"/>
  <img src="https://img.shields.io/badge/PostgreSQL-42.7.3-4169E1?logo=postgresql&logoColor=white" alt="PostgreSQL 42"/>
  <img src="https://img.shields.io/badge/Lombok-1.18.34-FF69B4" alt="Lombok 1.18"/>
  <img src="https://img.shields.io/badge/iText-5.5.13-0892D0" alt="iText 5.5"/>
  <img src="https://img.shields.io/badge/Docker-29.2.1-2496ED?logo=docker&logoColor=white" alt="Docker 29"/>
  <img src="https://img.shields.io/badge/JUnit-5.12.1-25A162?logo=junit5&logoColor=white" alt="JUnit 5"/>
</p>

Application de gestion de restaurant (ERP léger) — interface desktop JavaFX, base de données PostgreSQL via Hibernate/JPA.

---

## Sommaire

- [Technologies](#technologies)
- [Architecture du projet](#architecture-du-projet)
- [Structure des packages](#structure-des-packages)
- [Fonctionnalités](#fonctionnalités)
- [Prérequis](#prérequis)
- [Installation et lancement](#installation-et-lancement)
- [Base de données](#base-de-données)
- [Logique métier](#logique-métier)
- [Captures d'écran](#captures-décran)

---

## Technologies

| Technologie | Version | Rôle |
|-------------|---------|------|
| Java | 22 | Langage |
| JavaFX | 21.0.6 | Interface graphique |
| Maven | 3.8.7 | Build & dépendances |
| Hibernate ORM | 6.5.2.Final | ORM / persistance |
| Jakarta Persistence | 3.2.0 | API JPA |
| PostgreSQL Driver | 42.7.3 | Connecteur JDBC |
| Lombok | 1.18.34 | Réduction de code (getters/setters) |
| iTextPDF | 5.5.13.3 | Génération de factures PDF |
| JUnit Jupiter | 5.12.1 | Tests unitaires |
| Maven Compiler Plugin | 3.13.0 | Compilation |
| JavaFX Maven Plugin | 0.0.8 | Lancement JavaFX |
| Docker | 29.2.1 | Conteneurisation PostgreSQL |
| Docker Compose | v2.26.1 | Orchestration conteneur |

---

## Architecture du projet

```
RestaurantGestion/
├── docker-compose.yml        # Base PostgreSQL conteneurisée
├── docker/init.sql           # Script d'initialisation BDD
├── pom.xml                   # Configuration Maven
├── disp/                     # Ressources SVG (icônes)
│   ├── logo.svg
│   ├── menu.svg
│   ├── table.svg
│   ├── commande.svg
│   └── profil.svg
└── src/main/
    ├── java/.../models/      # Entités JPA (Produit, Commande, Table, etc.)
    ├── java/.../dao/         # Accès aux données (CRUD)
    ├── java/.../views/       # Composants d'interface (JavaFX)
    ├── java/.../util/        # Utilitaires (HibernateUtil)
    ├── java/module-info.java # Module Java (JPMS)
    └── resources/.../css/    # Styles CSS
```

### Modèle MVC simplifié

- **Modèle** (`models/`) : Entités JPA mappées aux tables PostgreSQL
- **Vue** (`views/`) : Composants JavaFX construits en pur code (pas de FXML)
- **Contrôleur** : Logique métier intégrée dans les DAO + gestionnaires d'événements dans les vues

### Flux de navigation

```
MainView (BorderPane)
├── SidebarView (gauche) — Navigation par icônes SVG
├── HeaderView (haut)    — Logo + titre + profil
└── StackPane (centre)   — Vue active
    ├── DashboardView    — KPIs, graphiques, commandes récentes
    ├── TablesView       — Plan de salle (FlowPane de cartes)
    ├── MenuView         — Gestion de la carte (TableView)
    ├── CommandesView    — Commandes & facturation (TableView)
    ├── PaiementsView    — Caisse / encaissement
    ├── StocksView       — Gestion des stocks
    └── VentesView       — Statistiques de ventes
```

---

## Structure des packages

### `models/` — Entités JPA

| Classe | Table PostgreSQL | Description |
|--------|-----------------|-------------|
| `Produit` | `produits` | Plats, boissons, desserts (nom, prix, catégorie, description, image) |
| `Commande` | `commandes` | Commandes clients (table, date, total, statut) |
| `TableRestaurant` | `tables_restaurant` | Tables (numéro, capacité, statut : Libre/Occupée/Réservée) |
| `LigneCommande` | `lignes_commande` | Lignes de commande (produit, quantité, prix) |
| `Facture` | `factures` | Factures générées |
| `Paiement` | `paiements` | Paiements enregistrés |
| `Stock` | `stocks` | Ingrédients en stock |

### `dao/` — Accès aux données

| Classe | Méthodes principales |
|--------|---------------------|
| `ProduitDAO` | `getAllProduits()`, `ajouterProduit()` |
| `CommandeDAO` | `getHistoriqueVentes()`, `mettreAJourStatut()`, `genererFacturePDF()` |
| `TableDAO` | `getAllTables()`, `ajouterTable()`, `modifierTable()`, `supprimerTable()` |
| `PaiementDAO` | `validerPaiement()` |
| `IngredientDAO` | Gestion des ingrédients |
| `LigneCommandeDAO` | Lignes de commande |

### `views/` — Interface JavaFX

| Vue | Type | Description |
|-----|------|-------------|
| `MainView` | `BorderPane` | Structure principale (header + sidebar + content) |
| `SidebarView` | `VBox` | Navigation latérale avec icônes SVG |
| `HeaderView` | `HBox` | Barre d'en-tête (logo, titre, profil) |
| `DashboardView` | `VBox` | Tableau de bord (KPIs, BarChart, PieChart, commandes récentes) |
| `TablesView` | `VBox` | Plan de salle (FlowPane de cartes) |
| `MenuView` | `VBox` | Gestion de la carte (TableView) |
| `CommandesView` | `VBox` | Commandes & facturation (TableView) |
| `PaiementsView` | `VBox` | Caisse / encaissement |
| `StocksView` | `VBox` | Gestion des stocks |
| `VentesView` | `VBox` | Statistiques (BarChart) |
| `ProductDialog` | `Stage` | Dialogue d'ajout de produit |
| `CommandeDialog` | `Stage` | Dialogue de nouvelle commande |
| `TableDialog` | `Stage` | Dialogue d'ajout de table |

---

## Fonctionnalités

### Dashboard
- 4 indicateurs clés : tables disponibles, CA du jour, commandes en cours, alertes stock
- BarChart d'évolution du CA sur 7 jours
- PieChart d'occupation des tables
- Liste des 8 dernières commandes (interactive)

### Gestion de la carte (Menu)
- Tableau complet des produits avec filtres par catégorie
- Badges de catégorie colorés (Entrée/Plat/Dessert/Boisson)
- Vue détaillée par double-clic (image/emoji, description)
- Ajout de produit avec dialogue dédié

### Commandes & facturation
- Tableau des commandes avec filtres par statut
- Statuts : En cours / Payé
- Génération de facture PDF par commande
- Encaissement rapide

### Plan de salle (Tables)
- Cartes visuelles avec code couleur (vert/rouge/orange)
- Création, modification et suppression de tables
- Lancement de commande depuis une table

### Caisse (Paiements)
- Saisie du montant encaissé
- Calcul automatique de la monnaie à rendre
- Validation du paiement

---

## Prérequis

- **Java 21+** (JDK)
- **Maven 3.9+**
- **Docker** (pour la base PostgreSQL) ou PostgreSQL installé localement
- **JavaFX** (intégré via les dépendances Maven)

---

## Installation et lancement

### 1. Cloner le projet

```bash
git clone <url-du-projet>
cd RestaurantGestion
```

### 2. Démarrer la base de données (Docker)

```bash
docker-compose up -d
```

Cela démarre un conteneur PostgreSQL avec :
- Base : `restaurant_gestion`
- Utilisateur : `admin`
- Mot de passe : `admin123`
- Port : `5432`

### 3. Lancer l'application

```bash
mvn clean javafx:run
```

### 4. (Optionnel) Générer un JAR autonome

```bash
mvn clean package
```

---

## Base de données

### Script d'initialisation

Le fichier `docker/init.sql` crée les tables automatiquement au premier démarrage du conteneur.

### Schéma relationnel

```
tables_restaurant (id, numero_table, capacite, statut)
produits         (id_produit, nom, prix, categorie, description, image_path)
commandes        (id_commande, id_table, date_commande, total, statut)
lignes_commande  (id_ligne, id_commande, id_produit, quantite, prix_unitaire)
paiements        (id_paiement, id_commande, montant, mode_paiement, date_paiement)
factures         (id_facture, id_commande, montant_total, date_creation)
stocks           (id_stock, nom_ingredient, quantite, seuil_alerte, unite)
```

### Connexion

Les paramètres de connexion sont dans `src/main/resources/META-INF/persistence.xml` :
- URL : `jdbc:postgresql://localhost:5432/restaurant_gestion`
- Driver : `org.postgresql.Driver`
- Dialecte : `org.hibernate.dialect.PostgreSQLDialect`

---

## Logique métier

### Calcul du chiffre d'affaires
- Le CA du jour est calculé en filtrant les commandes du jour avec statut "Payé" ou "En attente"
- Le graphique 7 jours affiche le cumul des commandes payées par date

### Statuts des commandes
- **En cours** : commande active, non encore payée
- **Payé** : commande soldée, facture disponible
- Le passage de "En cours" à "Payé" se fait depuis la vue Commandes

### Gestion des tables
- 3 statuts : Libre (vert), Occupée (rouge), Réservée (orange)
- Une table occupée ne peut pas recevoir de nouvelle commande
- Le changement de statut se fait via le dialogue d'édition

### Génération de PDF
- Les factures sont générées via iTextPDF
- Sauvegardées dans le répertoire du projet sous le format `facture_<id>.pdf`
- Contenu : en-tête du restaurant, numéro de commande, tableau des articles, total

---

## Captures d'écran

*(Ajoutez ici des captures d'écran de l'application)*

---

## Licence

Projet personnel — usage libre.
# RestaurantGestion
