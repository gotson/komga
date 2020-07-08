create table series_metadata
(
    id                 bigint    not null,
    created_date       timestamp not null,
    last_modified_date timestamp not null,
    status             varchar   not null,
    primary key (id)
);

alter table series
    add (metadata_id bigint);

alter table series
    add constraint fk_series_series_metadata_metadata_id foreign key (metadata_id) references series_metadata (id);

