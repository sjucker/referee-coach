CREATE TABLE video_report
(
    id                        VARCHAR(255)  NOT NULL,
    game_number               VARCHAR(255)  NOT NULL,
    competition               VARCHAR(255)  NOT NULL,
    date                      DATE          NOT NULL,
    result                    VARCHAR(255)  NOT NULL,
    team_a                    VARCHAR(255)  NOT NULL,
    team_b                    VARCHAR(255)  NOT NULL,
    officiating_mode          VARCHAR(255)  NOT NULL,
    referee1_id               BIGINT        NULL,
    referee2_id               BIGINT        NULL,
    referee3_id               BIGINT        NULL,
    youtube_id                VARCHAR(255)  NULL,
    reporter_id               BIGINT        NOT NULL,
    reportee                  VARCHAR(255)  NOT NULL,
    image_comment             VARCHAR(1024) NULL,
    mechanics_comment         VARCHAR(1024) NULL,
    fouls_comment             VARCHAR(1024) NULL,
    game_management_comment   VARCHAR(1024) NULL,
    points_to_keep_comment    VARCHAR(1024) NULL,
    points_to_improve_comment VARCHAR(1024) NULL,
    finished                  BIT(1)        NOT NULL,
    CONSTRAINT pk_video_report PRIMARY KEY (id)
);

ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE1ID FOREIGN KEY (referee1_id) REFERENCES referee (id);

ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE2ID FOREIGN KEY (referee2_id) REFERENCES referee (id);

ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE3ID FOREIGN KEY (referee3_id) REFERENCES referee (id);

ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_REPORTERID FOREIGN KEY (reporter_id) REFERENCES user (id);

CREATE TABLE video_report_comment
(
    timestamp       INT           NOT NULL,
    comment         VARCHAR(1024) NOT NULL,
    video_report_id VARCHAR(255)  NOT NULL
);

ALTER TABLE video_report_comment
    ADD CONSTRAINT FK_VIDEO_COMMENT_ON_VIDEO_REPORT FOREIGN KEY (video_report_id) REFERENCES video_report (id);
