-- PROVISIONAL default wait limits (minutes) for levels 2 to 5.
-- No requirement defines these values yet: they are placeholders so that
-- REQ-11 can run out of the box. They are editable (REQ-10) and must be
-- replaced by the values the business confirms, through a new migration or
-- through the application.

INSERT INTO wait_limit (level, limit_minutes) VALUES
    (2, 30),
    (3, 60),
    (4, 120),
    (5, 240);
