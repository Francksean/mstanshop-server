-- Snapshot of the product thumbnail URL at order time, so order history keeps showing what the
-- customer actually bought even if the product's images later change or the product is deleted.
ALTER TABLE order_items ADD COLUMN product_thumbnail_url VARCHAR(255);
