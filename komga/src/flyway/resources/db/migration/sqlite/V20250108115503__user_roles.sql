CREATE TABLE USER_ROLE
(
    USER_ID varchar NOT NULL,
    ROLE    varchar NOT NULL,
    PRIMARY KEY (USER_ID, ROLE),
    FOREIGN KEY (USER_ID) REFERENCES USER (ID)
);

insert into USER_ROLE
select id, "ADMIN"
from user
where ROLE_ADMIN = 1;

insert into USER_ROLE
select id, "KOREADER_SYNC"
from user
where ROLE_ADMIN = 1;

insert into USER_ROLE
select id, "FILE_DOWNLOAD"
from user
where ROLE_FILE_DOWNLOAD = 1;

insert into USER_ROLE
select id, "PAGE_STREAMING"
from user
where ROLE_PAGE_STREAMING = 1;

insert into USER_ROLE
select id, "KOBO_SYNC"
from user
where ROLE_KOBO_SYNC = 1;

-- Remove columns ROLE_ADMIN, ROLE_FILE_DOWNLOAD, ROLE_PAGE_STREAMING, ROLE_KOBO_SYNC from USER
PRAGMA foreign_keys= OFF;

create table USER_dg_tmp
(
    ID                         varchar                            not null
        primary key,
    CREATED_DATE               datetime default CURRENT_TIMESTAMP not null,
    LAST_MODIFIED_DATE         datetime default CURRENT_TIMESTAMP not null,
    EMAIL                      varchar                            not null
        unique,
    PASSWORD                   varchar                            not null,
    SHARED_ALL_LIBRARIES       boolean  default 1                 not null,
    AGE_RESTRICTION            integer,
    AGE_RESTRICTION_ALLOW_ONLY boolean
);

insert into USER_dg_tmp(ID, CREATED_DATE, LAST_MODIFIED_DATE, EMAIL, PASSWORD, SHARED_ALL_LIBRARIES, AGE_RESTRICTION,
                        AGE_RESTRICTION_ALLOW_ONLY)
select ID,
       CREATED_DATE,
       LAST_MODIFIED_DATE,
       EMAIL,
       PASSWORD,
       SHARED_ALL_LIBRARIES,
       AGE_RESTRICTION,
       AGE_RESTRICTION_ALLOW_ONLY
from USER;

drop table USER;

alter table USER_dg_tmp
    rename to USER;

PRAGMA foreign_keys= ON;
