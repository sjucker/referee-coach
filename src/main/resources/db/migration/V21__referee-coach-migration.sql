INSERT INTO login (email, name, password, last_login, password_reset_token, admin, role)
SELECT email, name, password, last_login, password_reset_token, admin, 'COACH'
FROM coach;

INSERT INTO login (email, name, password, last_login, password_reset_token, admin, role)
SELECT email, name, password, last_login, password_reset_token, false, 'REFEREE'
FROM referee;

ALTER TABLE game_discussion
    DROP CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE1;
ALTER TABLE game_discussion
    DROP CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE2;
ALTER TABLE game_discussion
    DROP CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE3;

UPDATE game_discussion
SET referee1_id = (SELECT l.id
                   FROM login l
                            JOIN referee r ON r.email = l.email
                   WHERE r.id = referee1_id);

UPDATE game_discussion
SET referee2_id = (SELECT l.id
                   FROM login l
                            JOIN referee r ON r.email = l.email
                   WHERE r.id = referee2_id);

UPDATE game_discussion
SET referee3_id = (SELECT l.id
                   FROM login l
                            JOIN referee r ON r.email = l.email
                   WHERE r.id = referee3_id);

ALTER TABLE game_discussion
    ADD CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE1 FOREIGN KEY (referee1_id) REFERENCES login (id);
ALTER TABLE game_discussion
    ADD CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE2 FOREIGN KEY (referee2_id) REFERENCES login (id);
ALTER TABLE game_discussion
    ADD CONSTRAINT FK_GAME_DISCUSSION_ON_REFEREE3 FOREIGN KEY (referee3_id) REFERENCES login (id);

ALTER TABLE video_report
    DROP CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE1ID;
ALTER TABLE video_report
    DROP CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE2ID;
ALTER TABLE video_report
    DROP CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE3ID;
ALTER TABLE video_report
    DROP CONSTRAINT FK_VIDEO_REPORT_ON_REPORTERID;

UPDATE video_report
SET referee1_id = (SELECT l.id
                   FROM login l
                            JOIN referee r ON r.email = l.email
                   WHERE r.id = referee1_id);
UPDATE video_report
SET referee2_id = (SELECT l.id
                   FROM login l
                            JOIN referee r ON r.email = l.email
                   WHERE r.id = referee2_id);
UPDATE video_report
SET referee3_id = (SELECT l.id
                   FROM login l
                            JOIN referee r ON r.email = l.email
                   WHERE r.id = referee3_id);
UPDATE video_report
SET coach_id = (SELECT l.id
                FROM login l
                         JOIN coach c ON c.email = l.email
                WHERE c.id = coach_id);

ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE1 FOREIGN KEY (referee1_id) REFERENCES login (id);
ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE2 FOREIGN KEY (referee2_id) REFERENCES login (id);
ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_REFEREE3 FOREIGN KEY (referee3_id) REFERENCES login (id);
ALTER TABLE video_report
    ADD CONSTRAINT FK_VIDEO_REPORT_ON_COACH FOREIGN KEY (coach_id) REFERENCES login (id);

DROP TABLE coach;
DROP TABLE referee;
