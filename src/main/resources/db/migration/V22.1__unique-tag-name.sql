ALTER TABLE tags
    ADD CONSTRAINT uq_tags_name UNIQUE (name);
