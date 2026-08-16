-- Delivery address now captures the recipient's name and an optional WhatsApp number, so the
-- customer's contact details entered at checkout are visible on the order (admin view included).
ALTER TABLE addresses ADD COLUMN first_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE addresses ADD COLUMN last_name VARCHAR(255) NOT NULL DEFAULT '';
ALTER TABLE addresses ADD COLUMN whatsapp VARCHAR(255);

ALTER TABLE addresses ALTER COLUMN first_name DROP DEFAULT;
ALTER TABLE addresses ALTER COLUMN last_name DROP DEFAULT;
