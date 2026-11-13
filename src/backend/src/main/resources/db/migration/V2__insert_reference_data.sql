-- Reference data required for the module to work: the five triage levels and
-- one queue for each level 2 to 5 (BR-TRG-03). Level 1 has no queue.

INSERT INTO triage_level (level, name) VALUES
    (1, 'LEVEL_1'),
    (2, 'LEVEL_2'),
    (3, 'LEVEL_3'),
    (4, 'LEVEL_4'),
    (5, 'LEVEL_5');

INSERT INTO level_queue (level) VALUES
    (2),
    (3),
    (4),
    (5);
