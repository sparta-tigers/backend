-- Change items.image column from VARCHAR(255) to TEXT to support JSON image URLs
ALTER TABLE items MODIFY COLUMN image TEXT COLLATE utf8mb4_unicode_ci;
