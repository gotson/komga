alter table series_metadata
    add column TOTAL_BOOK_COUNT int NULL;
alter table series_metadata
    add column TOTAL_BOOK_COUNT_LOCK boolean NOT NULL DEFAULT 0;
