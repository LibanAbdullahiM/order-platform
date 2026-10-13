CREATE TABLE users (
    id UUID primary key,
    email VARCHAR(254) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    enabled BOOLEAN,
    created_at TIMESTAMPTZ DEFAULT NOW()
);
