create table library
(
    id                 bigint    not null,
    created_date       timestamp not null,
    last_modified_date timestamp not null,
    name               varchar   not null,
    root               varchar   not null,
    primary key (id)
);

alter table library
    add constraint uk_library_name unique (name);

alter table serie
    add (library_id bigint);

alter table serie
    add constraint fk_serie_library_library_id foreign key (library_id) references library (id);