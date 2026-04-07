alter table library
    add column SCAN_FORCE_MODIFIED_TIME boolean NOT NULL DEFAULT false;
alter table library
    add column SCAN_DEEP boolean NOT NULL DEFAULT false;
