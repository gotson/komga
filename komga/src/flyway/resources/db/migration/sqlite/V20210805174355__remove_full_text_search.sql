drop table fts_book_metadata;
DROP TRIGGER book_metadata__after_insert;
DROP TRIGGER book_metadata__after_delete;
DROP TRIGGER book_metadata__after_update;

drop table fts_series_metadata;
DROP TRIGGER series_metadata__after_insert;
DROP TRIGGER series_metadata__after_delete;
DROP TRIGGER series_metadata__after_update;

drop table fts_collection;
DROP TRIGGER collection__after_insert;
DROP TRIGGER collection__after_delete;
DROP TRIGGER collection__after_update;

drop table fts_readlist;
DROP TRIGGER readlist__after_insert;
DROP TRIGGER readlist__after_delete;
DROP TRIGGER readlist__after_update;

drop table fts_book_metadata_aggregation_author;
DROP TRIGGER book_metadata_aggregation_author__after_insert;
DROP TRIGGER book_metadata_aggregation_author__after_delete;
DROP TRIGGER book_metadata_aggregation_author__after_update;
