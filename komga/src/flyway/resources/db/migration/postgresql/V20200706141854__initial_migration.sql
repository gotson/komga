CREATE TABLE LIBRARY
(
    ID                          varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE                timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    NAME                        varchar  NOT NULL,
    ROOT                        varchar  NOT NULL,
    IMPORT_COMICINFO_BOOK       boolean  NOT NULL DEFAULT true,
    IMPORT_COMICINFO_SERIES     boolean  NOT NULL DEFAULT true,
    IMPORT_COMICINFO_COLLECTION boolean  NOT NULL DEFAULT true,
    IMPORT_EPUB_BOOK            boolean  NOT NULL DEFAULT true,
    IMPORT_EPUB_SERIES          boolean  NOT NULL DEFAULT true
);

CREATE TABLE "USER"
(
    ID                   varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE         timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE   timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    EMAIL                varchar  NOT NULL UNIQUE,
    PASSWORD             varchar  NOT NULL,
    SHARED_ALL_LIBRARIES boolean  NOT NULL DEFAULT true,
    ROLE_ADMIN           boolean  NOT NULL DEFAULT false,
    ROLE_FILE_DOWNLOAD   boolean  NOT NULL DEFAULT true,
    ROLE_PAGE_STREAMING  boolean  NOT NULL DEFAULT true
);

CREATE TABLE USER_LIBRARY_SHARING
(
    USER_ID    varchar NOT NULL,
    LIBRARY_ID varchar NOT NULL,
    PRIMARY KEY (USER_ID, LIBRARY_ID),
    FOREIGN KEY (USER_ID) REFERENCES "USER" (ID),
    FOREIGN KEY (LIBRARY_ID) REFERENCES LIBRARY (ID)
);

CREATE TABLE SERIES
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FILE_LAST_MODIFIED timestamp NOT NULL,
    NAME               varchar  NOT NULL,
    URL                varchar  NOT NULL,
    LIBRARY_ID         varchar  NOT NULL,
    FOREIGN KEY (LIBRARY_ID) REFERENCES LIBRARY (ID)
);

CREATE TABLE SERIES_METADATA
(
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    STATUS             varchar  NOT NULL,
    STATUS_LOCK        boolean  NOT NULL DEFAULT false,
    TITLE              varchar  NOT NULL,
    TITLE_LOCK         boolean  NOT NULL DEFAULT false,
    TITLE_SORT         varchar  NOT NULL,
    TITLE_SORT_LOCK    boolean  NOT NULL DEFAULT false,
    SUMMARY            text,
    SUMMARY_LOCK       boolean  NOT NULL DEFAULT false,
    PUBLISHER          varchar,
    PUBLISHER_LOCK     boolean  NOT NULL DEFAULT false,
    READING_DIRECTION  varchar,
    READING_DIRECTION_LOCK boolean NOT NULL DEFAULT false,
    AGE_RATING         integer,
    AGE_RATING_LOCK    boolean  NOT NULL DEFAULT false,
    LANGUAGE           varchar,
    LANGUAGE_LOCK      boolean  NOT NULL DEFAULT false,
    SERIES_ID          varchar  NOT NULL PRIMARY KEY,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

CREATE TABLE BOOK
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FILE_LAST_MODIFIED timestamp NOT NULL,
    NAME               varchar  NOT NULL,
    URL                varchar  NOT NULL,
    FILE_SIZE          bigint   NOT NULL,
    NUMBER             integer,
    SERIES_ID          varchar  NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

CREATE TABLE BOOK_METADATA
(
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    TITLE              varchar  NOT NULL,
    TITLE_LOCK         boolean  NOT NULL DEFAULT false,
    TITLE_SORT         varchar  NOT NULL,
    TITLE_SORT_LOCK    boolean  NOT NULL DEFAULT false,
    SUMMARY            text,
    SUMMARY_LOCK       boolean  NOT NULL DEFAULT false,
    NUMBER             varchar,
    NUMBER_LOCK        boolean  NOT NULL DEFAULT false,
    NUMBER_SORT        double precision,
    NUMBER_SORT_LOCK   boolean  NOT NULL DEFAULT false,
    RELEASE_DATE       date,
    RELEASE_DATE_LOCK  boolean  NOT NULL DEFAULT false,
    AUTHORS            text,
    AUTHORS_LOCK       boolean  NOT NULL DEFAULT false,
    BOOK_ID            varchar  NOT NULL PRIMARY KEY,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);

CREATE TABLE READ_PROGRESS
(
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PAGE               integer  NOT NULL,
    COMPLETED          boolean  NOT NULL DEFAULT false,
    USER_ID            varchar  NOT NULL,
    BOOK_ID            varchar  NOT NULL,
    PRIMARY KEY (USER_ID, BOOK_ID),
    FOREIGN KEY (USER_ID) REFERENCES "USER" (ID),
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);

CREATE TABLE READLIST
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    NAME               varchar  NOT NULL,
    SUMMARY            text,
    FILTERED           boolean  NOT NULL DEFAULT false,
    USER_ID            varchar  NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES "USER" (ID)
);

CREATE TABLE READLIST_BOOKS
(
    READLIST_ID varchar NOT NULL,
    BOOK_ID     varchar NOT NULL,
    NUMBER      integer NOT NULL,
    PRIMARY KEY (READLIST_ID, BOOK_ID),
    FOREIGN KEY (READLIST_ID) REFERENCES READLIST (ID),
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);

CREATE TABLE COLLECTION
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    NAME               varchar  NOT NULL,
    SUMMARY            text,
    FILTERED           boolean  NOT NULL DEFAULT false,
    USER_ID            varchar  NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES "USER" (ID)
);

CREATE TABLE COLLECTION_SERIES
(
    COLLECTION_ID varchar NOT NULL,
    SERIES_ID     varchar NOT NULL,
    NUMBER        integer NOT NULL,
    PRIMARY KEY (COLLECTION_ID, SERIES_ID),
    FOREIGN KEY (COLLECTION_ID) REFERENCES COLLECTION (ID),
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);