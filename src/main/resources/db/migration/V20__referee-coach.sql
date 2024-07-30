CREATE TABLE login
(
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    email                VARCHAR(255) NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    password             VARCHAR(255) NOT NULL,
    last_login           datetime     NULL,
    password_reset_token VARCHAR(255) NULL,
    admin                BIT(1)       NOT NULL,
    role                 VARCHAR(255) NOT NULL,
    CONSTRAINT pk_login PRIMARY KEY (id),
    CONSTRAINT uq_email UNIQUE (email)
);
