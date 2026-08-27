create table role(
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);

create table role_permission(
    role_id bigint not null references role(id),
    permission varchar(20) not null,
    primary key (role_id, permission)
);
