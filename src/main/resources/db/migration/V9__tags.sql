CREATE TABLE tags
(
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name    VARCHAR(255) NOT NULL,
    CONSTRAINT pk_tags PRIMARY KEY (id)
);

INSERT INTO tags (name)
VALUES ('TRAIL'),
       ('CENTER'),
       ('LEAD'),
       ('TV - TRAVELLING'),
       ('DD - DOUBLE DRIBBLE'),
       ('BCV - BACKCOURT'),
       ('TIME V. - TIME 3/5/8'),
       ('SCV - SHOT CLOCK VIOLATION'),
       ('OOB_T-IN - OUT OF BOUNDS & THROW-IN'),
       ('GT_BI - GOAL TENDING AND INTERFERENCE'),
       ('UF - UNSPORTSMANLIKE FOUL'),
       ('FK - FAKE BEING FOULED'),
       ('AOS D - AOS: DEFENDER'),
       ('AOS S - AOS: SHOOTER'),
       ('NL_CH - BLOCK-CHARGE'),
       ('CYL - CYLINDER PLAY'),
       ('SCR - SCREENS'),
       ('PNR - PICK AND ROLL'),
       ('IUA_DEF - USE OH HANDS/ARMS DEFENSIVE'),
       ('IUA_OF - USE OF HANDS/ARMS OFFENSIVE'),
       ('SHOTS - MOVING SHOTS (AOS or not)'),
       ('MC - MARGINAL CALLS'),
       ('OP - OBVIOUS PLAYS'),
       ('RB - REBOUNDING'),
       ('TRANS - TRANSITION'),
       ('PLAYER - PLAYER MANAGEMENT'),
       ('COACH - COACH MANAGEMENT'),
       ('TW - TEAMWORK'),
       ('DW - DOUBLE WHISTLE'),
       ('COM - COMMUNICATION WITH COLLEAGUES'),
       ('PTP - PROCESS THE PLAY'),
       ('PW_CW - PATIENT WHISTLE /CADENCED WHISTLE'),
       ('QW - QUICK WHISTLE'),
       ('OA_CA - OPEN ANGLE vs CLOSE ANGLE'),
       ('D&S - DISTANCE AND STATIONARY'),
       ('CLOCKS - CLOCKS CONTROL'),
       ('IRS - INSTANT REPLAY'),
       ('ATT - ATTITUDE'),
       ('FIT - FITNESS');


