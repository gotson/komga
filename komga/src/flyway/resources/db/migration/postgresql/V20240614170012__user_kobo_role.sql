alter table "USER"
    add column ROLE_KOBO_SYNC boolean NOT NULL DEFAULT false;

update "USER"
set ROLE_KOBO_SYNC = true
where ROLE_ADMIN = true;
