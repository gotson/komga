alter table READ_PROGRESS_SERIES
    add MOST_RECENT_READ_DATE datetime NULL;
alter table READ_PROGRESS_SERIES
    add LAST_MODIFIED_DATE datetime NULL;

update READ_PROGRESS_SERIES
set MOST_RECENT_READ_DATE = (select max(r.READ_DATE)
                                                  from READ_PROGRESS r
                                                           inner join BOOK b on r.BOOK_ID = b.ID
                                                  where b.SERIES_ID = READ_PROGRESS_SERIES.SERIES_ID);

update READ_PROGRESS_SERIES
set LAST_MODIFIED_DATE = (select max(r.LAST_MODIFIED_DATE)
                                               from READ_PROGRESS r
                                                        inner join BOOK b on r.BOOK_ID = b.ID
                                               where b.SERIES_ID = READ_PROGRESS_SERIES.SERIES_ID);
