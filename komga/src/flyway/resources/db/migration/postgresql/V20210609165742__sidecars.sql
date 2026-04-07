CREATE TABLE SIDECAR
(
    URL                varchar  NOT NULL PRIMARY KEY,
    PARENT_URL         varchar  NOT NULL,
    LAST_MODIFIED_TIME timestamp NOT NULL,
    LIBRARY_ID         varchar  NOT NULL
);
