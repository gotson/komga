alter table library
    add column IMPORT_BARCODE_ISBN boolean NOT NULL DEFAULT 1;

alter table book_metadata
    add column ISBN varchar NOT NULL DEFAULT '';
alter table book_metadata
    add column ISBN_LOCK boolean NOT NULL DEFAULT 0;
