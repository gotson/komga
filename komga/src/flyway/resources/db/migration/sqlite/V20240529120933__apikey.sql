CREATE TABLE USER_API_KEY
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    USER_ID            varchar  NOT NULL,
    CREATED_DATE       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    API_KEY            varchar  NOT NULL UNIQUE,
    COMMENT            varchar  NOT NULL,
    FOREIGN KEY (USER_ID) REFERENCES USER (ID)
);

create index if not exists idx__user_api_key__user_id
    on USER_API_KEY (USER_ID);
