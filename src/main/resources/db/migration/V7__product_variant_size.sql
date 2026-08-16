ALTER TABLE product_variants ADD COLUMN size VARCHAR(50) NOT NULL DEFAULT '';
ALTER TABLE product_variants ALTER COLUMN size DROP DEFAULT;

ALTER TABLE product_variants DROP CONSTRAINT uk_product_variants_product_color;
ALTER TABLE product_variants ADD CONSTRAINT uk_product_variants_product_color_size
    UNIQUE (product_id, color_hex, size);

ALTER TABLE order_items ADD COLUMN variant_size VARCHAR(50);
