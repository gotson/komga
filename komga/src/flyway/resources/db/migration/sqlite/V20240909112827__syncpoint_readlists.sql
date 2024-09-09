CREATE TABLE SYNC_POINT_READLIST
(
    SYNC_POINT_ID               varchar  NOT NULL,
    READLIST_ID                 varchar  NOT NULL,
    READLIST_NAME               varchar  NOT NULL,
    READLIST_CREATED_DATE       datetime NOT NULL,
    READLIST_LAST_MODIFIED_DATE datetime NOT NULL,
    SYNCED                      boolean  NOT NULL default false,
    PRIMARY KEY (SYNC_POINT_ID, READLIST_ID),
    FOREIGN KEY (SYNC_POINT_ID) REFERENCES SYNC_POINT (ID)
);

create index if not exists idx__sync_point_readlist__sync_point_id
    on SYNC_POINT_READLIST (SYNC_POINT_ID);

CREATE TABLE SYNC_POINT_READLIST_BOOK
(
    SYNC_POINT_ID varchar NOT NULL,
    READLIST_ID   varchar NOT NULL,
    BOOK_ID       varchar NOT NULL,
    PRIMARY KEY (SYNC_POINT_ID, READLIST_ID, BOOK_ID),
    FOREIGN KEY (SYNC_POINT_ID) REFERENCES SYNC_POINT (ID)
);

create index if not exists idx__sync_point_readlist_book__sync_point_id_readlist_id
    on SYNC_POINT_READLIST_BOOK (SYNC_POINT_ID, READLIST_ID);

CREATE TABLE SYNC_POINT_READLIST_REMOVED_SYNCED
(
    SYNC_POINT_ID varchar NOT NULL,
    READLIST_ID   varchar NOT NULL,
    PRIMARY KEY (SYNC_POINT_ID, READLIST_ID),
    FOREIGN KEY (SYNC_POINT_ID) REFERENCES SYNC_POINT (ID)
);
