CREATE TABLE TASK
(
    ID                 varchar  NOT NULL PRIMARY KEY,
    PRIORITY           int      NOT NULL,
    GROUP_ID           varchar  NULL,
    CLASS              varchar  NOT NULL,
    SIMPLE_TYPE        varchar  NOT NULL,
    PAYLOAD            varchar  NOT NULL,
    OWNER              varchar  NULL,
    CREATED_DATE       datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE datetime NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create index idx__tasks__owner_group_id
    on TASK (OWNER, GROUP_ID);

