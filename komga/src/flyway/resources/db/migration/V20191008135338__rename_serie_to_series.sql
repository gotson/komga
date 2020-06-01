alter table serie
    rename to series;

alter table series
    rename constraint fk_serie_library_library_id to fk_series_library_library_id;

alter index if exists fk_serie_library_library_id_index_4 rename to fk_series_library_library_id_index_4;


alter table book
    alter column serie_id
        rename to series_id;

alter table book
    rename constraint fk_book_serie_serie_id to fk_book_series_series_id;

alter index if exists fk_book_serie_serie_id_index_1 rename to fk_book_series_series_id_index_1;
