create table basics_comp_id(
    type varchar not null,
    num int not null,
    "value" varchar(100) not null,
    primary key (type, num)
);
