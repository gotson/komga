alter table BOOK
    add column oneshot boolean NOT NULL DEFAULT false;

alter table SERIES
    add column oneshot boolean NOT NULL DEFAULT false;

ALTER TABLE LIBRARY
    add column ONESHOTS_DIRECTORY varchar NULL DEFAULT NULL;
