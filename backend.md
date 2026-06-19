# Cahier de charges de conception backend — Application de gestion de restaurant

## 1. Contexte du projet

Le projet consiste à développer une application de gestion de restaurant permettant d’automatiser les opérations principales d’un établissement de restauration. L’application devra faciliter la gestion des menus, des commandes clients, des tables, des paiements, des stocks, ainsi que la génération de factures et le suivi des ventes.

Le backend constitue la couche centrale du système. Il sera responsable de la logique métier, de la communication avec la base de données, de la validation des données et du traitement des opérations internes.

---

## 2. Objectifs du backend

Le backend devra permettre de :

* gérer les données métier du restaurant ;
* assurer la persistance des données dans une base PostgreSQL ;
* implémenter les règles métier ;
* centraliser la logique de calcul (totaux, paiements, statistiques) ;
* garantir la cohérence entre commandes, stocks et ventes ;
* fournir une architecture maintenable et extensible.

---

## 3. Technologies retenues

Le backend sera développé avec les technologies suivantes :

* **Langage** : Java 21+
* **Architecture** : MVC (Model-View-Controller) avec couche Service
* **Base de données** : PostgreSQL
* **ORM** : Hibernate / Jakarta Persistence
* **Gestion des dépendances** : Maven
* **Réduction du boilerplate** : Project Lombok
* **Conteneurisation** : Docker
* **Tests** : JUnit

---

## 4. Architecture logicielle

Le backend suivra une architecture multicouche.

### 4.1 Couche View

La couche View correspond à l’interface graphique développée avec **JavaFX** ou **Java Swing**.

Responsabilités :

* afficher les données ;
* recevoir les interactions utilisateur ;
* transmettre les actions au contrôleur.

---

### 4.2 Couche Controller

Responsabilités :

* recevoir les événements de l’interface ;
* valider les entrées utilisateur ;
* invoquer les services métier ;
* transmettre les résultats à la vue.

Exemples :

* création de commande ;
* validation de paiement ;
* ajout d’un menu.

---

### 4.3 Couche Service

Responsabilités :

* implémenter la logique métier ;
* appliquer les règles de gestion ;
* coordonner plusieurs repositories.

Exemples :

* calcul du montant total d’une commande ;
* vérification de disponibilité des ingrédients ;
* génération des statistiques de ventes.

---

### 4.4 Couche Repository / DAO

Responsabilités :

* interagir avec PostgreSQL ;
* exécuter les opérations CRUD ;
* encapsuler les requêtes SQL.

Opérations :

* INSERT
* SELECT
* UPDATE
* DELETE

---

### 4.5 Couche Model

Contient les entités métier représentant les objets du domaine.

Exemples :

* MenuItem
* Commande
* LigneCommande
* TableRestaurant
* Paiement
* Facture
* Stock
* Vente

---

## 5. Modules fonctionnels backend

## 5.1 Gestion des menus

Le système devra permettre :

* l’ajout d’un plat ;
* la modification d’un plat ;
* la suppression d’un plat ;
* l’affichage des plats disponibles.

Attributs d’un plat :

* identifiant ;
* nom ;
* prix ;
* catégorie ;
* disponibilité.

---

## 5.2 Gestion des tables

Le backend devra gérer l’état des tables.

États possibles :

* libre ;
* occupée ;
* réservée.

Fonctionnalités :

* assigner une table ;
* changer son état ;
* consulter disponibilité.

---

## 5.3 Gestion des commandes

Le système devra permettre :

* création de commande ;
* ajout/suppression de plats ;
* calcul automatique du total ;
* association à une table.

Une commande contient :

* identifiant ;
* date ;
* table ;
* liste des plats ;
* montant total.

Règles métier :

* une commande doit contenir au moins un plat ;
* une table occupée ne peut pas être réassignée.

---

## 5.4 Gestion des paiements

Le système devra gérer les paiements clients.

Modes supportés :

* espèces ;
* carte bancaire ;
* mobile money.

Fonctionnalités :

* enregistrer paiement ;
* vérifier montant payé ;
* confirmer transaction.

Règles :

* paiement impossible si commande inexistante ;
* montant payé ≥ total facture.

---

## 5.5 Gestion des stocks

Le backend devra assurer le suivi des stocks d’ingrédients.

Exemples :

* farine ;
* tomate ;
* fromage ;
* viande.

Fonctionnalités :

* consulter stock ;
* ajouter stock ;
* retirer stock ;
* alerte stock faible.

Règles métier :

* stock insuffisant → commande refusée ;
* validation commande → décrémentation automatique.

---

## 5.6 Facturation

Après paiement, le système génère une facture.

Une facture contient :

* numéro facture ;
* date ;
* commande associée ;
* montant total ;
* mode de paiement.

Fonctionnalités :

* génération automatique ;
* archivage ;
* impression/export PDF.

---

## 5.7 Suivi des ventes

Le backend devra calculer :

* chiffre d’affaires journalier ;
* chiffre d’affaires mensuel ;
* plats les plus vendus ;
* nombre total de commandes.

Ces statistiques permettront l’analyse des performances du restaurant.

---

## 6. Modèle de données relationnel

Tables principales :

### MENU_ITEMS

* id
* nom
* prix
* categorie
* disponible

### TABLES_RESTAURANT

* id
* numero
* statut
* capacite

### COMMANDES

* id
* table_id
* date_creation
* total

### LIGNES_COMMANDE

* id
* commande_id
* menu_item_id
* quantite
* sous_total

### PAIEMENTS

* id
* commande_id
* mode
* montant
* date_paiement

### FACTURES

* id
* commande_id
* numero
* total
* date_generation

### STOCKS

* id
* ingredient
* quantite
* seuil_alerte

---

## 7. Contraintes non fonctionnelles

Le backend devra respecter les contraintes suivantes :

### Performance

* temps de réponse faible ;
* accès rapide aux données.

### Maintenabilité

* code modulaire ;
* respect des principes SOLID ;
* documentation claire.

### Scalabilité

L’architecture doit permettre l’ajout futur de :

* réservations en ligne ;
* authentification ;
* API REST ;
* dashboard administrateur.

### Fiabilité

* transactions sécurisées ;
* gestion des exceptions ;
* cohérence des données.

---

## 8. Conteneurisation

Le système sera conteneurisé via Docker avec :

### Container 1

Application Java backend

### Container 2

Base PostgreSQL

Avantages :

* environnement reproductible ;
* déploiement simplifié ;
* portabilité.

---

## 9. Tests backend

Des tests devront être réalisés pour :

### Tests unitaires

Validation des services métier :

* calcul total ;
* gestion stock ;
* paiements.

### Tests d’intégration

Validation :

* ORM ↔ PostgreSQL ;
* transactions complètes.

---

## 10. Livrables attendus

Le projet devra fournir :

* code source backend ;
* base PostgreSQL ;
* fichiers Docker ;
* documentation technique ;
* diagrammes UML ;
* rapport final.

---

## 11. Conclusion

Le backend de cette application constituera le noyau fonctionnel du système de gestion de restaurant. Son architecture modulaire en Java avec PostgreSQL garantira robustesse, maintenabilité et extensibilité, tout en assurant une gestion efficace des opérations du restaurant.
