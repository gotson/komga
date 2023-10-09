ALTER TABLE main.THUMBNAIL_BOOK
    add column WIDTH int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_BOOK
    add column HEIGHT int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_BOOK
    add column MEDIA_TYPE varchar NOT NULL default '';
ALTER TABLE main.THUMBNAIL_BOOK
    add column FILE_SIZE int8 NOT NULL default 0;

ALTER TABLE main.THUMBNAIL_SERIES
    add column WIDTH int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_SERIES
    add column HEIGHT int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_SERIES
    add column MEDIA_TYPE varchar NOT NULL default '';
ALTER TABLE main.THUMBNAIL_SERIES
    add column FILE_SIZE int8 NOT NULL default 0;

ALTER TABLE main.THUMBNAIL_COLLECTION
    add column WIDTH int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_COLLECTION
    add column HEIGHT int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_COLLECTION
    add column MEDIA_TYPE varchar NOT NULL default '';
ALTER TABLE main.THUMBNAIL_COLLECTION
    add column FILE_SIZE int8 NOT NULL default 0;

ALTER TABLE main.THUMBNAIL_READLIST
    add column WIDTH int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_READLIST
    add column HEIGHT int NOT NULL default 0;
ALTER TABLE main.THUMBNAIL_READLIST
    add column MEDIA_TYPE varchar NOT NULL default '';
ALTER TABLE main.THUMBNAIL_READLIST
    add column FILE_SIZE int8 NOT NULL default 0;

