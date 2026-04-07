CREATE TABLE BOOK_METADATA_AGGREGATION_TAG
(
    TAG       varchar NOT NULL,
    SERIES_ID varchar NOT NULL,
    FOREIGN KEY (SERIES_ID) REFERENCES SERIES (ID)
);

-- aggregate existing data
insert into BOOK_METADATA_AGGREGATION_TAG
select distinct bt.TAG, b.SERIES_ID
from BOOK_METADATA_TAG bt
         left join BOOK B on B.ID = bt.BOOK_ID;
