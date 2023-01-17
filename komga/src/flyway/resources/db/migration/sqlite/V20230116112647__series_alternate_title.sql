CREATE TABLE SERIES_METADATA_ALTERNATE_TITLE
(
    LABEL     varchar NOT NULL,
    TITLE     varchar NOT NULL,
    SERIES_ID varchar NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

alter table series_metadata
    add column ALTERNATE_TITLES_LOCK boolean NOT NULL DEFAULT 0;

create index idx__series_metadata_alternate_title__series_id
    on SERIES_METADATA_ALTERNATE_TITLE (SERIES_ID);
