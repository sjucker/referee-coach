ALTER TABLE video_report
    ADD COLUMN general_comment VARCHAR(1024) NULL;

ALTER TABLE video_report
    ADD COLUMN fitness_comment VARCHAR(1024) NULL;

ALTER TABLE video_report
    ADD COLUMN violations_comment VARCHAR(1024) NULL;

ALTER TABLE video_report
    ADD COLUMN image_score DECIMAL(2, 1) NULL;
ALTER TABLE video_report
    ADD COLUMN fitness_score DECIMAL(2, 1) NULL;
ALTER TABLE video_report
    ADD COLUMN mechanics_score DECIMAL(2, 1) NULL;
ALTER TABLE video_report
    ADD COLUMN fouls_score DECIMAL(2, 1) NULL;
ALTER TABLE video_report
    ADD COLUMN violations_score DECIMAL(2, 1) NULL;
ALTER TABLE video_report
    ADD COLUMN game_management_score DECIMAL(2, 1);

ALTER TABLE video_report
    ADD COLUMN version INT NOT NULL DEFAULT 1;


