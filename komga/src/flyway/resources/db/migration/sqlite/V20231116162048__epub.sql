alter table MEDIA_FILE
    add column MEDIA_TYPE varchar NULL;
alter table MEDIA_FILE
    add column SUB_TYPE varchar NULL;
alter table MEDIA_FILE
    add column FILE_SIZE int8 NULL;

alter table MEDIA
    add column EXTENSION_CLASS varchar NULL;
alter table MEDIA
    add column EXTENSION_VALUE varchar NULL;

update media
set STATUS = 'OUTDATED'
where MEDIA_TYPE = 'application/epub+zip'
  and STATUS = 'READY';
