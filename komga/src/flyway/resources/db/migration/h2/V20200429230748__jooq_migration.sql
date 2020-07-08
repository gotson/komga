-- set default values for created_date / last_modified_date
alter table user
    alter column CREATED_DATE set default now();
alter table user
    alter column LAST_MODIFIED_DATE set default now();

alter table library
    alter column CREATED_DATE set default now();
alter table library
    alter column LAST_MODIFIED_DATE set default now();

alter table book
    alter column CREATED_DATE set default now();
alter table book
    alter column LAST_MODIFIED_DATE set default now();

alter table book_metadata
    alter column CREATED_DATE set default now();
alter table book_metadata
    alter column LAST_MODIFIED_DATE set default now();

alter table media
    alter column CREATED_DATE set default now();
alter table media
    alter column LAST_MODIFIED_DATE set default now();

alter table series
    alter column CREATED_DATE set default now();
alter table series
    alter column LAST_MODIFIED_DATE set default now();

alter table series_metadata
    alter column CREATED_DATE set default now();
alter table series_metadata
    alter column LAST_MODIFIED_DATE set default now();

-- replace USER_ROLE table by boolean value per role in USER table
alter table user
    add role_admin boolean default false;
update user u
set role_admin = exists(select roles from user_role ur where ur.roles like 'ADMIN' and ur.user_id = u.id);
drop table user_role;

-- add LIBRARY_ID field to table BOOK
alter table book
    add library_id bigint;
alter table book
    add constraint fk_book_library_library_id foreign key (library_id) references library (id);
update book b
set library_id = (select s.library_id from series s where s.ID = b.series_id);
alter table book
    alter column library_id set not null;

-- inverse relationship between series and series_metadata
alter table SERIES_METADATA
    add column series_id bigint;
update SERIES_METADATA m
set m.series_id = (select s.id from series s where s.metadata_id = m.id);
alter table SERIES
    drop constraint FK_SERIES_SERIES_METADATA_METADATA_ID;
alter table SERIES_METADATA
    drop primary key;
alter table SERIES_METADATA
    drop column id;
alter table SERIES_METADATA
    alter column series_id set not null;
alter table SERIES_METADATA
    add primary key (series_id);
alter table series
    drop column METADATA_ID;

alter table SERIES_METADATA
    add constraint FK_SERIES_METADATA_SERIES_SERIES_ID foreign key (series_id) references series (id);

-- inverse relationship between book and book_metadata
alter table BOOK_METADATA
    add column book_id bigint;
update BOOK_METADATA m
set m.book_id = (select b.id from book b where b.metadata_id = m.id);

alter table BOOK_METADATA_AUTHOR
    add column book_id bigint;
update BOOK_METADATA_AUTHOR a
set a.book_id = (select m.book_id from BOOK_METADATA m where m.id = a.BOOK_METADATA_ID);

alter table BOOK
    drop constraint FK_BOOK_BOOK__METADATA_METADATA_ID;
alter table BOOK_METADATA_AUTHOR
    drop constraint FK_BOOK_METADATA_AUTHOR_BOOK_METADATA_ID;

alter table BOOK_METADATA
    drop primary key;
alter table BOOK_METADATA
    drop column id;
alter table BOOK_METADATA
    alter column book_id set not null;
alter table BOOK_METADATA
    add primary key (book_id);

alter table BOOK_METADATA_AUTHOR
    drop column BOOK_METADATA_ID;
alter table BOOK_METADATA_AUTHOR
    alter column book_id set not null;

alter table BOOK
    drop column METADATA_ID;

alter table BOOK_METADATA
    add constraint FK_BOOK_METADATA_BOOK_BOOK_ID foreign key (book_id) references book (id);
alter table BOOK_METADATA_AUTHOR
    add constraint FK_BOOK_METADATA_AUTHOR_BOOK_BOOK_ID foreign key (book_id) references book (id);

-- inverse relationship between book and media
alter table MEDIA
    add column book_id bigint;
update MEDIA m
set m.book_id = (select b.id from book b where b.MEDIA_ID = m.id);

alter table MEDIA_PAGE
    add column book_id bigint;
update MEDIA_PAGE p
set p.book_id = (select m.book_id from MEDIA m where m.id = p.MEDIA_ID);

alter table MEDIA_FILE
    add column book_id bigint;
update MEDIA_FILE f
set f.book_id = (select m.book_id from MEDIA m where m.id = f.MEDIA_ID);

alter table BOOK
    drop constraint FK_BOOK_MEDIA_MEDIA_ID;
alter table MEDIA_PAGE
    drop constraint FK_MEDIA_PAGE_MEDIA_MEDIA_ID;
alter table MEDIA_FILE
    drop constraint FK_MEDIA_FILE_MEDIA_MEDIA_ID;

alter table MEDIA
    drop primary key;
alter table MEDIA
    drop column id;
alter table MEDIA
    alter column book_id set not null;
alter table MEDIA
    add primary key (book_id);

alter table MEDIA_PAGE
    drop column MEDIA_ID;
alter table MEDIA_PAGE
    alter column book_id set not null;

alter table MEDIA_FILE
    drop column MEDIA_ID;
alter table MEDIA_FILE
    alter column book_id set not null;
alter table MEDIA_FILE
    alter column FILES rename to FILE_NAME;

alter table BOOK
    drop column MEDIA_ID;

alter table MEDIA
    add constraint FK_MEDIA_BOOK_BOOK_ID foreign key (book_id) references book (id);
alter table MEDIA_PAGE
    add constraint FK_MEDIA_PAGE_BOOK_BOOK_ID foreign key (book_id) references book (id);
alter table MEDIA_FILE
    add constraint FK_MEDIA_FILE_BOOK_BOOK_ID foreign key (book_id) references book (id);

-- store media page count in DB
alter table media
    add column page_count bigint default 0;

update media m
set page_count = (select count(p.BOOK_ID) from media_page p where p.BOOK_ID = m.BOOK_ID);
