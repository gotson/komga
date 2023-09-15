update media
set STATUS = 'OUTDATED'
WHERE BOOK_ID in
      (select distinct M.BOOK_ID
       from MEDIA_PAGE P
                inner join MEDIA M on P.BOOK_ID = M.BOOK_ID
       where M.MEDIA_TYPE in ('application/zip', 'application/x-rar-compressed; version=4')
         and P.FILE_SIZE is null);
