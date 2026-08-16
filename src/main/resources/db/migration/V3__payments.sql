-- Real payment tracking (CamPay MTN MoMo / Orange Money collections). One order can have several
-- attempts (a failed/expired collection can be retried), hence a separate table rather than
-- columns on orders.
CREATE TABLE payments (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    order_id UUID NOT NULL REFERENCES orders(id),
    external_reference UUID NOT NULL,
    provider_reference VARCHAR(255),
    status VARCHAR(255) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    CONSTRAINT uk_payments_external_reference UNIQUE (external_reference)
);

CREATE INDEX idx_payments_provider_reference ON payments(provider_reference);
CREATE INDEX idx_payments_order_id ON payments(order_id);
