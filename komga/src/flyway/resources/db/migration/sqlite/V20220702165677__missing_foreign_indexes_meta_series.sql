create index idx__series_metadata_sharing__series_id
    on SERIES_METADATA_SHARING (SERIES_ID);

create index idx__book_metadata_aggregation_tag__series_id
    on BOOK_METADATA_AGGREGATION_TAG (SERIES_ID);
