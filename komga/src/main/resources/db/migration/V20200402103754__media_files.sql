create table media_file
(
    media_id bigint not null,
    files    varchar
);

alter table media_file
    add constraint fk_media_file_media_media_id foreign key (media_id) references media(id);
