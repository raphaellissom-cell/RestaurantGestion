# Cahier de charges de conception frontend — Application de gestion de restaurant

## 1. Objectif du frontend

Le frontend constitue la couche de présentation du système. Il devra permettre aux utilisateurs (serveurs, caissiers, administrateurs) d’interagir avec l’application de manière intuitive, rapide et ergonomique.

L’interface sera développée avec JavaFX afin d’obtenir une expérience moderne, fluide et visuellement attractive.

Les objectifs principaux sont :

* offrir une interface premium et professionnelle ;
* minimiser le nombre de clics pour les opérations fréquentes ;
* assurer une navigation claire entre les modules ;
* afficher les données de manière structurée et élégante ;
* garantir une expérience utilisateur fluide.

---

## 2. Contraintes techniques

Le frontend devra respecter les contraintes suivantes :

* Langage : Java
* Framework UI : JavaFX
* Architecture : MVC
* Communication interne :

  * Controllers → Services
  * Services → Repositories
  * Repositories → PostgreSQL via ORM

Le frontend ne dialoguera jamais directement avec la base de données.

Toutes les opérations passeront par la couche backend.

---

## 3. Philosophie de design

L’interface suivra les principes suivants :

### Simplicité

Les écrans devront être compréhensibles immédiatement.

### Lisibilité

Les informations critiques (prix, total, alertes stock) devront être mises en évidence.

### Rapidité

Les actions fréquentes devront être accessibles en 1 à 3 clics maximum.

### Élégance

Le style visuel devra donner une impression haut de gamme.

L’inspiration visuelle pourra s’approcher de :

* dashboards SaaS modernes ;
* logiciels de caisse premium ;
* interfaces fintech.

---

## 4. Charte graphique

### Style général

Le style retenu sera :

* moderne
* minimaliste
* premium
* sombre ou semi-sombre

Exemple de palette :

Couleurs principales :

* fond principal : noir profond / gris anthracite
* cartes : gris foncé
* texte : blanc cassé
* accent principal : doré / bleu premium / vert émeraude

Couleurs sémantiques :

* succès : vert
* avertissement : orange
* erreur : rouge

---

## 5. Typographie

Le frontend devra utiliser une typographie claire et élégante.

Critères :

* lisibilité élevée ;
* hiérarchie visuelle ;
* contraste fort.

Tailles recommandées :

* titre principal : 28–36 px
* sous-titres : 18–24 px
* texte normal : 14–16 px
* indicateurs statistiques : 24–32 px

---

## 6. Architecture des vues

L’application utilisera une architecture de vues imbriquées.

Structure :

MainView
├── HeaderView
├── SidebarView
└── ContentView
├── DashboardView
├── MenuView
├── TablesView
├── CommandesView
├── PaiementsView
├── StocksView
└── VentesView

Le changement de module remplacera uniquement ContentView.

La scène principale restera unique.

---

## 7. Layout principal

Le layout principal sera organisé en 3 zones.

### Header

Contient :

* nom du restaurant
* date / heure
* utilisateur connecté
* notifications

### Sidebar

Contient les accès aux modules :

* Dashboard
* Menus
* Commandes
* Tables
* Paiements
* Stocks
* Ventes

### Zone centrale

Affiche le contenu du module actif.

---

## 8. Dashboard

Le dashboard est la page d’accueil.

Il devra présenter une vue synthétique du restaurant.

Indicateurs :

* nombre de tables libres
* nombre de tables occupées
* commandes en cours
* chiffre d’affaires journalier
* alertes de stock

Widgets recommandés :

* KPI cards
* mini graphes
* notifications

Objectif :
permettre un aperçu global en moins de 5 secondes.

---

## 9. Module Menus

Fonctionnalités :

* afficher tous les plats
* ajouter un plat
* modifier un plat
* supprimer un plat
* activer/désactiver disponibilité

Affichage recommandé :

* table moderne
  ou
* cartes visuelles des plats

Chaque plat affichera :

* nom
* catégorie
* prix
* disponibilité

Actions :

* éditer
* supprimer
* désactiver

---

## 10. Module Tables

Ce module devra être très visuel.

Chaque table sera représentée par une carte interactive.

Informations affichées :

* numéro table
* capacité
* statut

Statuts :

* libre
* occupée
* réservée

Interactions :

* sélectionner
* assigner commande
* libérer table

Le statut devra être identifiable instantanément.

---

## 11. Module Commandes

Ce module est le cœur de l’application.

Le layout sera divisé en 2 parties.

### Partie gauche

Catalogue des plats :

* recherche
* filtre catégorie
* ajout rapide

### Partie droite

Commande active :

* liste des plats
* quantités
* sous-total
* total final

Fonctionnalités :

* ajouter plat
* retirer plat
* modifier quantité
* valider commande

Contraintes UX :
Le temps de prise de commande doit être minimal.

---

## 12. Module Paiement

Le module paiement gère l’encaissement.

Informations affichées :

* commande
* total
* mode de paiement

Modes :

* espèces
* carte
* mobile money

Fonctionnalités :

* saisie montant reçu
* calcul monnaie
* validation paiement
* génération facture

L’interface devra rassurer l’utilisateur en limitant les erreurs.

---

## 13. Module Stocks

Affichage :
tableau enrichi avec alertes.

Colonnes :

* ingrédient
* quantité
* seuil critique
* état

États :

* normal
* faible
* critique

Fonctionnalités :

* réapprovisionnement
* modification seuil
* alertes automatiques

---

## 14. Module Ventes

Ce module affichera les statistiques.

Visualisations :

* histogrammes
* camemberts
* courbes

Statistiques :

* ventes journalières
* ventes mensuelles
* plats les plus vendus
* revenus

Objectif :
fournir des informations décisionnelles.

---

## 15. Composants UI JavaFX recommandés

Composants principaux :

Containers :

* BorderPane
* VBox
* HBox
* StackPane
* GridPane

Contrôles :

* Button
* TableView
* ListView
* TextField
* ComboBox
* DatePicker
* Label

Visualisation :

* BarChart
* PieChart
* LineChart

Navigation :

* custom sidebar
* transitions animées

---

## 16. Animations et micro-interactions

Pour un rendu premium, intégrer :

* transitions de pages
* hover animations
* effets d’ombre
* boutons animés
* cartes interactives

Exemples :

* fade in
* slide transition
* scale on hover

Les animations doivent rester discrètes.

Objectif :
renforcer la perception de qualité.

---

## 17. Responsabilités MVC frontend

### View

Responsable de :

* rendu UI
* composants graphiques

### Controller

Responsable de :

* événements utilisateur
* validation de saisies
* communication avec services

### Model

Responsable de :

* représentation des entités métier

---

## 18. Performances

Le frontend devra :

* charger rapidement ;
* rester fluide ;
* éviter les blocages UI ;
* exécuter tâches lourdes en arrière-plan.

Les appels backend devront être asynchrones si nécessaire.

---

## 19. Extensibilité

L’architecture devra permettre l’ajout futur de :

* authentification multi-rôles
* notifications temps réel
* réservations
* commandes en ligne
* analytics avancés

---

## 20. Conclusion

Le frontend sera conçu comme une interface premium de gestion de restaurant, combinant esthétique moderne, ergonomie et performances. L’utilisation de JavaFX avec une architecture MVC cohérente avec le backend permettra d’obtenir une application robuste, élégante et évolutive.
