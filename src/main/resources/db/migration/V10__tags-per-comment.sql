CREATE TABLE video_report_comment_tags
(
    video_report_comment_id BIGINT NOT NULL,
    tag_id                  BIGINT NOT NULL,
    CONSTRAINT pk_comment_tags PRIMARY KEY (video_report_comment_id, tag_id),
    CONSTRAINT fk_comment_id FOREIGN KEY (video_report_comment_id) REFERENCES video_report_comment (id) ON DELETE CASCADE,
    CONSTRAINT fk_tag_id FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);
