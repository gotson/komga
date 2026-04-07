update media
set STATUS = 'OUTDATED'
where MEDIA_TYPE = 'application/epub+zip';

update media
set STATUS = 'OUTDATED'
where BOOK_ID in (select ID
                  from BOOK
                  where URL like '%.epub' collate NOCASE);
