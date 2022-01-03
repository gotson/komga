CREATE TABLE BOOK_METADATA_LINK
(
    LABEL   varchar NOT NULL,
    URL     varchar NOT NULL,
    BOOK_ID varchar NOT NULL,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);

alter table book_metadata
    add column LINKS_LOCK boolean NOT NULL DEFAULT 0;
