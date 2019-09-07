update BOOK_METADATA
set STATUS = 'UNKNOWN'
where ID in (
    SELECT ID FROM BOOK_METADATA where MEDIA_TYPE = 'application/pdf'
    );