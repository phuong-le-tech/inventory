ALTER TABLE items ADD COLUMN barcode VARCHAR(255);

CREATE INDEX idx_items_barcode ON items (barcode);
