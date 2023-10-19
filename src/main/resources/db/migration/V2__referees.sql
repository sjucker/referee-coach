CREATE TABLE referee
(
    id    BIGINT       NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    name  VARCHAR(255) NOT NULL,
    level VARCHAR(255) NOT NULL,
    CONSTRAINT pk_referee PRIMARY KEY (id)
);
