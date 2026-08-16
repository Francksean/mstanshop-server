-- Category banner/thumbnail images and a single schedulable category-wide promotion slot.
ALTER TABLE categories ADD COLUMN banner_url VARCHAR(255);
ALTER TABLE categories ADD COLUMN thumbnail_url VARCHAR(255);
ALTER TABLE categories ADD COLUMN promo_discount_type VARCHAR(255);
ALTER TABLE categories ADD COLUMN promo_discount_value NUMERIC(12,2);
ALTER TABLE categories ADD COLUMN promo_starts_at TIMESTAMPTZ;
ALTER TABLE categories ADD COLUMN promo_ends_at TIMESTAMPTZ;
ALTER TABLE categories ADD COLUMN promo_active BOOLEAN NOT NULL DEFAULT false;
