CREATE TABLE LIBRARY_EXCLUSIONS
(
    LIBRARY_ID varchar NOT NULL,
    EXCLUSION  varchar NOT NULL,
    PRIMARY KEY (LIBRARY_ID, EXCLUSION),
    FOREIGN KEY (LIBRARY_ID) REFERENCES LIBRARY (ID)
);

CREATE INDEX idx__library_exclusions__library_id on LIBRARY_EXCLUSIONS (LIBRARY_ID);

INSERT INTO LIBRARY_EXCLUSIONS
WITH cte_exclusions(exclude) AS (VALUES ('#recycle'),
                                        ('@eaDir'),
                                        ('@Recycle'))
SELECT LIBRARY.ID, cte_exclusions.exclude
FROM LIBRARY
         cross join cte_exclusions;
