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
    region SMALLINT NOT NULL CHECK (region BETWEEN 0 AND 5),
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

ALTER TABLE characters ADD COLUMN IF NOT EXISTS dex_unlocked BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE characters DROP CONSTRAINT IF EXISTS characters_region_check;
ALTER TABLE characters ADD CONSTRAINT characters_region_check CHECK (region BETWEEN 0 AND 5);

ALTER TABLE characters ADD COLUMN IF NOT EXISTS money BIGINT NOT NULL DEFAULT 0 CHECK (money >= 0);
ALTER TABLE characters ADD COLUMN IF NOT EXISTS battle_points BIGINT NOT NULL DEFAULT 0 CHECK (battle_points >= 0);
ALTER TABLE characters ADD COLUMN IF NOT EXISTS online_seconds BIGINT NOT NULL DEFAULT 0 CHECK (online_seconds >= 0);
ALTER TABLE characters ADD COLUMN IF NOT EXISTS kanto_badges SMALLINT NOT NULL DEFAULT 0 CHECK (kanto_badges BETWEEN 0 AND 8);
ALTER TABLE characters ADD COLUMN IF NOT EXISTS hoenn_badges SMALLINT NOT NULL DEFAULT 0 CHECK (hoenn_badges BETWEEN 0 AND 8);

ALTER TABLE characters ADD COLUMN IF NOT EXISTS kanto_champion BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE characters ADD COLUMN IF NOT EXISTS hoenn_champion BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS party_species SMALLINT[] NOT NULL
    DEFAULT ARRAY[1,4,7,150,151]::SMALLINT[]
    CHECK (cardinality(party_species) BETWEEN 0 AND 6 AND 0 < ALL(party_species) AND 1026 > ALL(party_species));
ALTER TABLE characters ADD COLUMN IF NOT EXISTS party_flags SMALLINT[] NOT NULL
    DEFAULT ARRAY[2,2,2,0,0]::SMALLINT[]
    CHECK (cardinality(party_flags) = cardinality(party_species) AND party_flags <@ ARRAY[0,1,2,3]::SMALLINT[]);
ALTER TABLE characters ALTER COLUMN party_flags SET DEFAULT ARRAY[2,2,2,0,0]::SMALLINT[];
UPDATE characters SET party_flags=ARRAY(SELECT (flags & 2)::SMALLINT FROM unnest(party_flags) AS flags)
    WHERE cardinality(party_species)=5 AND party_species @> ARRAY[1,4,7,150,151]::SMALLINT[]
        AND party_flags && ARRAY[1,3]::SMALLINT[];
ALTER TABLE characters ADD COLUMN IF NOT EXISTS party_hp INTEGER[];
ALTER TABLE characters ADD COLUMN IF NOT EXISTS party_max_hp INTEGER[];
UPDATE characters SET party_hp=array_fill(100, ARRAY[cardinality(party_species)]) WHERE party_hp IS NULL;
UPDATE characters SET party_max_hp=array_fill(100, ARRAY[cardinality(party_species)]) WHERE party_max_hp IS NULL;
ALTER TABLE characters ALTER COLUMN party_hp SET DEFAULT ARRAY[100,100,100,100,100];
ALTER TABLE characters ALTER COLUMN party_max_hp SET DEFAULT ARRAY[100,100,100,100,100];
ALTER TABLE characters ALTER COLUMN party_hp SET NOT NULL;
ALTER TABLE characters ALTER COLUMN party_max_hp SET NOT NULL;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS party_levels SMALLINT[];
UPDATE characters SET party_levels=array_fill(0::SMALLINT, ARRAY[cardinality(party_species)]) WHERE party_levels IS NULL;
ALTER TABLE characters ALTER COLUMN party_levels SET DEFAULT ARRAY[0,0,0,0,0]::SMALLINT[];
ALTER TABLE characters ALTER COLUMN party_levels SET NOT NULL;

CREATE TABLE IF NOT EXISTS character_inventory (
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    item_id INTEGER NOT NULL CHECK (item_id BETWEEN 1 AND 32767),
    quantity INTEGER NOT NULL CHECK (quantity BETWEEN 1 AND 9999),
    PRIMARY KEY (character_id, item_id)
);

ALTER TABLE character_inventory DROP CONSTRAINT IF EXISTS character_inventory_item_id_check;
ALTER TABLE character_inventory ADD CONSTRAINT character_inventory_item_id_check CHECK (item_id BETWEEN 1 AND 32767);

