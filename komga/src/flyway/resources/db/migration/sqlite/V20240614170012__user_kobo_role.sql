alter table USER
    add column ROLE_KOBO_SYNC boolean NOT NULL DEFAULT 0;

update USER
set ROLE_KOBO_SYNC = 1
where ROLE_ADMIN = 1;
