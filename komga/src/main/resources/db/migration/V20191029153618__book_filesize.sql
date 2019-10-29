alter table book
    add (file_size bigint default 0);

-- force rescan to update file size of all books
update series
set file_last_modified = '1970-01-01 00:00:00';
update book
set file_last_modified = '1970-01-01 00:00:00';
