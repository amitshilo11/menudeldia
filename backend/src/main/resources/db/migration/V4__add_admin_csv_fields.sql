-- V4: admin portal — fields that mirror the CSV source-of-truth + hidden flag.
-- The admin portal lets curators edit any of these from a web form; on save we
-- rewrite restaurants_db_ready.csv from the DB. The columns are nullable / use
-- safe defaults so existing rows do not need backfill.

ALTER TABLE restaurants
    ADD COLUMN hidden            BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN price_alt         VARCHAR(64),
    ADD COLUMN menu_details_raw  TEXT,
    ADD COLUMN includes_dessert  BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN includes_drink    BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN days_from         VARCHAR(8),
    ADD COLUMN days_to           VARCHAR(8),
    ADD COLUMN excluded_day      VARCHAR(8),
    ADD COLUMN open_time         VARCHAR(5),
    ADD COLUMN close_time        VARCHAR(5),
    ADD COLUMN google_maps_url   VARCHAR(512);
