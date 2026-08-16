ALTER TABLE categories ADD COLUMN parent_id UUID REFERENCES categories(id);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
