CREATE TABLE IF NOT EXISTS accounts (
    id INTEGER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_salt BYTEA NOT NULL CHECK (octet_length(password_salt) = 32),
    password_hash BYTEA NOT NULL CHECK (octet_length(password_hash) = 32),
    password_iterations INTEGER NOT NULL CHECK (password_iterations >= 100000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (length(username) BETWEEN 1 AND 64)
);
CREATE TABLE IF NOT EXISTS characters (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id INTEGER NOT NULL REFERENCES accounts(id),
    name VARCHAR(16) NOT NULL CHECK (length(name) BETWEEN 2 AND 16),
    gender SMALLINT NOT NULL CHECK (gender BETWEEN 0 AND 1),
    skin SMALLINT NOT NULL CHECK (skin BETWEEN 0 AND 4),
    region SMALLINT NOT NULL CHECK (region = 0),
    items SMALLINT[] NOT NULL CHECK (cardinality(items) = 12 AND array_ndims(items) = 1),
    colors SMALLINT[] NOT NULL CHECK (cardinality(colors) = 12 AND array_ndims(colors) = 1),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE UNIQUE INDEX IF NOT EXISTS characters_name_unique ON characters (lower(name));
CREATE INDEX IF NOT EXISTS characters_account ON characters (account_id, id);

ALTER TABLE characters ADD COLUMN IF NOT EXISTS map_group SMALLINT NOT NULL DEFAULT 4;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS map_id SMALLINT NOT NULL DEFAULT 1;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS position_x SMALLINT NOT NULL DEFAULT 6;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS position_y SMALLINT NOT NULL DEFAULT 6;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS elevation SMALLINT NOT NULL DEFAULT 3;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS facing SMALLINT NOT NULL DEFAULT 0;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS return_position SMALLINT[] NOT NULL DEFAULT ARRAY[]::SMALLINT[];
