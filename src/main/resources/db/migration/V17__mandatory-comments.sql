ALTER TABLE video_report
    ADD COLUMN finished_at DATETIME NULL AFTER finished;

ALTER TABLE video_report_comment
    ADD COLUMN requires_reply BIT NOT NULL DEFAULT 0;
