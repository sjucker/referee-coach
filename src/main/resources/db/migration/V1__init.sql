CREATE TABLE users
(
    id       BIGSERIAL
        CONSTRAINT pk_user PRIMARY KEY,
    email    VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    admin    BOOLEAN      NOT NULL
);

ALTER TABLE users
    ADD CONSTRAINT uc_user_email UNIQUE (email);
