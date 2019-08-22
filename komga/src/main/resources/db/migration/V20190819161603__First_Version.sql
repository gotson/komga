create sequence hibernate_sequence start with 1 increment by 1;

create table book
(
    id                 bigint    not null,
    created_date       timestamp not null,
    last_modified_date timestamp not null,
    file_last_modified timestamp not null,
    name               varchar   not null,
    url                varchar   not null,
    book_metadata_id   bigint    not null,
    serie_id           bigint    not null,
    index              integer,
    primary key (id)
);

create table book_metadata
(
    id         bigint  not null,
    media_type varchar,
    status     varchar not null,
    thumbnail  blob,
    primary key (id)
);

create table book_metadata_page
(
    book_metadata_id bigint  not null,
    file_name        varchar not null,
    media_type       varchar not null,
    number           integer
);

create table serie
(
    id                 bigint    not null,
    created_date       timestamp not null,
    last_modified_date timestamp not null,
    file_last_modified timestamp not null,
    name               varchar   not null,
    url                varchar   not null,
    primary key (id)
);

alter table book
    add constraint uk_book_book_metadata_id unique (book_metadata_id);

alter table book
    add constraint fk_book_book_metadata_book_metadata_id foreign key (book_metadata_id) references book_metadata (id);

alter table book
    add constraint fk_book_serie_serie_id foreign key (serie_id) references serie (id);

alter table book_metadata_page
    add constraint fk_book_metadata_page_book_metadata_book_metadata_id foreign key (book_metadata_id) references book_metadata (id);
