-- =============================================================
--  SEED : Données de démonstration cohérentes — L'ÉLIXIR GOURMAND
--  Vérifié : chaque total de commande = somme de ses lignes
--            chaque statut de table correspond à ses commandes
-- =============================================================

-- ─────────────────────────────────────────────────────────────
--  1. TABLES (10)
--     Statut final cohérent avec les commandes ci-dessous
-- ─────────────────────────────────────────────────────────────
INSERT INTO tables_restaurant (numero_table, capacite, statut, localisation) VALUES
    (1,  2, 'Libre',    'Terrasse'),
    (2,  4, 'Occupée',  'Salle principale'),
    (3,  4, 'Libre',    'Salle principale'),
    (4,  6, 'Réservée', 'Salle principale'),
    (5,  2, 'Occupée',  'Terrasse'),
    (6,  4, 'Libre',    'Salle principale'),
    (7,  8, 'Occupée',  'Salon privé'),
    (8,  4, 'Libre',    'Salle principale'),
    (9,  2, 'Libre',    'Bar'),
    (10, 6, 'Réservée', 'Salon privé');

-- ─────────────────────────────────────────────────────────────
--  2. PRODUITS (24)
-- ─────────────────────────────────────────────────────────────
INSERT INTO produits (nom, prix, categorie, disponible, description) VALUES
    -- Entrées
    ('Foie Gras de Canard',      4500, 'Entrée',  TRUE, 'Foie gras maison, chutney de figues, toast brioché'),
    ('Tartare de Saumon',        3500, 'Entrée',  TRUE, 'Saumon sauvage, avocat, citronnelle'),
    ('Soupe à l''oignon',        2500, 'Entrée',  TRUE, 'Traditionnelle, gratiné au comté'),
    ('Carpaccio de Bœuf',        3500, 'Entrée',  TRUE, 'Roquette, parmesan, huile de truffe'),
    ('Velouté de champignons',   2500, 'Entrée',  TRUE, 'Crème fraîche, truffes noires'),
    -- Plats
    ('Entrecôte Frites Maison',  6500, 'Plat',    TRUE, '300g, sauce au poivre, frites artisanales'),
    ('Saumon Sauvage Grillé',    6000, 'Plat',    TRUE, 'Risotto aux asperges, beurre blanc'),
    ('Poulet Fermier Rôti',      5000, 'Plat',    TRUE, 'Légumes de saison, jus de volaille'),
    ('Magret de Canard',         5500, 'Plat',    TRUE, 'Pommes sarladaises, sauce aux cerises'),
    ('Burger Maison',            4500, 'Plat',    TRUE, 'Bœuf charolais, cheddar affiné, frites'),
    ('Risotto aux Truffes',      7500, 'Plat',    TRUE, 'Truffe noire du Périgord, parmesan 24 mois'),
    ('Blanquette de Veau',       5000, 'Plat',    TRUE, 'Recette traditionnelle, riz pilaf'),
    -- Desserts
    ('Crème Brûlée Maison',      2500, 'Dessert', TRUE, 'Vanille Bourbon de Madagascar'),
    ('Café Gourmand',            2500, 'Dessert', TRUE, 'Expresso + 3 mignardises'),
    ('Fondant au Chocolat',      3000, 'Dessert', TRUE, 'Cœur coulant, glace vanille'),
    ('Île Flottante',            2000, 'Dessert', TRUE, 'Caramel, pralines roses'),
    ('Assiette de Fromages',     3000, 'Dessert', TRUE, 'Sélection affinée, miel, noix'),
    -- Boissons
    ('Eau Minérale 50cl',        1000, 'Boisson', TRUE, 'Evian ou Perrier'),
    ('Jus de Fruits Frais',      2000, 'Boisson', TRUE, 'Orange, pomme ou ananas'),
    ('Coca-Cola 33cl',           1000, 'Boisson', TRUE, NULL),
    ('Café Expresso',            1000, 'Boisson', TRUE, 'Café de spécialité'),
    ('Thé / Infusion',           1000, 'Boisson', TRUE, 'Sélection de 8 parfums'),
    ('Vin Rouge - Bordeaux 75cl',15000,'Boisson', TRUE, 'Château Margaux 2019'),
    ('Vin Blanc - Chablis 75cl', 12000,'Boisson', TRUE, 'Premier Cru 2021');

