create table invoice
(
    id   bigint       not null,
    name varchar(100) not null,
    primary key (id)
);

create table invoice_entry
(
    invoice_id bigint      not null references invoice (id),
    name       varchar(20) not null,
    price      varchar(20) not null,
    amount     varchar(20) not null,
    primary key (invoice_id, name, price, amount)
);
