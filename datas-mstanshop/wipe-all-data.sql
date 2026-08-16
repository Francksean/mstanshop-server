-- Wipe complet : supprime TOUS les produits, catégories, commandes, paniers, reviews, images R2 enregistrées.
-- Préserve : utilisateurs (users), rôles (roles), authentification, configuration.
-- Usage : psql $DATABASE_URL -f datas-mstanshop/wipe-all-data.sql
-- ⚠️  DESTRUCTEUR — vérifier avant d'exécuter en production.

BEGIN;

-- Supprime les messages WhatsApp liés aux commandes
DELETE FROM whatsapp_messages;

-- Supprime les paiements
DELETE FROM payments;

-- Supprime les lignes de commande (order_items)
DELETE FROM order_items;

-- Supprime les commandes
DELETE FROM orders;

-- Supprime les adresses (liées aux commandes)
DELETE FROM addresses;

-- Supprime les articles du panier (cart_items)
DELETE FROM cart_items;

-- Supprime les paniers
DELETE FROM carts;

-- Supprime les avis produit (reviews)
DELETE FROM reviews;

-- Supprime les images produit
DELETE FROM product_images;

-- Supprime les variantes produit
DELETE FROM product_variants;

-- Supprime les produits
DELETE FROM products;

-- Supprime les catégories (self-referential parent_id — safe après produits)
DELETE FROM categories;

-- Supprime les fournisseurs (optional — garder si tu veux les réutiliser)
-- DELETE FROM suppliers;

COMMIT;

-- Résumé
SELECT
    (SELECT COUNT(*) FROM products) as products,
    (SELECT COUNT(*) FROM categories) as categories,
    (SELECT COUNT(*) FROM orders) as orders,
    (SELECT COUNT(*) FROM product_images) as product_images,
    (SELECT COUNT(*) FROM carts) as carts,
    (SELECT COUNT(*) FROM reviews) as reviews,
    (SELECT COUNT(*) FROM payments) as payments,
    (SELECT COUNT(*) FROM users) as users_preserved;
