alter table book
    add (number float4 default 0);
update book
set number = (index + 1);
alter table book
    drop column index;
