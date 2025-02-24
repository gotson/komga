CREATE TABLE ANNOTATION
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    CREATED_DATE       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    BOOK_ID            varchar  NOT NULL,
    USER_ID            varchar  NOT NULL,
    COLOR              varchar,
    NOTE               varchar,
    START_SPAN         varchar  NOT NULL,
    END_SPAN           varchar  NOT NULL,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID),
    FOREIGN KEY (USER_ID) REFERENCES USER (ID)
);


create index if not exists idx__annotation__book_id_user_id
    on ANNOTATION (BOOK_ID, USER_ID);
