CREATE TABLE SYNC_POINT
(
    ID           varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    USER_ID      varchar  NOT NULL,
    API_KEY_ID   varchar  NULL,
    FOREIGN KEY (USER_ID) REFERENCES USER (ID)
);

create index if not exists idx__sync_point__user_id
    on SYNC_POINT (USER_ID);

CREATE TABLE SYNC_POINT_BOOK_REMOVED_SYNCED
(
    SYNC_POINT_ID varchar NOT NULL,
    BOOK_ID       varchar NOT NULL,
    PRIMARY KEY (SYNC_POINT_ID, BOOK_ID),
    FOREIGN KEY (SYNC_POINT_ID) REFERENCES SYNC_POINT (ID)
);

create index if not exists idx__sync_point_book_removed_status__sync_point_id
    on SYNC_POINT_BOOK_REMOVED_SYNCED (SYNC_POINT_ID);

CREATE TABLE SYNC_POINT_BOOK
(
    SYNC_POINT_ID                         varchar  NOT NULL,
    BOOK_ID                               varchar  NOT NULL,
    BOOK_CREATED_DATE                     datetime NOT NULL,
    BOOK_LAST_MODIFIED_DATE               datetime NOT NULL,
    BOOK_FILE_LAST_MODIFIED               datetime NOT NULL,
    BOOK_FILE_SIZE                        int8     NOT NULL,
    BOOK_FILE_HASH                        varchar  NOT NULL,
    BOOK_METADATA_LAST_MODIFIED_DATE      datetime NOT NULL,
    BOOK_READ_PROGRESS_LAST_MODIFIED_DATE datetime NULL,
    SYNCED                                boolean  NOT NULL default false,
    PRIMARY KEY (SYNC_POINT_ID, BOOK_ID),
    FOREIGN KEY (SYNC_POINT_ID) REFERENCES SYNC_POINT (ID)
);

create index if not exists idx__sync_point_book__sync_point_id
    on SYNC_POINT_BOOK (SYNC_POINT_ID);
