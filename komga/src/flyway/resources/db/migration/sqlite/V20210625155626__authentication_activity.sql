create table AUTHENTICATION_ACTIVITY
(
    USER_ID    varchar  NULL     DEFAULT NULL,
    EMAIL      varchar  NULL     DEFAULT NULL,
    IP         varchar  NULL     DEFAULT NULL,
    USER_AGENT varchar  NULL     DEFAULT NULL,
    SUCCESS    boolean  NOT NULL,
    ERROR      varchar  NULL     DEFAULT NULL,
    DATE_TIME  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (USER_ID) references USER (ID)
);