CREATE TABLE IF NOT EXISTS character_pokemon (
    individual_id BIGINT PRIMARY KEY CHECK (individual_id > 0),
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    slot SMALLINT NOT NULL CHECK (slot BETWEEN 0 AND 5),
    species SMALLINT NOT NULL CHECK (species BETWEEN 1 AND 1025),
    individual_data BYTEA NOT NULL,
    UNIQUE (character_id, slot)
);

ALTER TABLE characters ADD COLUMN IF NOT EXISTS pc_boxes SMALLINT NOT NULL DEFAULT 1;

ALTER TABLE characters DROP CONSTRAINT IF EXISTS characters_pc_boxes_check;
ALTER TABLE characters ADD CONSTRAINT characters_pc_boxes_check CHECK (pc_boxes BETWEEN 1 AND 13);

CREATE TABLE IF NOT EXISTS character_box_pokemon (
    individual_id BIGINT PRIMARY KEY CHECK (individual_id > 0),
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    box SMALLINT NOT NULL CHECK (box BETWEEN 0 AND 12),
    slot SMALLINT NOT NULL CHECK (slot BETWEEN 0 AND 59),
    species SMALLINT NOT NULL CHECK (species BETWEEN 1 AND 1025),
    flags SMALLINT NOT NULL CHECK (flags BETWEEN 0 AND 4),
    level SMALLINT NOT NULL CHECK (level BETWEEN 1 AND 100),
    hp INTEGER NOT NULL CHECK (hp BETWEEN 0 AND 65535),
    individual_data BYTEA NOT NULL,
    UNIQUE (character_id, box, slot)
);

ALTER TABLE characters ADD COLUMN IF NOT EXISTS unova_badges SMALLINT NOT NULL DEFAULT 0 CHECK (unova_badges BETWEEN 0 AND 8);
ALTER TABLE characters ADD COLUMN IF NOT EXISTS unova_champion BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS sinnoh_badges SMALLINT NOT NULL DEFAULT 0 CHECK (sinnoh_badges BETWEEN 0 AND 8);
ALTER TABLE characters ADD COLUMN IF NOT EXISTS sinnoh_champion BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS cycling BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS debug_noclip BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE characters ADD COLUMN IF NOT EXISTS debug_speed_multiplier SMALLINT NOT NULL DEFAULT 1 CHECK (debug_speed_multiplier IN (1,2,4,8));

ALTER TABLE characters DROP CONSTRAINT IF EXISTS characters_party_species_check;
ALTER TABLE characters ADD CONSTRAINT characters_party_species_check
    CHECK (cardinality(party_species) BETWEEN 0 AND 6 AND 0 < ALL(party_species) AND 1026 > ALL(party_species));
ALTER TABLE character_pokemon DROP CONSTRAINT IF EXISTS character_pokemon_species_check;
ALTER TABLE character_pokemon ADD CONSTRAINT character_pokemon_species_check CHECK (species BETWEEN 1 AND 1025);
ALTER TABLE character_box_pokemon DROP CONSTRAINT IF EXISTS character_box_pokemon_species_check;
ALTER TABLE character_box_pokemon ADD CONSTRAINT character_box_pokemon_species_check CHECK (species BETWEEN 1 AND 1025);

ALTER TABLE characters ADD COLUMN IF NOT EXISTS kanto_opening_complete BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE characters ADD COLUMN IF NOT EXISTS wild_encounters_enabled BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE characters ADD COLUMN IF NOT EXISTS story_completed TEXT[] NOT NULL DEFAULT ARRAY[]::text[];
ALTER TABLE characters ADD COLUMN IF NOT EXISTS gba_story_state TEXT NOT NULL DEFAULT '';
CREATE TABLE IF NOT EXISTS character_script_gifts (
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    rom_hash TEXT NOT NULL,
    instruction INTEGER NOT NULL,
    individual_id BIGINT NOT NULL,
    species INTEGER NOT NULL,
    PRIMARY KEY(character_id, rom_hash, instruction)
);

CREATE TABLE IF NOT EXISTS character_trainers (
    character_id BIGINT NOT NULL REFERENCES characters(id) ON DELETE CASCADE,
    region SMALLINT NOT NULL DEFAULT 0 CHECK (region=0),
    trainer_id INTEGER NOT NULL CHECK (trainer_id BETWEEN 1 AND 742),
    PRIMARY KEY(character_id,region,trainer_id)
);