-- ─────────────────────────────────────────────────────────────
--  3. STOCKS (12 ingrédients)
--     3 alertes : Comté (3/5), Foie gras (0.8/2), Truffe (0.3/0.5)
-- ─────────────────────────────────────────────────────────────
INSERT INTO stocks (nom_ingredient, quantite, unite, seuil_alerte) VALUES
    ('Farine de blé',         50.0, 'kg',    10.0),
    ('Tomates fraîches',      20.0, 'kg',     5.0),
    ('Fromage (comté)',        3.0, 'kg',     5.0),
    ('Viande hachée',         25.0, 'kg',     8.0),
    ('Saumon frais',          12.0, 'kg',     5.0),
    ('Café en grains',        15.0, 'kg',     3.0),
    ('Crème fraîche',          6.0, 'L',      3.0),
    ('Foie gras',              0.8, 'kg',     2.0),
    ('Truffe noire',           0.3, 'kg',     0.5),
    ('Beurre',                 10.0, 'kg',     4.0),
    ('Œufs',                   60.0, 'unité', 20.0),
    ('Huile d''olive',          8.0, 'L',      2.0);

-- ─────────────────────────────────────────────────────────────
--  4. COMPOSITION DES PLATS (produit → ingrédient + quantité)
-- ─────────────────────────────────────────────────────────────
INSERT INTO produit_ingredients (id_produit, id_stock, quantite) VALUES
    -- Foie Gras de Canard (1)
    (1, 8,  0.12), (1, 11, 1),  (1, 1,  0.04),
    -- Tartare de Saumon (2)
    (2, 5,  0.15), (2, 12, 0.02),
    -- Entrecôte Frites Maison (6)
    (6, 4,  0.3),  (6, 1,  0.2),  (6, 11, 1),   (6, 10, 0.05),
    -- Saumon Sauvage Grillé (7)
    (7, 5,  0.2),  (7, 12, 0.02), (7, 7,  0.05),
    -- Burger Maison (10)
    (10, 4, 0.2),  (10, 1, 0.1),  (10, 3, 0.03), (10, 11, 1), (10, 10, 0.03),
    -- Risotto aux Truffes (11)
    (11, 9, 0.02), (11, 7, 0.1),  (11, 10, 0.03), (11, 1, 0.15),
    -- Crème Brûlée Maison (13)
    (13, 7, 0.1),  (13, 11, 2),   (13, 1, 0.03),
    -- Café Gourmand (14)
    (14, 6, 0.02), (14, 7, 0.03), (14, 1, 0.02),  (14, 10, 0.02), (14, 11, 1);

-- ─────────────────────────────────────────────────────────────
--  5. COMMANDES (6)
--     Chaque total = somme(quantite × prix_unitaire) de ses lignes
-- ─────────────────────────────────────────────────────────────

-- C1 — Table 2 (Occupée) — En cours — 4 lignes
-- 2× Saumon Grillé(6000) + 1× Crème Brûlée(2500) + 2× Café Gourmand(2500) + 1× Vin Blanc(12000)
-- = 12000 + 2500 + 5000 + 12000 = 31500
INSERT INTO commandes (id_table, date_commande, statut, total, serveur) VALUES
    (2, NOW() - INTERVAL '45 minutes', 'En cours', 31500, 'Marie');
INSERT INTO lignes_commande (id_commande, id_produit, quantite, prix_unitaire) VALUES
    (1, 7,  2, 6000),
    (1, 13, 1, 2500),
    (1, 14, 2, 2500),
    (1, 24, 1, 12000);

-- C2 — Table 5 (Occupée) — En cours — 4 lignes
-- 1× Foie Gras(4500) + 1× Magret Canard(5500) + 1× Fondant Chocolat(3000) + 1× Café Expresso(1000)
-- = 4500 + 5500 + 3000 + 1000 = 14000
INSERT INTO commandes (id_table, date_commande, statut, total, serveur) VALUES
    (5, NOW() - INTERVAL '30 minutes', 'En cours', 14000, 'Thomas');
INSERT INTO lignes_commande (id_commande, id_produit, quantite, prix_unitaire) VALUES
    (2, 1,  1, 4500),
    (2, 9,  1, 5500),
    (2, 15, 1, 3000),
    (2, 21, 1, 1000);

-- C3 — Table 7 (Occupée) — En cours — 5 lignes
-- 3× Entrecôte(6500) + 2× Risotto Truffes(7500) + 6× Eau(1000) + 2× Vin Rouge(15000) + 3× Café Gourmand(2500)
-- = 19500 + 15000 + 6000 + 30000 + 7500 = 78000
INSERT INTO commandes (id_table, date_commande, statut, total, serveur) VALUES
    (7, NOW() - INTERVAL '2 hours', 'En cours', 78000, 'Lucas');
