alter table user
    add (shared_all_libraries boolean not null default true);

create table user_library_sharing
(
    user_id    bigint not null,
    library_id bigint not null,
    primary key (user_id, library_id)
);

alter table user_library_sharing
    add constraint fk_user_library_sharing_library_id foreign key (library_id) references library (id);

alter table user_library_sharing
    add constraint fk_user_library_sharing_user_id foreign key (user_id) references user (id);
