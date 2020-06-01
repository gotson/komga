alter table book_metadata
    rename to media;

alter table book
    alter column book_metadata_id
        rename to media_id;

alter table book
    rename constraint uk_book_book_metadata_id to uk_book_media_id;

alter table book
    rename constraint fk_book_book_metadata_book_metadata_id to fk_book_media_media_id;

alter table book_metadata_page
    rename to media_page;

alter table media_page
    alter column book_metadata_id
        rename to media_id;

alter table media_page
    rename constraint fk_book_metadata_page_book_metadata_book_metadata_id to fk_media_page_media_media_id;

alter index if exists uk_book_book_metadata_id_index_7 rename to uk_book_media_id_index_7;
alter index if exists fk_book_metadata_page_book_metadata_book_metadata_id_index_9 rename to fk_media_page_media_media_id_index_9;
