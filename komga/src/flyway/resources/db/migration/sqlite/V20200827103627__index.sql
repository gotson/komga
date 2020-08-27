CREATE INDEX idx__book__series_id on BOOK (SERIES_ID);
CREATE INDEX idx__book__library_id on BOOK (LIBRARY_ID);

CREATE INDEX idx__book_metadata_author__book_id on BOOK_METADATA_AUTHOR (BOOK_ID);
CREATE INDEX idx__book_metadata_tag__book_id on BOOK_METADATA_TAG (BOOK_ID);

CREATE INDEX idx__media_file__book_id on MEDIA_FILE (BOOK_ID);


CREATE INDEX idx__series__library_id on SERIES (LIBRARY_ID);

CREATE INDEX idx__series_metadata_genre__series_id on SERIES_METADATA_GENRE (SERIES_ID);
CREATE INDEX idx__series_metadata_tag__series_id on SERIES_METADATA_TAG (SERIES_ID);

CREATE INDEX idx__thumbnail_series__series_id on THUMBNAIL_SERIES (SERIES_ID);
