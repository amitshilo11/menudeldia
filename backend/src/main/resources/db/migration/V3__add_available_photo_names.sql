ALTER TABLE restaurants
  ADD COLUMN available_photo_names jsonb NOT NULL DEFAULT '[]'::jsonb;

ALTER TABLE restaurants
    ADD COLUMN hidden              BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN vegetarian_options  BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN gluten_free_options BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN menu_details_raw    TEXT,
    ADD COLUMN days_from           VARCHAR(8),
    ADD COLUMN days_to             VARCHAR(8),
    ADD COLUMN excluded_day        VARCHAR(8),
    ADD COLUMN open_time           VARCHAR(5),
    ADD COLUMN close_time          VARCHAR(5),
    ADD COLUMN google_maps_url     VARCHAR(512);
