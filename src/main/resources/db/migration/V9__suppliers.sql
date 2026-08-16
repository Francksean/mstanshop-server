CREATE TABLE suppliers (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    name VARCHAR(255) NOT NULL,
    primary_phone VARCHAR(255) NOT NULL,
    secondary_phone VARCHAR(255),
    location VARCHAR(255)
);

ALTER TABLE products ADD COLUMN supplier_id UUID REFERENCES suppliers(id);
