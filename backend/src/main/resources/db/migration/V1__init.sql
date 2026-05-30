CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE restaurants (
    id                    UUID PRIMARY KEY,
    name                  TEXT NOT NULL,
    address               TEXT,
    lat                   DOUBLE PRECISION NOT NULL,
    lng                   DOUBLE PRECISION NOT NULL,
    geom                  GEOGRAPHY(Point, 4326)
                          GENERATED ALWAYS AS (
                              ST_SetSRID(ST_MakePoint(lng, lat), 4326)::geography
                          ) STORED,
    phone                 TEXT,
    website               TEXT,
    google_place_id       TEXT UNIQUE,
    cuisine_type          TEXT,
    cuisine_emoji         TEXT,
    description_es        TEXT,
    description_en        TEXT,
    menu_price            NUMERIC(6, 2),
    menu_details_raw      TEXT,
    vegetarian_options    BOOLEAN NOT NULL DEFAULT FALSE,
    gluten_free_options   BOOLEAN NOT NULL DEFAULT FALSE,
    days_from             VARCHAR(8),
    days_to               VARCHAR(8),
    excluded_day          VARCHAR(8),
    open_time             VARCHAR(5),
    close_time            VARCHAR(5),
    google_maps_url       VARCHAR(512),
    hidden                BOOLEAN NOT NULL DEFAULT FALSE,
    currency              VARCHAR(3) NOT NULL DEFAULT 'EUR',
    price_includes_es     JSONB NOT NULL DEFAULT '[]'::jsonb,
    price_includes_en     JSONB NOT NULL DEFAULT '[]'::jsonb,
    weekday_hours         JSONB NOT NULL DEFAULT '{}'::jsonb,
    opening_hours         JSONB NOT NULL DEFAULT '{}'::jsonb,
    rating                DOUBLE PRECISION,
    user_rating_count     INT,
    editorial_summary     TEXT,
    ai_summary            TEXT,
    reviews               JSONB NOT NULL DEFAULT '[]'::jsonb,
    serves_lunch          BOOLEAN NOT NULL DEFAULT FALSE,
    serves_vegetarian     BOOLEAN NOT NULL DEFAULT FALSE,
    outdoor_seating       BOOLEAN NOT NULL DEFAULT FALSE,
    reservable            BOOLEAN NOT NULL DEFAULT FALSE,
    takeout               BOOLEAN NOT NULL DEFAULT FALSE,
    photo_names           JSONB NOT NULL DEFAULT '[]'::jsonb,
    available_photo_names JSONB NOT NULL DEFAULT '[]'::jsonb,
    places_fetched_at     TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_restaurants_geom              ON restaurants USING GIST (geom);
CREATE INDEX idx_restaurants_places_fetched_at ON restaurants (places_fetched_at);
CREATE INDEX idx_restaurants_cuisine_type      ON restaurants (cuisine_type);

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
