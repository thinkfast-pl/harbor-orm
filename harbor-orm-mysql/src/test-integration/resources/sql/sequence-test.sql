create table harbor_sequences (
    name varchar(100) not null,
    next_val bigint not null,
    primary key (name)
);

insert into harbor_sequences (name, next_val) values ('test_seq', 0);

CREATE PROCEDURE harbor_sequence_nextval(IN seq_name VARCHAR(100)) UPDATE harbor_sequences SET next_val = LAST_INSERT_ID(next_val + 1) WHERE name = seq_name;
