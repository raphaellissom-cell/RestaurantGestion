-- =============================================================
--  INITIALISATION DE LA BASE DE DONNÉES — L'Élixir Gourmand
--  Schéma complet : tables, contraintes, index, données initiales
-- =============================================================

-- Extension pour les UUIDs si besoin futur
-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ─────────────────────────────────────────────────────────────
--  1. TABLES DU RESTAURANT
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS tables_restaurant (
    id              SERIAL PRIMARY KEY,
    numero_table    INTEGER NOT NULL UNIQUE,
    capacite        INTEGER NOT NULL DEFAULT 4,
    statut          VARCHAR(20) NOT NULL DEFAULT 'Libre'
                    CHECK (statut IN ('Libre', 'Occupée', 'Réservée')),
    localisation    VARCHAR(50) DEFAULT 'Salle principale'
);

-- ─────────────────────────────────────────────────────────────
--  2. MENU (plats, boissons, desserts)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS produits (
    id_produit      SERIAL PRIMARY KEY,
    nom             VARCHAR(100) NOT NULL,
    prix            NUMERIC(10, 2) NOT NULL CHECK (prix >= 0),
    categorie       VARCHAR(50) NOT NULL DEFAULT 'Plat',
    disponible      BOOLEAN NOT NULL DEFAULT TRUE,
    description     TEXT,
    created_at      TIMESTAMP DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
--  3. COMMANDES
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS commandes (
    id_commande     SERIAL PRIMARY KEY,
    id_table        INTEGER REFERENCES tables_restaurant(id) ON DELETE SET NULL,
    date_commande   TIMESTAMP NOT NULL DEFAULT NOW(),
    statut          VARCHAR(20) NOT NULL DEFAULT 'En cours'
                    CHECK (statut IN ('En cours', 'En attente', 'Payée', 'Annulée')),
    total           NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    notes           TEXT,
    serveur         VARCHAR(100) DEFAULT 'Non assigné'
);

-- ─────────────────────────────────────────────────────────────
--  4. LIGNES DE COMMANDE
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS lignes_commande (
    id_ligne        SERIAL PRIMARY KEY,
    id_commande     INTEGER NOT NULL REFERENCES commandes(id_commande) ON DELETE CASCADE,
    id_produit      INTEGER REFERENCES produits(id_produit) ON DELETE SET NULL,
    quantite        INTEGER NOT NULL DEFAULT 1 CHECK (quantite > 0),
    prix_unitaire   NUMERIC(10, 2) NOT NULL,
    sous_total      NUMERIC(10, 2) GENERATED ALWAYS AS (quantite * prix_unitaire) STORED,
    notes           TEXT
);

-- ─────────────────────────────────────────────────────────────
--  5. PAIEMENTS
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS paiements (
    id_paiement     SERIAL PRIMARY KEY,
    id_commande     INTEGER NOT NULL REFERENCES commandes(id_commande) ON DELETE CASCADE,
    mode_paiement   VARCHAR(30) NOT NULL DEFAULT 'Espèces'
                    CHECK (mode_paiement IN ('Espèces', 'Carte Bancaire', 'Mobile Money')),
    montant_recu    NUMERIC(10, 2) NOT NULL,
    -- la monnaie à rendre sera calculée côté application
    date_paiement   TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
--  6. FACTURES
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS factures (
    id_facture      SERIAL PRIMARY KEY,
    id_commande     INTEGER NOT NULL REFERENCES commandes(id_commande) ON DELETE CASCADE,
    numero_facture  VARCHAR(30) NOT NULL UNIQUE DEFAULT ('FAC-' || TO_CHAR(NOW(), 'YYYYMMDD') || '-' || NEXTVAL('factures_id_facture_seq')),
    total           NUMERIC(10, 2) NOT NULL,
    mode_paiement   VARCHAR(30),
    date_generation TIMESTAMP NOT NULL DEFAULT NOW(),
    chemin_pdf      VARCHAR(255)
);

-- ─────────────────────────────────────────────────────────────
--  7. STOCKS (ingrédients)
-- ─────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS stocks (
    id_stock        SERIAL PRIMARY KEY,
    nom_ingredient  VARCHAR(100) NOT NULL UNIQUE,
    quantite        NUMERIC(10, 2) NOT NULL DEFAULT 0 CHECK (quantite >= 0),
    unite           VARCHAR(20) DEFAULT 'kg',
    seuil_alerte    NUMERIC(10, 2) NOT NULL DEFAULT 5,
    last_updated    TIMESTAMP DEFAULT NOW()
);

-- ─────────────────────────────────────────────────────────────
--  INDEX pour les performances
-- ─────────────────────────────────────────────────────────────
CREATE INDEX IF NOT EXISTS idx_commandes_statut       ON commandes(statut);
CREATE INDEX IF NOT EXISTS idx_commandes_date         ON commandes(date_commande);
CREATE INDEX IF NOT EXISTS idx_lignes_commande_cmd    ON lignes_commande(id_commande);
CREATE INDEX IF NOT EXISTS idx_produits_categorie     ON produits(categorie);
CREATE INDEX IF NOT EXISTS idx_stocks_seuil           ON stocks(quantite, seuil_alerte);

-- =============================================================
--  DONNÉES INITIALES DE DÉMONSTRATION
-- =============================================================

-- ─────────────────────────────────────────────────────────────
--  Tables du restaurant (10 tables)
-- ─────────────────────────────────────────────────────────────
INSERT INTO tables_restaurant (numero_table, capacite, statut, localisation) VALUES
    (1,  2, 'Libre',    'Terrasse'),
    (2,  4, 'Occupée',  'Salle principale'),
    (3,  4, 'Libre',    'Salle principale'),
    (4,  6, 'Réservée', 'Salle principale'),
    (5,  2, 'Libre',    'Terrasse'),
    (6,  4, 'Occupée',  'Salle principale'),
    (7,  8, 'Libre',    'Salon privé'),
    (8,  4, 'Libre',    'Salle principale'),
    (9,  2, 'Libre',    'Bar'),
    (10, 6, 'Réservée', 'Salon privé')
ON CONFLICT (numero_table) DO NOTHING;

-- ─────────────────────────────────────────────────────────────
--  Carte du restaurant (produits)
-- ─────────────────────────────────────────────────────────────
INSERT INTO produits (nom, prix, categorie, disponible, description) VALUES
    -- Entrées
    ('Foie Gras de Canard',      18.50, 'Entrée',  TRUE, 'Foie gras maison, chutney de figues, toast brioché'),
    ('Tartare de Saumon',        14.00, 'Entrée',  TRUE, 'Saumon sauvage, avocat, citronnelle'),
    ('Soupe à l''oignon',         9.50, 'Entrée',  TRUE, 'Traditionnelle, gratiné au comté'),
    ('Carpaccio de Bœuf',        13.50, 'Entrée',  TRUE, 'Roquette, parmesan, huile de truffe'),
    ('Velouté de champignons',    8.50, 'Entrée',  TRUE, 'Crème fraîche, truffes noires'),

    -- Plats
    ('Entrecôte Frites Maison',  26.00, 'Plat',    TRUE, '300g, sauce au poivre, frites artisanales'),
    ('Saumon Sauvage Grillé',    24.00, 'Plat',    TRUE, 'Risotto aux asperges, beurre blanc'),
    ('Poulet Fermier Rôti',      19.50, 'Plat',    TRUE, 'Légumes de saison, jus de volaille'),
    ('Magret de Canard',         23.00, 'Plat',    TRUE, 'Pommes sarladaises, sauce aux cerises'),
    ('Burger Maison',            18.00, 'Plat',    TRUE, 'Bœuf charolais, cheddar affiné, frites'),
    ('Risotto aux Truffes',      28.00, 'Plat',    TRUE, 'Truffe noire du Périgord, parmesan 24 mois'),
    ('Blanquette de Veau',       21.00, 'Plat',    TRUE, 'Recette traditionnelle, riz pilaf'),

    -- Desserts
    ('Crème Brûlée Maison',       8.50, 'Dessert', TRUE, 'Vanille Bourbon de Madagascar'),
    ('Café Gourmand',             9.00, 'Dessert', TRUE, 'Expresso + 3 mignardises'),
    ('Fondant au Chocolat',       9.50, 'Dessert', TRUE, 'Cœur coulant, glace vanille'),
    ('Île Flottante',             7.50, 'Dessert', TRUE, 'Caramel, pralines roses'),
    ('Assiette de Fromages',     11.00, 'Dessert', TRUE, 'Sélection affinée, miel, noix'),

    -- Boissons
    ('Eau Minérale 50cl',         3.00, 'Boisson', TRUE, 'Evian ou Perrier'),
    ('Jus de Fruits Frais',       5.50, 'Boisson', TRUE, 'Orange, pomme ou ananas'),
    ('Coca-Cola 33cl',            3.50, 'Boisson', TRUE, NULL),
    ('Café Expresso',             2.80, 'Boisson', TRUE, 'Café de spécialité'),
    ('Thé / Infusion',            3.20, 'Boisson', TRUE, 'Sélection de 8 parfums'),
    ('Vin Rouge - Bordeaux 75cl', 28.00,'Boisson', TRUE, 'Château Margaux 2019'),
    ('Vin Blanc - Chablis 75cl',  24.00,'Boisson', TRUE, 'Premier Cru 2021')
ON CONFLICT DO NOTHING;

-- ─────────────────────────────────────────────────────────────
--  Stocks d'ingrédients
-- ─────────────────────────────────────────────────────────────
INSERT INTO stocks (nom_ingredient, quantite, unite, seuil_alerte) VALUES
    ('Farine de blé',         50.0,  'kg',    10.0),
    ('Tomates fraîches',       8.0,  'kg',    15.0),
    ('Fromage (comté)',         3.0,  'kg',     5.0),
    ('Viande hachée',          25.0, 'kg',     8.0),
    ('Saumon frais',           12.0, 'kg',     5.0),
    ('Café en grains',         15.0, 'kg',     3.0),
    ('Crème fraîche',           6.0, 'L',      3.0),
    ('Foie gras',               2.5, 'kg',     2.0),
    ('Truffe noire',            0.3, 'kg',     0.2),
    ('Beurre',                 10.0, 'kg',     4.0),
    ('Œufs',                   60.0, 'unité', 20.0),
    ('Huile d''olive',          8.0, 'L',      2.0)
ON CONFLICT (nom_ingredient) DO NOTHING;

-- ─────────────────────────────────────────────────────────────
--  Commandes de démonstration
-- ─────────────────────────────────────────────────────────────
INSERT INTO commandes (id_table, date_commande, statut, total, serveur) VALUES
    (2, NOW() - INTERVAL '45 minutes', 'En cours',   67.50, 'Marie - 101'),
    (6, NOW() - INTERVAL '1 hour',     'En cours',   89.00, 'Thomas - 102'),
    (1, NOW() - INTERVAL '2 hours',    'Payée',      45.30, 'Marie - 101'),
    (3, NOW() - INTERVAL '3 hours',    'Payée',     112.00, 'Lucas - 103'),
    (5, NOW() - INTERVAL '1 day',      'Payée',      38.50, 'Thomas - 102'),
    (4, NOW() - INTERVAL '1 day',      'Annulée',    0.00,  'Lucas - 103');

-- Lignes des commandes actives
INSERT INTO lignes_commande (id_commande, id_produit, quantite, prix_unitaire, notes) VALUES
    -- Commande 1 (table 2, en cours)
    (1, 1,  1, 18.50, NULL),           -- Foie Gras
    (1, 7,  2, 24.00, 'Sans sauce'),   -- 2x Saumon
    (1, 13, 2,  8.50, NULL),           -- 2x Crème Brûlée
    -- Commande 2 (table 6, en cours)
    (2, 6,  2, 26.00, 'Bien cuit'),    -- 2x Entrecôte
    (2, 14, 2,  9.00, NULL),           -- 2x Café Gourmand
    (2, 23, 1, 28.00, NULL);           -- 1x Vin rouge

-- =============================================================
--  VUE UTILE : Tableau de bord CA du jour
-- =============================================================
CREATE OR REPLACE VIEW vue_ca_journalier AS
SELECT
    DATE(date_commande)         AS jour,
    COUNT(id_commande)          AS nb_commandes,
    SUM(total)                  AS chiffre_affaires,
    AVG(total)                  AS panier_moyen
FROM commandes
WHERE statut = 'Payée'
GROUP BY DATE(date_commande)
ORDER BY jour DESC;

-- Vue : produits les plus commandés
CREATE OR REPLACE VIEW vue_top_produits AS
SELECT
    p.nom,
    p.categorie,
    SUM(lc.quantite)            AS total_commande,
    SUM(lc.sous_total)          AS chiffre_affaires
FROM lignes_commande lc
JOIN produits p ON lc.id_produit = p.id_produit
JOIN commandes c ON lc.id_commande = c.id_commande
WHERE c.statut = 'Payée'
GROUP BY p.id_produit, p.nom, p.categorie
ORDER BY total_commande DESC;

-- =============================================================
--  FINALISATION
-- =============================================================
SELECT 'Base de données gestion_restaurant initialisée avec succès !' AS message;
