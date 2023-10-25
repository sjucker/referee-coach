ALTER TABLE coach
    ADD COLUMN password_reset_token VARCHAR(255) NULL;

ALTER TABLE referee
    ADD COLUMN password_reset_token VARCHAR(255) NULL;
