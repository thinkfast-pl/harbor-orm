create table basics_blob(
    id bigint not null,
    name varchar(100) not null,
    data blob,
    primary key (id)
);
