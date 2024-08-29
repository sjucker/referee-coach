ALTER TABLE video_report_comment
    ADD COLUMN id BIGSERIAL
        CONSTRAINT pk_video_report_comment PRIMARY KEY;

ALTER TABLE video_report_comment
    DROP CONSTRAINT FK_VIDEO_COMMENT_ON_VIDEO_REPORT;
ALTER TABLE video_report_comment
    ADD CONSTRAINT FK_VIDEO_COMMENT_ON_VIDEO_REPORT FOREIGN KEY (video_report_id) REFERENCES video_report (id) ON DELETE CASCADE;


CREATE TABLE video_report_comment_reply
(
    id                      BIGSERIAL
        CONSTRAINT pk_video_report_comment_reply PRIMARY KEY,
    replied_by              VARCHAR(255)  NOT NULL,
    replied_at              TIMESTAMP     NOT NULL,
    reply                   VARCHAR(1024) NOT NULL,
    video_report_comment_id BIGINT        NOT NULL
);

ALTER TABLE video_report_comment_reply
    ADD CONSTRAINT FK_VIDEO_COMMENT_REPLY_ON_VIDEO_COMMENT FOREIGN KEY (video_report_comment_id) REFERENCES video_report_comment (id) ON DELETE CASCADE;
