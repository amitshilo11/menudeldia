-- Coordinates were previously treated as "not yet set" by comparing them against a
-- sentinel value. That sentinel changed from Barcelona's centre (41.3851, 2.1734) to
-- (0, 0), and the comparison is exact — so every row created under the old sentinel
-- became invisible to the fill-when-empty check in PlacesEnrichmentService and never
-- received its real location. Those rows enrich fine in every other respect, which is
-- why the gap went unnoticed: they simply all sat on the city centre.
--
-- An explicit flag replaces the sentinel comparison, so changing or colliding with a
-- coordinate value can no longer strand a row.
ALTER TABLE restaurants
    ADD COLUMN coords_verified BOOLEAN NOT NULL DEFAULT FALSE;

-- Anything not parked on a known sentinel already holds a real, Places-supplied location.
-- The remainder stay FALSE and are repaired by POST /admin/restaurants/backfill-coords.
UPDATE restaurants
   SET coords_verified = TRUE
 WHERE NOT (lat = 0 AND lng = 0)
   AND NOT (ROUND(lat::NUMERIC, 4) = 41.3851 AND ROUND(lng::NUMERIC, 4) = 2.1734);
