insert into invoice(id, name) values
    (1, 'Inv1'),
    (2, 'Inv2');

insert into invoice_entry(invoice_id, name, price, amount) values
    (1, 'E1', 55.1, 12),
    (1, 'E2', 65.1, 13),
    (1, 'E3', 75.1, 14),
    (2, 'E4', 85.1, 15),
    (2, 'E5', 71.1, 16);
