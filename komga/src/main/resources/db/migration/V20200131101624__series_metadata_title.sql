alter table series_metadata
    add (
        status_lock boolean default false,
        title varchar,
        title_lock boolean default false,
        title_sort varchar,
        title_sort_lock boolean default false
        );

update series_metadata m
set m.title      = (select name from series where metadata_id = m.id),
    m.title_sort = (select name from series where metadata_id = m.id);

alter table series_metadata
    alter column title set not null;

alter table series_metadata
    alter column title_sort set not null;
