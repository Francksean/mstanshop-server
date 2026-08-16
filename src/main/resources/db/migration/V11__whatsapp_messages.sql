CREATE TABLE whatsapp_messages (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    order_id UUID NOT NULL REFERENCES orders(id),
    phone_number VARCHAR(255) NOT NULL,
    template_name VARCHAR(255) NOT NULL,
    provider_message_id VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    error_message VARCHAR(1000)
);
