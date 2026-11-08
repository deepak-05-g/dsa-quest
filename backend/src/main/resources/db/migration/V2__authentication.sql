CREATE TABLE users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(320) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT users_role_check CHECK (role IN ('STUDENT', 'ADMIN'))
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash CHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX refresh_tokens_user_id_idx ON refresh_tokens(user_id);
CREATE INDEX refresh_tokens_family_id_idx ON refresh_tokens(family_id);
CREATE INDEX refresh_tokens_expires_at_idx ON refresh_tokens(expires_at);
