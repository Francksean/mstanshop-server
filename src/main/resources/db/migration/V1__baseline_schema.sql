-- Baseline schema migration.
-- Recreates, table by table, the exact structure that Hibernate's ddl-auto (create/validate)
-- has been deriving from the JPA entities up to this point. From here on, schema changes are
-- expressed as new Vn__*.sql files instead of being auto-generated.

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255),
    CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    deleted BOOLEAN NOT NULL,
    deleted_at TIMESTAMPTZ,
    auth_provider VARCHAR(255) NOT NULL,
    provider_id VARCHAR(255),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    token VARCHAR(512) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMPTZ NOT NULL,
    revoked BOOLEAN NOT NULL,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token)
);

CREATE TABLE password_reset_tokens (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    token VARCHAR(512) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL,
    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token)
);

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE TABLE products (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description VARCHAR(4000),
    price NUMERIC(12,2) NOT NULL,
    stock INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(id),
    CONSTRAINT uk_products_slug UNIQUE (slug)
);

CREATE TABLE product_variants (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    product_id UUID NOT NULL REFERENCES products(id),
    color_name VARCHAR(255),
    color_hex VARCHAR(255) NOT NULL,
    stock INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT uk_product_variants_product_color UNIQUE (product_id, color_hex)
);

CREATE TABLE product_images (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    product_id UUID NOT NULL REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),
    url VARCHAR(255) NOT NULL,
    is_thumbnail BOOLEAN NOT NULL
);

CREATE TABLE carts (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    user_id UUID NOT NULL REFERENCES users(id),
    CONSTRAINT uk_carts_user UNIQUE (user_id)
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    cart_id UUID NOT NULL REFERENCES carts(id),
    product_id UUID NOT NULL REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),
    quantity INTEGER NOT NULL
);

CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    user_id UUID NOT NULL REFERENCES users(id),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    postal_code VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    phone VARCHAR(255)
);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    order_number VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    address_id UUID NOT NULL REFERENCES addresses(id),
    status VARCHAR(255) NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL,
    subtotal_amount NUMERIC(12,2),
    discount_amount NUMERIC(12,2),
    promo_code VARCHAR(255),
    CONSTRAINT uk_orders_order_number UNIQUE (order_number)
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    order_id UUID NOT NULL REFERENCES orders(id),
    product_id UUID NOT NULL REFERENCES products(id),
    variant_id UUID REFERENCES product_variants(id),
    product_name VARCHAR(255) NOT NULL,
    variant_color_name VARCHAR(255),
    variant_color_hex VARCHAR(255),
    unit_price NUMERIC(12,2) NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE TABLE promo_codes (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    code VARCHAR(255) NOT NULL,
    discount_type VARCHAR(255) NOT NULL,
    discount_value NUMERIC(12,2) NOT NULL,
    expires_at TIMESTAMPTZ,
    min_order_amount NUMERIC(12,2),
    max_uses INTEGER,
    uses_count INTEGER NOT NULL,
    active BOOLEAN NOT NULL,
    CONSTRAINT uk_promo_codes_code UNIQUE (code)
);

CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    product_id UUID NOT NULL REFERENCES products(id),
    user_id UUID NOT NULL REFERENCES users(id),
    rating INTEGER NOT NULL,
    comment VARCHAR(2000),
    CONSTRAINT uk_reviews_product_user UNIQUE (product_id, user_id)
);
