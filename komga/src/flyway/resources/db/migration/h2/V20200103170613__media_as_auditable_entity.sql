alter table media
    add (created_date timestamp);
alter table media
    add (last_modified_date timestamp);

update media
set created_date       = CURRENT_TIMESTAMP(),
    last_modified_date = CURRENT_TIMESTAMP();

alter table media
    alter column created_date set not null;
alter table media
    alter column last_modified_date set not null;
