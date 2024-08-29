CREATE TABLE game_discussion
(
    id               VARCHAR(255) NOT NULL,
    game_number      VARCHAR(255) NOT NULL,
    competition      VARCHAR(255) NOT NULL,
    date             date         NOT NULL,
    result           VARCHAR(255) NOT NULL,
    team_a           VARCHAR(255) NOT NULL,
    team_b           VARCHAR(255) NOT NULL,
    officiating_mode VARCHAR(255) NOT NULL,
    referee1_id      BIGINT       NULL,
    referee2_id      BIGINT       NULL,
    referee3_id      BIGINT       NULL,
    youtube_id       VARCHAR(255) NOT NULL,
    CONSTRAINT PK_GAME_DISCUSSION PRIMARY KEY (id)
);

ALTER TABLE game_discussion
    ADD CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE1 FOREIGN KEY (referee1_id) REFERENCES referee (id);

ALTER TABLE game_discussion
    ADD CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE2 FOREIGN KEY (referee2_id) REFERENCES referee (id);

ALTER TABLE game_discussion
    ADD CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE3 FOREIGN KEY (referee3_id) REFERENCES referee (id);

CREATE TABLE game_discussion_comment
(
    id                 BIGSERIAL
        CONSTRAINT PK_GAME_DISCUSSION_COMMENT PRIMARY KEY,
    timestamp          INT           NOT NULL,
    comment            VARCHAR(1024) NOT NULL,
    game_discussion_id VARCHAR(255)  NOT NULL
);

ALTER TABLE game_discussion_comment
    ADD CONSTRAINT FK_GAME_DISCUSSION_COMMENT FOREIGN KEY (game_discussion_id) REFERENCES game_discussion (id) ON DELETE CASCADE;

CREATE TABLE game_discussion_comment_reply
(
    id                         BIGSERIAL
        CONSTRAINT PK_GAME_DISCUSSION_COMMENT_REPLY PRIMARY KEY,
    replied_by                 VARCHAR(255)  NOT NULL,
    replied_at                 timestamp     NOT NULL,
    reply                      VARCHAR(1024) NOT NULL,
    game_discussion_comment_id BIGINT        NOT NULL
);

ALTER TABLE game_discussion_comment_reply
    ADD CONSTRAINT FK_GAME_DISCUSSION_COMMENT_REPLY FOREIGN KEY (game_discussion_comment_id) REFERENCES game_discussion_comment (id) ON DELETE CASCADE;
