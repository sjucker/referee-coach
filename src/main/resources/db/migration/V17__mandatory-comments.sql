ALTER TABLE video_report
    ADD COLUMN finished_at TIMESTAMP NULL;

ALTER TABLE video_report_comment
    ADD COLUMN requires_reply BOOLEAN NOT NULL DEFAULT FALSE;
