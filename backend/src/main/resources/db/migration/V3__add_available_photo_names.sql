ALTER TABLE restaurants
  ADD COLUMN available_photo_names jsonb NOT NULL DEFAULT '[]'::jsonb;
