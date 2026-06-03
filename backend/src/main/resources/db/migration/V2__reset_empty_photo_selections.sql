-- Reset places_fetched_at for restaurants that have available photos but no curated
-- selection, so the next enrichment run auto-picks the first 5 photos.
UPDATE restaurants
SET places_fetched_at = NULL
WHERE photo_names = '[]'::jsonb
  AND available_photo_names != '[]'::jsonb;
