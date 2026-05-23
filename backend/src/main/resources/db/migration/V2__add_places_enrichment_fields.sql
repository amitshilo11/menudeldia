ALTER TABLE restaurants
    ADD COLUMN rating             DOUBLE PRECISION,
    ADD COLUMN user_rating_count  INT,
    ADD COLUMN editorial_summary  TEXT,
    ADD COLUMN ai_summary         TEXT,
    ADD COLUMN reviews            JSONB NOT NULL DEFAULT '[]'::jsonb,
    ADD COLUMN serves_lunch       BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN serves_vegetarian  BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN outdoor_seating    BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN reservable         BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN takeout            BOOLEAN NOT NULL DEFAULT FALSE;
