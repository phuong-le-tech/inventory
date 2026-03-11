ALTER TABLE items ADD COLUMN image_key VARCHAR(500);

CREATE INDEX idx_items_legacy_images ON items (id) WHERE image_data IS NOT NULL AND image_key IS NULL;
