create table transaction(
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);

create table transaction_node(
    id bigint not null,
    transaction_id bigint not null references transaction(id),
    name varchar(100) not null,
    primary key (id)
);
