update MEDIA
set EXTENSION_VALUE = null;

update media
set STATUS = 'OUTDATED'
where MEDIA_TYPE = 'application/epub+zip'
  and STATUS = 'READY';

ALTER TABLE MEDIA
    RENAME COLUMN EXTENSION_VALUE to _UNUSED;

alter table MEDIA
    add column EXTENSION_VALUE_BLOB blob NULL;
