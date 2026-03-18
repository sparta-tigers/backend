-- Add location columns to items for map-based search and marker display
ALTER TABLE items
    ADD COLUMN latitude DOUBLE NULL,
    ADD COLUMN longitude DOUBLE NULL,
    ADD COLUMN address VARCHAR(255) NULL;
