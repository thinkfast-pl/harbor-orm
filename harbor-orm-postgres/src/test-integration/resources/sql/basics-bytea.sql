create table basics_bytea(
   id bigint not null,
   name varchar(100) not null,
   data bytea not null ,
   primary key (id)
);
