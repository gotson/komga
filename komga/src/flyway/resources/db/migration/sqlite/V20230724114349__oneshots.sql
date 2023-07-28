alter table BOOK
    add column oneshot boolean NOT NULL DEFAULT 0;

alter table SERIES
    add column oneshot boolean NOT NULL DEFAULT 0;

ALTER TABLE LIBRARY
    add column ONESHOTS_DIRECTORY varchar NULL DEFAULT NULL;
