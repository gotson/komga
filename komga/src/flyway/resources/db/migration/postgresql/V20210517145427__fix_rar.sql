-- fix media type for files analyzed with tika 1.23 or before which didn't get the rar version
update MEDIA
set MEDIA_TYPE = 'application/x-rar-compressed; version=4'
where MEDIA_TYPE = 'application/x-rar-compressed'
  and STATUS = 'READY';

-- rar files that could have had an incorrect analysis are marked at OUTDATED to be ra-analyzed
update MEDIA
set STATUS = 'OUTDATED'
where BOOK_ID in (
    select F.BOOK_ID
    from MEDIA_FILE F
             left join MEDIA M on F.BOOK_ID = M.BOOK_ID
    where F.FILE_NAME like '%.%'
      and M.MEDIA_TYPE like 'application/x-rar-compressed%'
      and lower(replace(F.FILE_NAME, rtrim(F.FILE_NAME, replace(F.FILE_NAME, '.', '')), ''))
        in ('jpg', 'jpeg', 'webp', 'tiff', 'tif', 'gif', 'png', 'bmp')
    );
