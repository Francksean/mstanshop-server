-- Supprime les données créées par DataSeeder.java (produits + catégories démo), sans toucher au compte admin.
BEGIN;

WITH demo_products AS (
    SELECT id FROM products
    WHERE name IN ('Robe en lin', 'Chemise oversize', 'Collier en argent',
                    'Bague dorée sertie', 'Sac à main en cuir', 'Écharpe en cachemire')
)
DELETE FROM reviews WHERE product_id IN (SELECT id FROM demo_products);

WITH demo_products AS (
    SELECT id FROM products
    WHERE name IN ('Robe en lin', 'Chemise oversize', 'Collier en argent',
                    'Bague dorée sertie', 'Sac à main en cuir', 'Écharpe en cachemire')
)
DELETE FROM cart_items WHERE product_id IN (SELECT id FROM demo_products);

WITH demo_products AS (
    SELECT id FROM products
    WHERE name IN ('Robe en lin', 'Chemise oversize', 'Collier en argent',
                    'Bague dorée sertie', 'Sac à main en cuir', 'Écharpe en cachemire')
)
DELETE FROM order_items WHERE product_id IN (SELECT id FROM demo_products);

WITH demo_products AS (
    SELECT id FROM products
    WHERE name IN ('Robe en lin', 'Chemise oversize', 'Collier en argent',
                    'Bague dorée sertie', 'Sac à main en cuir', 'Écharpe en cachemire')
)
DELETE FROM product_images WHERE product_id IN (SELECT id FROM demo_products);

WITH demo_products AS (
    SELECT id FROM products
    WHERE name IN ('Robe en lin', 'Chemise oversize', 'Collier en argent',
                    'Bague dorée sertie', 'Sac à main en cuir', 'Écharpe en cachemire')
)
DELETE FROM product_variants WHERE product_id IN (SELECT id FROM demo_products);

DELETE FROM products
WHERE name IN ('Robe en lin', 'Chemise oversize', 'Collier en argent',
               'Bague dorée sertie', 'Sac à main en cuir', 'Écharpe en cachemire');

DELETE FROM categories
WHERE name IN ('Vêtements', 'Bijoux', 'Accessoires')
  AND id NOT IN (SELECT DISTINCT category_id FROM products)
  AND id NOT IN (SELECT DISTINCT parent_id FROM categories WHERE parent_id IS NOT NULL);

COMMIT;
