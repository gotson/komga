create table book_metadata
(
    id                     bigint    not null,
    created_date           timestamp not null,
    last_modified_date     timestamp not null,
    age_rating             integer,
    age_rating_lock        boolean   not null default false,
    number                 varchar   not null,
    number_lock            boolean   not null default false,
    number_sort            float4    not null,
    number_sort_lock       boolean   not null default false,
    publisher              varchar   not null default '',
    publisher_lock         boolean   not null default false,
    reading_direction      varchar,
    reading_direction_lock boolean   not null default false,
    release_date           date,
    release_date_lock      boolean   not null default false,
    summary                varchar   not null default '',
    summary_lock           boolean   not null default false,
    title                  varchar   not null,
    title_lock             boolean   not null default false,
    authors_lock           boolean   not null default false,
    primary key (id)
);

create table book_metadata_author
(
    book_metadata_id bigint  not null,
    name             varchar not null,
    role             varchar not null
);

alter table book
    add (metadata_id bigint);

alter table book_metadata_author
    add constraint fk_book_metadata_author_book_metadata_id foreign key (book_metadata_id) references book_metadata (id);

alter table book
    add constraint fk_book_book__metadata_metadata_id foreign key (metadata_id) references book_metadata (id);
