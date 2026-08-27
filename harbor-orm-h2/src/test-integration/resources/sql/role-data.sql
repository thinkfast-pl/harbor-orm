insert into role (id, name) values
    (1, 'Admin'),
    (2, 'User'),
    (3, 'Empty');

insert into role_permission (role_id, permission) values
    (1, 'USER_CREATE'),
    (1, 'USER_READ'),
    (1, 'USER_UPDATE'),
    (1, 'USER_DELETE'),
    (1, 'ROLE_CRUD'),
    (2, 'USER_READ');
