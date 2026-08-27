insert into transaction(id, name) values
      (1, 'Tra1'),
      (2, 'Tra2');

insert into transaction_node(id, transaction_id, name) values
    (1, 1, 'Node1'),
    (2, 1, 'Node2'),
    (3, 1, 'Node3'),
    (4, 2, 'Node4'),
    (5, 2, 'Node5');
