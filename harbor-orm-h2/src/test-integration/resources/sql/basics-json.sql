create table basics_json(
    id bigint not null,
    json_data json not null,
    varchar_data varchar(2000) not null,
    primary key (id)
);
