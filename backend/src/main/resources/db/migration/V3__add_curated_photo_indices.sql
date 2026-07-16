-- Tracks curated photo selection by position in available_photo_names rather than by the
-- literal Google photo resource string, since those strings go stale and are re-fetched on
-- every enrichment refresh.
ALTER TABLE restaurants
    ADD COLUMN curated_photo_indices JSONB NOT NULL DEFAULT '[]'::jsonb;
