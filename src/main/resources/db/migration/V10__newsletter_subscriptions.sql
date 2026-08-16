CREATE TABLE newsletter_subscriptions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    email VARCHAR(255),
    phone_number VARCHAR(255),
    CONSTRAINT uk_newsletter_subscriptions_email UNIQUE (email)
);
