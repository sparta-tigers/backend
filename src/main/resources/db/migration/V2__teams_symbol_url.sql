ALTER TABLE teams ADD COLUMN symbol_url VARCHAR(2048);
UPDATE teams SET symbol_url = path WHERE path IS NOT NULL;
ALTER TABLE teams DROP COLUMN path;
