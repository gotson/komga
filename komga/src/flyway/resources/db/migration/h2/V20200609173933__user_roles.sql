alter table user
    add column role_file_download boolean default true;
alter table user
    add column role_page_streaming boolean default true;
