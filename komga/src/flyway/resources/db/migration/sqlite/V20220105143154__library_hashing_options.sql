alter table library
    add column HASH_FILES boolean NOT NULL DEFAULT ${library-file-hashing};
alter table library
    add column HASH_PAGES boolean NOT NULL DEFAULT 0;
alter table library
    add column ANALYZE_DIMENSIONS boolean NOT NULL DEFAULT 1;
