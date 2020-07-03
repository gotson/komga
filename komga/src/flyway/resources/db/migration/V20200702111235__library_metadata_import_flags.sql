alter table library
    add column import_comicinfo_book boolean default true;
alter table library
    add column import_comicinfo_series boolean default true;
alter table library
    add column import_comicinfo_collection boolean default true;
alter table library
    add column import_epub_book boolean default true;
alter table library
    add column import_epub_series boolean default true;
