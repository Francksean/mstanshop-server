CREATE TABLE whatsapp_templates (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ,
    genuka_template_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL UNIQUE,
    language VARCHAR(10) NOT NULL DEFAULT 'fr',
    category VARCHAR(20) NOT NULL DEFAULT 'UTILITY',
    event_code VARCHAR(50),
    components TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
);
