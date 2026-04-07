UPDATE BOOK_METADATA_TAG
SET tag = lower(trim(tag));

delete
from BOOK_METADATA_TAG
where ROWID not in
      (
          select min(rowid)
          from BOOK_METADATA_TAG
          group by tag, BOOK_ID
      );


UPDATE SERIES_METADATA_TAG
SET tag = lower(trim(tag));

delete
from SERIES_METADATA_TAG
where ROWID not in
      (
          select min(rowid)
          from SERIES_METADATA_TAG
          group by tag, SERIES_ID
      );


UPDATE SERIES_METADATA_GENRE
SET GENRE = lower(trim(GENRE));

delete
from SERIES_METADATA_GENRE
where ROWID not in
      (
          select min(rowid)
          from SERIES_METADATA_GENRE
          group by GENRE, SERIES_ID
      );
