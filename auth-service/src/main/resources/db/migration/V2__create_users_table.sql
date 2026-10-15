CREATE TABLE users (
    id UUID primary key,
    email VARCHAR(254) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT  TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
