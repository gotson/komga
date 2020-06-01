create table user
(
    id                 bigint    not null,
    created_date       timestamp not null,
    last_modified_date timestamp not null,
    email              varchar   not null,
    password           varchar   not null,
    primary key (id)
);

alter table user
    add constraint uk_user_login unique (email);

create table user_role
(
    user_id bigint not null,
    roles   varchar
);

alter table user_role
    add constraint fk_user_role_user_user_id foreign key (user_id) references user (id);
