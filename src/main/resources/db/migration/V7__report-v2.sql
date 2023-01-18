ALTER TABLE video_report
    ADD COLUMN general_comment VARCHAR(1024) NULL AFTER reportee;

ALTER TABLE video_report
    ADD COLUMN fitness_comment VARCHAR(1024) NULL AFTER image_comment;

ALTER TABLE video_report
    ADD COLUMN violations_comment VARCHAR(1024) NULL AFTER fouls_comment;

ALTER TABLE video_report
    ADD COLUMN image_score DECIMAL(2, 1) NULL AFTER image_comment;
ALTER TABLE video_report
    ADD COLUMN fitness_score DECIMAL(2, 1) NULL AFTER fitness_comment;
ALTER TABLE video_report
    ADD COLUMN mechanics_score DECIMAL(2, 1) NULL AFTER mechanics_comment;
ALTER TABLE video_report
    ADD COLUMN fouls_score DECIMAL(2, 1) NULL AFTER fouls_comment;
ALTER TABLE video_report
    ADD COLUMN violations_score DECIMAL(2, 1) NULL AFTER violations_comment;
ALTER TABLE video_report
    ADD COLUMN game_management_score DECIMAL(2, 1) NULL AFTER game_management_comment;

ALTER TABLE video_report
    ADD COLUMN version INT NOT NULL DEFAULT 1;