INSERT INTO lignes_commande (id_commande, id_produit, quantite, prix_unitaire) VALUES
    (3, 6,  3, 6500),
    (3, 11, 2, 7500),
    (3, 18, 6, 1000),
    (3, 23, 2, 15000),
    (3, 14, 3, 2500);

-- C4 — Table 1 (Libre, payée) — Payée — 4 lignes
-- 1× Tartare Saumon(3500) + 1× Poulet Fermier(5000) + 1× Crème Brûlée(2500) + 1× Jus Fruits(2000)
-- = 3500 + 5000 + 2500 + 2000 = 13000
INSERT INTO commandes (id_table, date_commande, statut, total, serveur) VALUES
    (1, NOW() - INTERVAL '3 hours', 'Payée', 13000, 'Marie');
INSERT INTO lignes_commande (id_commande, id_produit, quantite, prix_unitaire) VALUES
    (4, 2,  1, 3500),
    (4, 8,  1, 5000),
    (4, 13, 1, 2500),
    (4, 19, 1, 2000);

-- C5 — Table 6 (Libre, payée) — Payée — 4 lignes
-- 1× Velouté champignons(2500) + 1× Burger Maison(4500) + 1× Île Flottante(2000) + 1× Eau(1000)
-- = 2500 + 4500 + 2000 + 1000 = 10000
INSERT INTO commandes (id_table, date_commande, statut, total, serveur) VALUES
    (6, NOW() - INTERVAL '4 hours', 'Payée', 10000, 'Thomas');
INSERT INTO lignes_commande (id_commande, id_produit, quantite, prix_unitaire) VALUES
    (5, 5,  1, 2500),
    (5, 10, 1, 4500),
    (5, 16, 1, 2000),
    (5, 18, 1, 1000);

-- C6 — Table 3 (Libre, payée) — Payée — 5 lignes
-- 1× Foie Gras(4500) + 1× Entrecôte(6500) + 1× Fondant Chocolat(3000) + 1× Vin Rouge(15000) + 1× Café Gourmand(2500)
-- = 4500 + 6500 + 3000 + 15000 + 2500 = 31500
INSERT INTO commandes (id_table, date_commande, statut, total, serveur) VALUES
    (3, NOW() - INTERVAL '5 hours', 'Payée', 31500, 'Lucas');
INSERT INTO lignes_commande (id_commande, id_produit, quantite, prix_unitaire) VALUES
    (6, 1,  1, 4500),
    (6, 6,  1, 6500),
    (6, 15, 1, 3000),
    (6, 23, 1, 15000),
    (6, 14, 1, 2500);

-- ─────────────────────────────────────────────────────────────
--  6. PAIEMENTS + FACTURES (pour les 3 commandes Payée)
-- ─────────────────────────────────────────────────────────────

-- Paiement C4 (Table 1) — 13000 FCFA en CB
INSERT INTO paiements (id_commande, mode_paiement, montant_recu, date_paiement) VALUES
    (4, 'Carte Bancaire', 15000, NOW() - INTERVAL '2 hours 50 minutes');
INSERT INTO factures (id_commande, total, mode_paiement, date_generation) VALUES
    (4, 13000, 'Carte Bancaire', NOW() - INTERVAL '2 hours 50 minutes');

-- Paiement C5 (Table 6) — 10000 FCFA en espèces
INSERT INTO paiements (id_commande, mode_paiement, montant_recu, date_paiement) VALUES
    (5, 'Espèces', 10000, NOW() - INTERVAL '3 hours 50 minutes');
INSERT INTO factures (id_commande, total, mode_paiement, date_generation) VALUES
    (5, 10000, 'Espèces', NOW() - INTERVAL '3 hours 50 minutes');

-- Paiement C6 (Table 3) — 31500 FCFA en Mobile Money
INSERT INTO paiements (id_commande, mode_paiement, montant_recu, date_paiement) VALUES
    (6, 'Mobile Money', 35000, NOW() - INTERVAL '4 hours 50 minutes');
INSERT INTO factures (id_commande, total, mode_paiement, date_generation) VALUES
    (6, 31500, 'Mobile Money', NOW() - INTERVAL '4 hours 50 minutes');

-- ─────────────────────────────────────────────────────────────
--  7. VUES ANALYTIQUES
-- ─────────────────────────────────────────────────────────────
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

SELECT '✅ Seed terminé : ' ||
       (SELECT COUNT(*)::text FROM tables_restaurant) || ' tables, ' ||
       (SELECT COUNT(*)::text FROM produits) || ' produits, ' ||
       (SELECT COUNT(*)::text FROM commandes) || ' commandes, ' ||
       (SELECT COUNT(*)::text FROM paiements) || ' paiements'
AS message;
