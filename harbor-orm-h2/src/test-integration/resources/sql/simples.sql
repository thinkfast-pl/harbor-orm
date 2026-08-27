
create table simples (
    id bigint not null,
    my_boolean boolean not null,
    my_big_boolean boolean,
    my_small_byte tinyint not null,
    my_big_byte tinyint,
    my_short smallint not null,
    my_big_short smallint,
    my_int integer not null,
    my_big_int integer,
    my_long bigint not null,
    my_big_long bigint,
    my_float real not null,
    my_big_float real,
    my_double double precision not null,
    my_big_double double precision,
    my_big_integer bigint,
    my_big_decimal numeric(20, 2),
    varchar_value varchar(300),
    primary key (id)
);
