-- missing foreign key indices
create index if not exists idx__book_metadata_link__book_id
    on BOOK_METADATA_LINK (BOOK_ID);
create index if not exists idx__series_metadata_sharing__series_id
    on SERIES_METADATA_SHARING (SERIES_ID);
create index if not exists idx__book_metadata_aggregation_tag__series_id
    on BOOK_METADATA_AGGREGATION_TAG (SERIES_ID);
create index if not exists idx__thumbnail_collection__collection_id
    on THUMBNAIL_COLLECTION (COLLECTION_ID);
create index if not exists idx__thumbnail_readlist__readlist_id
    on THUMBNAIL_READLIST (READLIST_ID);
create index if not exists idx__thumbnail_series__series_id
    on THUMBNAIL_SERIES (SERIES_ID);
create index if not exists idx__authentication_activity__user_id
    on AUTHENTICATION_ACTIVITY (USER_ID);

-- if you sort by it, index it
create index if not exists idx__book_metadata__number_sort
    on BOOK_METADATA (NUMBER_SORT);
create index if not exists idx__series__last_modified_date
    on SERIES (LAST_MODIFIED_DATE);
create index if not exists idx__series__created_date
    on SERIES (CREATED_DATE);
create index if not exists idx__book_metadata__release_date
    on BOOK_METADATA (RELEASE_DATE);
create index if not exists idx__book__created_date
    on BOOK (CREATED_DATE);
create index if not exists idx__read_progress__last_modified_date
    on READ_PROGRESS (LAST_MODIFIED_DATE);
create index if not exists idx__media__status
    on MEDIA (STATUS);
