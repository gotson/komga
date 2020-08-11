-- This is a multi-steps migration, mixing 2 SQL migrations and a Java migration in-between
CREATE TABLE THUMBNAIL_BOOK
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    THUMBNAIL          blob     NULL,
    URL                varchar  NULL,
    SELECTED           boolean  NOT NULL DEFAULT 0,
    TYPE               varchar  NOT NULL,
    CREATED_DATE       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    BOOK_ID            varchar  NOT NULL,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);
