ALTER TABLE referee
    ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';

ALTER TABLE referee
    ADD COLUMN last_login datetime NULL;

UPDATE referee
SET password = CONCAT('{noop}', SUBSTR(MD5(RAND()), 1, 10));
