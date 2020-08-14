alter table library
    add column SCAN_FORCE_MODIFIED_TIME boolean NOT NULL DEFAULT 0;
alter table library
    add column SCAN_DEEP boolean NOT NULL DEFAULT 0;
