-- Migrate item status from stock-related values to condition values
-- Old: IN_STOCK, LOW_STOCK, OUT_OF_STOCK
-- New: AVAILABLE, TO_VERIFY, NEEDS_MAINTENANCE, DAMAGED

UPDATE items SET status = 'AVAILABLE' WHERE status = 'IN_STOCK';
UPDATE items SET status = 'TO_VERIFY' WHERE status = 'LOW_STOCK';
UPDATE items SET status = 'TO_VERIFY' WHERE status = 'OUT_OF_STOCK';

-- Safety catch-all for any unexpected values
UPDATE items SET status = 'AVAILABLE' WHERE status NOT IN ('AVAILABLE', 'TO_VERIFY', 'NEEDS_MAINTENANCE', 'DAMAGED');
