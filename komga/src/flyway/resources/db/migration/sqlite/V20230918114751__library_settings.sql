ALTER TABLE LIBRARY
    RENAME COLUMN _UNUSED to SCAN_STARTUP;
UPDATE LIBRARY
SET SCAN_STARTUP = ${library-scan-startup};
ALTER TABLE LIBRARY
    add column SCAN_CBX boolean NOT NULL DEFAULT 1;
ALTER TABLE LIBRARY
    add column SCAN_PDF boolean NOT NULL DEFAULT 1;
ALTER TABLE LIBRARY
    add column SCAN_EPUB boolean NOT NULL DEFAULT 1;
ALTER TABLE LIBRARY
    add column SCAN_INTERVAL varchar NOT NULL DEFAULT 'EVERY_6H';
