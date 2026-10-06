ALTER TABLE login
    ADD COLUMN user_handle VARBINARY(64) NULL,
    ADD CONSTRAINT uq_user_handle UNIQUE (user_handle);

CREATE TABLE passkey_credential
(
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    user_id         BIGINT         NOT NULL,
    credential_id   VARBINARY(1024) NOT NULL,
    public_key_cose VARBINARY(2048) NOT NULL,
    signature_count BIGINT         NOT NULL,
    name            VARCHAR(255)   NOT NULL,
    created_at      datetime       NOT NULL,
    last_used_at    datetime       NULL,
    CONSTRAINT pk_passkey_credential PRIMARY KEY (id),
    CONSTRAINT uq_passkey_credential_id UNIQUE (credential_id),
    CONSTRAINT fk_passkey_credential_user_id FOREIGN KEY (user_id) REFERENCES login (id) ON DELETE CASCADE
);

CREATE TABLE passkey_ceremony
(
    id           VARCHAR(36) NOT NULL,
    request_json TEXT        NOT NULL,
    user_id      BIGINT      NULL,
    created_at   datetime    NOT NULL,
    CONSTRAINT pk_passkey_ceremony PRIMARY KEY (id),
    CONSTRAINT fk_passkey_ceremony_user_id FOREIGN KEY (user_id) REFERENCES login (id) ON DELETE CASCADE
);
