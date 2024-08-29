CREATE TABLE referee
(
    id    BIGSERIAL
        CONSTRAINT pk_referee PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    name  VARCHAR(255) NOT NULL,
    level VARCHAR(255) NOT NULL
);
