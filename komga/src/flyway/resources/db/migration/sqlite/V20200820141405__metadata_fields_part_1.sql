alter table series_metadata
    add column PUBLISHER varchar NOT NULL DEFAULT '';
alter table series_metadata
    add column PUBLISHER_LOCK boolean NOT NULL DEFAULT 0;

alter table series_metadata
    add column READING_DIRECTION varchar NULL;
alter table series_metadata
    add column READING_DIRECTION_LOCK boolean NOT NULL DEFAULT 0;

alter table series_metadata
    add column AGE_RATING int NULL;
alter table series_metadata
    add column AGE_RATING_LOCK boolean NOT NULL DEFAULT 0;

alter table SERIES_METADATA
    add column SUMMARY varchar NOT NULL DEFAULT '';
alter table SERIES_METADATA
    add column SUMMARY_LOCK boolean NOT NULL DEFAULT 0;

alter table SERIES_METADATA
    add column LANGUAGE varchar NOT NULL DEFAULT '';
alter table SERIES_METADATA
    add column LANGUAGE_LOCK boolean NOT NULL DEFAULT 0;

alter table SERIES_METADATA
    add column GENRES_LOCK boolean NOT NULL DEFAULT 0;

alter table SERIES_METADATA
    add column TAGS_LOCK boolean NOT NULL DEFAULT 0;


CREATE TABLE SERIES_METADATA_GENRE
(
    GENRE     varchar NOT NULL,
    SERIES_ID varchar NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

CREATE TABLE SERIES_METADATA_TAG
(
    TAG       varchar NOT NULL,
    SERIES_ID varchar NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

CREATE TABLE BOOK_METADATA_TAG
(
    TAG     varchar NOT NULL,
    BOOK_ID varchar NOT NULL,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);
