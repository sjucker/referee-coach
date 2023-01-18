ALTER TABLE video_report_comment
    ADD COLUMN id BIGINT FIRST;

-- assign a primary key to each comment
-- assumption: per video report no two comments have the same timestamp
WITH ranking AS (
    SELECT timestamp, video_report_id, RANK() OVER (ORDER BY video_report_id, timestamp) ranking
    FROM video_report_comment)
UPDATE video_report_comment, ranking r
SET video_report_comment.id = r.ranking
WHERE video_report_comment.video_report_id = r.video_report_id
  AND video_report_comment.timestamp = r.timestamp;

ALTER TABLE video_report_comment
    MODIFY id BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY;

ALTER TABLE video_report_comment
    DROP CONSTRAINT FK_VIDEO_COMMENT_ON_VIDEO_REPORT;
ALTER TABLE video_report_comment
    ADD CONSTRAINT FK_VIDEO_COMMENT_ON_VIDEO_REPORT FOREIGN KEY (video_report_id) REFERENCES video_report (id) ON DELETE CASCADE;


CREATE TABLE video_report_comment_reply
(
    id                      BIGINT AUTO_INCREMENT NOT NULL,
    replied_by              VARCHAR(255)          NOT NULL,
    replied_at              datetime              NOT NULL,
    reply                   VARCHAR(1024)         NOT NULL,
    video_report_comment_id BIGINT                NOT NULL,
    CONSTRAINT pk_video_report_comment_reply PRIMARY KEY (id)
);

ALTER TABLE video_report_comment_reply
    ADD CONSTRAINT FK_VIDEO_COMMENT_REPLY_ON_VIDEO_COMMENT FOREIGN KEY (video_report_comment_id) REFERENCES video_report_comment (id) ON DELETE CASCADE;
