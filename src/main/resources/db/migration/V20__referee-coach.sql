CREATE TABLE login
(
    id                   BIGSERIAL
        CONSTRAINT pk_login PRIMARY KEY,
    email                VARCHAR(255) NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    password             VARCHAR(255) NOT NULL,
    last_login           TIMESTAMP    NULL,
    password_reset_token VARCHAR(255) NULL,
    admin                BOOLEAN      NOT NULL,
    role                 VARCHAR(255) NOT NULL,
    CONSTRAINT uq_email UNIQUE (email)
);
