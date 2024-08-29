ALTER TABLE referee
    ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE referee
    ADD COLUMN last_login timestamp NULL;
