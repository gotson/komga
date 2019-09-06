update BOOK_METADATA
set STATUS = 'UNKNOWN'
where ID in (
    select m.id
    from BOOK_METADATA m,
         BOOK_METADATA_PAGE p
    where m.ID = p.BOOK_METADATA_ID
      and m.THUMBNAIL is null
      and p.NUMBER = 0
      and p.MEDIA_TYPE = 'image/webp');