-- COLLECTION_SERIES: primary key (COLLECTION_ID, SERIES_ID)
create index idx__collection_series__series_id on COLLECTION_SERIES (SERIES_ID);

-- READLIST_BOOK: primary key (READLIST_ID, BOOK_ID)
create index idx__readlist_book__book_id on READLIST_BOOK (BOOK_ID);

-- READ_PROGRESS: primary key (BOOK_ID, USER_ID)
create index idx__read_progress__user_id on READ_PROGRESS (USER_ID);

-- READ_PROGRESS_SERIES: primary key (SERIES_ID, USER_ID)
create index idx__read_progress_series__user_id on READ_PROGRESS_SERIES (USER_ID);

-- CLIENT_SETTINGS_USER: primary key (KEY, USER_ID)
create index idx__client_settings_user__user_id on CLIENT_SETTINGS_USER (USER_ID);

-- USER_LIBRARY_SHARING: primary key (USER_ID, LIBRARY_ID)
create index idx__user_library_sharing__library_id on USER_LIBRARY_SHARING (LIBRARY_ID);

-- USER_SHARING: primary key (LABEL, ALLOW, USER_ID)
create index idx__user_sharing__user_id on USER_SHARING (USER_ID);

-- redundant indices
DROP INDEX IF EXISTS idx__library_exclusions__library_id;
DROP INDEX IF EXISTS idx__sync_point_book__sync_point_id;
DROP INDEX IF EXISTS idx__sync_point_book_removed_status__sync_point_id;
DROP INDEX IF EXISTS idx__sync_point_readlist__sync_point_id;
DROP INDEX IF EXISTS idx__sync_point_readlist_book__sync_point_id_readlist_id;
