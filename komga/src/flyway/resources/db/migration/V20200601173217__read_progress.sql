create table read_progress
(
    book_id            bigint    not null,
    user_id            bigint    not null,
    created_date       timestamp not null default now(),
    last_modified_date timestamp not null default now(),
    page               integer   not null,
    completed          boolean   not null
);

alter table read_progress
    add constraint fk_read_progress_book_book_id foreign key (book_id) references book (id);

alter table read_progress
    add constraint fk_read_progress_user_user_id foreign key (user_id) references user (id);
