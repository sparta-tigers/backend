-- Add location columns to items for map-based search and marker display
ALTER TABLE items
    ADD COLUMN latitude DOUBLE NULL,
    ADD COLUMN longitude DOUBLE NULL,
    ADD COLUMN address VARCHAR(255) NULL;

SET FOREIGN_KEY_CHECKS = 0;
ALTER TABLE attendance_image CHANGE s3url image_url VARCHAR(255);
SET FOREIGN_KEY_CHECKS = 1;