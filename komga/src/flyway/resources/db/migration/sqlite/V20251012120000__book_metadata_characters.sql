-- Add characters support to book metadata
-- Following the same pattern as tags (BOOK_METADATA_TAG and BOOK_METADATA_AGGREGATION_TAG)

-- Table for book-level characters
CREATE TABLE BOOK_METADATA_CHARACTER
(
    CHARACTER varchar NOT NULL,
    BOOK_ID   varchar NOT NULL,
    FOREIGN KEY (BOOK_ID) REFERENCES BOOK (ID)
);

-- Index for performance
CREATE INDEX idx__book_metadata_character__book_id on BOOK_METADATA_CHARACTER (BOOK_ID);

-- Add characters lock column to book metadata
alter table BOOK_METADATA
    add column CHARACTERS_LOCK boolean NOT NULL DEFAULT 0;

-- Table for series-level aggregated characters
CREATE TABLE BOOK_METADATA_AGGREGATION_CHARACTER
(
    CHARACTER varchar NOT NULL,
    SERIES_ID varchar NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

-- Index for performance
CREATE INDEX idx__book_metadata_aggregation_character__series_id on BOOK_METADATA_AGGREGATION_CHARACTER (SERIES_ID);
