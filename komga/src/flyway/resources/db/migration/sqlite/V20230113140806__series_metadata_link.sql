CREATE TABLE SERIES_METADATA_LINK
(
    LABEL     varchar NOT NULL,
    URL       varchar NOT NULL,
    SERIES_ID varchar NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

alter table series_metadata
    add column LINKS_LOCK boolean NOT NULL DEFAULT 0;

create index idx__series_metadata_link__series_id
    on SERIES_METADATA_LINK (SERIES_ID);
