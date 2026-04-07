-- This is a multi-steps migration, mixing 2 SQL migrations and a Java migration in-between
CREATE TABLE THUMBNAIL_BOOK
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    THUMBNAIL          bytea     NULL,
    URL                varchar  NULL,
    SELECTED           boolean  NOT NULL DEFAULT false,
    TYPE               varchar  NOT NULL,
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    BOOK_ID            varchar  NOT NULL,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);
