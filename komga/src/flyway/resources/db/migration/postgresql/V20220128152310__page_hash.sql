CREATE TABLE PAGE_HASH
(
    HASH               varchar  NOT NULL,
    MEDIA_TYPE         varchar  NOT NULL,
    SIZE               bigint     NULL,
    ACTION             varchar  NOT NULL,
    DELETE_COUNT       int      NOT NULL DEFAULT false,
    CREATED_DATE       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    LAST_MODIFIED_DATE timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (HASH, MEDIA_TYPE, SIZE)
);

CREATE TABLE PAGE_HASH_THUMBNAIL
(
    HASH       varchar NOT NULL,
    MEDIA_TYPE varchar NOT NULL,
    SIZE       bigint    NULL,
    THUMBNAIL  bytea    NOT NULL,
    PRIMARY KEY (HASH, MEDIA_TYPE, SIZE)
);

UPDATE MEDIA_PAGE
SET FILE_HASH = ''
WHERE BOOK_ID IN (
    SELECT DISTINCT m.BOOK_ID
    FROM MEDIA m
             LEFT JOIN MEDIA_PAGE MP on m.BOOK_ID = MP.BOOK_ID
    WHERE mp.FILE_HASH <> ''
      AND m.MEDIA_TYPE <> 'application/zip'
);
