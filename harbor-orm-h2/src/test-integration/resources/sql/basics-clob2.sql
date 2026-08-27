create table basics_clob2(
    id bigint not null,
    name varchar(100) not null,
    data clob,
    primary key (id)
);
