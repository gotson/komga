-- FTS for BOOK_METADATA
create virtual table fts_book_metadata using fts5(title, isbn, book_id UNINDEXED, content=book_metadata, tokenize = 'porter unicode61 remove_diacritics 2');
INSERT INTO fts_book_metadata(fts_book_metadata) VALUES('rebuild');

-- Triggers to keep the FTS index up to date
CREATE TRIGGER book_metadata__after_insert AFTER INSERT ON book_metadata BEGIN
    INSERT INTO fts_book_metadata(rowid, title, isbn, book_id) VALUES (new.rowid, new.title, new.isbn, new.book_id);
END;
CREATE TRIGGER book_metadata__after_delete AFTER DELETE ON book_metadata BEGIN
    INSERT INTO fts_book_metadata(fts_book_metadata, rowid, title, isbn, book_id) VALUES('delete', old.rowid, old.title, old.isbn, old.book_id);
END;
CREATE TRIGGER book_metadata__after_update AFTER UPDATE ON book_metadata BEGIN
    INSERT INTO fts_book_metadata(fts_book_metadata, rowid, title, isbn, book_id) VALUES('delete', old.rowid, old.title, old.isbn, old.book_id);
    INSERT INTO fts_book_metadata(rowid, title, isbn, book_id) VALUES (new.rowid, new.title, new.isbn, new.book_id);
END;


-- FTS for SERIES_METADATA
create virtual table fts_series_metadata using fts5(title, publisher, series_id UNINDEXED, content=series_metadata, tokenize = 'porter unicode61 remove_diacritics 2');
INSERT INTO fts_series_metadata(fts_series_metadata) VALUES('rebuild');

-- Triggers to keep the FTS index up to date
CREATE TRIGGER series_metadata__after_insert AFTER INSERT ON series_metadata BEGIN
    INSERT INTO fts_series_metadata(rowid, title, publisher, series_id) VALUES (new.rowid, new.title, new.publisher, new.series_id);
END;
CREATE TRIGGER series_metadata__after_delete AFTER DELETE ON series_metadata BEGIN
    INSERT INTO fts_series_metadata(fts_series_metadata, rowid, title, publisher, series_id) VALUES('delete', old.rowid, old.title, old.publisher, old.series_id);
END;
CREATE TRIGGER series_metadata__after_update AFTER UPDATE ON series_metadata BEGIN
    INSERT INTO fts_series_metadata(fts_series_metadata, rowid, title, publisher, series_id) VALUES('delete', old.rowid, old.title, old.publisher, old.series_id);
    INSERT INTO fts_series_metadata(rowid, title, publisher, series_id) VALUES (new.rowid, new.title, new.publisher, new.series_id);
END;


-- FTS for COLLECTION
create virtual table fts_collection using fts5(name, id UNINDEXED, content=collection, tokenize = 'porter unicode61 remove_diacritics 2');
INSERT INTO fts_collection(fts_collection) VALUES('rebuild');

-- Triggers to keep the FTS index up to date
CREATE TRIGGER collection__after_insert AFTER INSERT ON collection BEGIN
    INSERT INTO fts_collection(rowid, name, id) VALUES (new.rowid, new.name, new.id);
END;
CREATE TRIGGER collection__after_delete AFTER DELETE ON collection BEGIN
    INSERT INTO fts_collection(fts_collection, rowid, name, id) VALUES('delete', old.rowid, old.name, old.id);
END;
CREATE TRIGGER collection__after_update AFTER UPDATE ON collection BEGIN
    INSERT INTO fts_collection(fts_collection, rowid, name, id) VALUES('delete', old.rowid, old.name, old.id);
    INSERT INTO fts_collection(rowid, name, id) VALUES (new.rowid, new.name, new.id);
END;


-- FTS for READLIST
create virtual table fts_readlist using fts5(name, id UNINDEXED, content=readlist, tokenize = 'porter unicode61 remove_diacritics 2');
INSERT INTO fts_readlist(fts_readlist) VALUES('rebuild');

-- Triggers to keep the FTS index up to date
CREATE TRIGGER readlist__after_insert AFTER INSERT ON readlist BEGIN
    INSERT INTO fts_readlist(rowid, name, id) VALUES (new.rowid, new.name, new.id);
END;
CREATE TRIGGER readlist__after_delete AFTER DELETE ON readlist BEGIN
    INSERT INTO fts_readlist(fts_readlist, rowid, name, id) VALUES('delete', old.rowid, old.name, old.id);
END;
CREATE TRIGGER readlist__after_update AFTER UPDATE ON readlist BEGIN
    INSERT INTO fts_readlist(fts_readlist, rowid, name, id) VALUES('delete', old.rowid, old.name, old.id);
    INSERT INTO fts_readlist(rowid, name, id) VALUES (new.rowid, new.name, new.id);
END;


-- FTS for BOOK_METADATA_AGGREGATION_AUTHORS
create virtual table fts_book_metadata_aggregation_author using fts5(name, series_id UNINDEXED, content=book_metadata_aggregation_author, tokenize = 'porter unicode61 remove_diacritics 2');
INSERT INTO fts_book_metadata_aggregation_author(fts_book_metadata_aggregation_author) VALUES('rebuild');

-- Triggers to keep the FTS index up to date
CREATE TRIGGER book_metadata_aggregation_author__after_insert AFTER INSERT ON book_metadata_aggregation_author BEGIN
    INSERT INTO fts_book_metadata_aggregation_author(rowid, name, series_id) VALUES (new.rowid, new.name, new.series_id);
END;
CREATE TRIGGER book_metadata_aggregation_author__after_delete AFTER DELETE ON book_metadata_aggregation_author BEGIN
    INSERT INTO fts_book_metadata_aggregation_author(fts_book_metadata_aggregation_author, rowid, name, series_id) VALUES('delete', old.rowid, old.name, old.series_id);
END;
CREATE TRIGGER book_metadata_aggregation_author__after_update AFTER UPDATE ON book_metadata_aggregation_author BEGIN
    INSERT INTO fts_book_metadata_aggregation_author(fts_book_metadata_aggregation_author, rowid, name, series_id) VALUES('delete', old.rowid, old.name, old.series_id);
    INSERT INTO fts_book_metadata_aggregation_author(rowid, name, series_id) VALUES (new.rowid, new.name, new.series_id);
END;
