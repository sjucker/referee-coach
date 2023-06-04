ALTER TABLE referee
    ADD CONSTRAINT uq__referee_email UNIQUE (email);
