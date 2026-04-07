delete from PAGE_HASH;
delete from PAGE_HASH_THUMBNAIL;
update BOOK set FILE_HASH = '';
update MEDIA_PAGE set FILE_HASH = '';
