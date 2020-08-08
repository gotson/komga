create table collection
(
    id                 bigint    not null,
    name               varchar   not null,
    ordered            boolean   not null default false,
    series_count       int       not null,
    created_date       timestamp not null default now(),
    last_modified_date timestamp not null default now(),
    primary key (id)
);

create table collection_series
(
    collection_id bigint  not null,
    series_id     bigint  not null,
    number        integer not null
);

alter table collection_series
    add constraint fk_collection_series_collection_collection_id foreign key (collection_id) references collection (id);

alter table collection_series
    add constraint fk_collection_series_series_series_id foreign key (series_id) references series (id);
