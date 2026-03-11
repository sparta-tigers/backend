-- Add device_token column to users table for push notification
ALTER TABLE users
    ADD COLUMN device_token VARCHAR(255) NULL;
