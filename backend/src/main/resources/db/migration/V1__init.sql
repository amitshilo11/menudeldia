-- V1: initial schema for restaurants + users.
-- Hibernate ddl-auto=validate, so this SQL is the source of truth.

CREATE EXTENSION IF NOT EXISTS postgis;

-- ============================================================================
-- restaurants
-- ============================================================================
CREATE TABLE restaurants (
    id                  UUID PRIMARY KEY,
    name                TEXT NOT NULL,
    address             TEXT,
    lat                 DOUBLE PRECISION NOT NULL,
    lng                 DOUBLE PRECISION NOT NULL,
    -- Generated geography column for fast ST_DWithin queries.
    geom                GEOGRAPHY(Point, 4326)
                        GENERATED ALWAYS AS (
                            ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography
                        ) STORED,
    phone               TEXT,
    website             TEXT,
    google_place_id     TEXT UNIQUE,
    cuisine_type        TEXT,
    cuisine_emoji       TEXT,
    description_es      TEXT,
    description_en      TEXT,
    menu_price          NUMERIC(6, 2),
    currency            CHAR(3) NOT NULL DEFAULT 'EUR',
    price_includes_es   JSONB NOT NULL DEFAULT '[]'::jsonb,
    price_includes_en   JSONB NOT NULL DEFAULT '[]'::jsonb,
    weekday_hours       JSONB NOT NULL DEFAULT '{}'::jsonb,
    opening_hours       JSONB NOT NULL DEFAULT '{}'::jsonb,
    photo_count         INT NOT NULL DEFAULT 0,
    places_fetched_at   TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_restaurants_geom              ON restaurants USING GIST (geom);
CREATE INDEX idx_restaurants_places_fetched_at ON restaurants (places_fetched_at);
CREATE INDEX idx_restaurants_cuisine_type      ON restaurants (cuisine_type);

-- ============================================================================
-- users
-- ============================================================================
CREATE TABLE users (
    id            UUID PRIMARY KEY,
    provider      TEXT NOT NULL CHECK (provider IN ('google', 'apple')),
    external_id   TEXT NOT NULL,
    email         TEXT,
    display_name  TEXT,
    avatar_url    TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_login    TIMESTAMPTZ,
    CONSTRAINT uq_users_provider_external_id UNIQUE (provider, external_id)
);

-- ============================================================================
-- updated_at trigger
-- ============================================================================
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_restaurants_updated_at
    BEFORE UPDATE ON restaurants
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
