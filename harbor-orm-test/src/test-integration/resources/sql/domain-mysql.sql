-- Simulated sequences (MySQL has no native sequences); values are fetched by
-- MySqlStoredProcedureSequenceGeneratorHandler via the procedure below.
create table harbor_sequences (
    name varchar(100) not null,
    next_val bigint not null,
    primary key (name)
);

insert into harbor_sequences (name, next_val) values ('products_id_seq', 0), ('orders_id_seq', 0), ('audited_entities_id_seq', 0);

CREATE PROCEDURE harbor_sequence_nextval(IN seq_name VARCHAR(100)) UPDATE harbor_sequences SET next_val = LAST_INSERT_ID(next_val + 1) WHERE name = seq_name;

create table basics(
    id bigint not null,
    name varchar(100) not null,
    numero int not null,
    primary key (id)
);

create table basics_bytea(
    id bigint not null,
    name varchar(100) not null,
    data longblob not null,
    primary key (id)
);

create table basics_blob(
    id bigint not null,
    name varchar(100) not null,
    data longblob,
    primary key (id)
);

create table basics_clob(
    id bigint not null,
    name varchar(100) not null,
    data longtext,
    primary key (id)
);

create table basics_clob2(
    id bigint not null,
    name varchar(100) not null,
    data longtext,
    primary key (id)
);

create table roles
(
    id   bigint primary key auto_increment,
    name varchar(20) not null
);

create table role_permissions
(
    role_id    bigint  not null,
    permission varchar(50) not null,
    primary key (role_id, permission),
    foreign key (role_id) references roles (id)
);

create table users
(
    id           char(36) primary key,
    email        varchar(100) not null,
    password     varchar(255) not null,
    first_name   varchar(50)  not null,
    last_name    varchar(50)  not null,
    phone_number varchar(20)
);

create table products
(
    id          bigint primary key,
    name        varchar(50)    not null,
    price_net   numeric(22, 2) not null,
    vat_rate    numeric(22, 2) not null,
    price_gross numeric(22, 2) not null
);


create table orders
(
    id                bigint primary key,
    user_id           char(36)       not null,
    state             varchar(50)    not null,
    total_price_net   numeric(22, 2) not null,
    total_price_gross numeric(22, 2) not null,

    residence_region varchar(255) not null,
    residence_postal_code varchar(255) not null,
    residence_town varchar(255) not null,
    residence_street varchar(255) not null,
    residence_building_no varchar(255) not null,
    residence_apartment_no varchar(255),

    shipment_region varchar(255) not null,
    shipment_postal_code varchar(255) not null,
    shipment_town varchar(255) not null,
    shipment_street varchar(255) not null,
    shipment_building_no varchar(255) not null,
    shipment_apartment_no varchar(255),
    foreign key (user_id) references users (id)
);


create table order_items
(
    order_id          bigint,
    product_id        bigint         not null,
    amount            numeric(22, 2) not null,
    unit_price_net    numeric(22, 2) not null,
    total_price_net   numeric(22, 2) not null,
    vat_rate          numeric(22, 2) not null,
    total_price_gross numeric(22, 2) not null,
    primary key (order_id, product_id),
    foreign key (order_id) references orders (id),
    foreign key (product_id) references products (id)
);

create table employees (
    id bigint primary key,
    name varchar(100),
    department varchar(50),
    salary decimal(10, 2),
    manager_id bigint,
    foreign key (manager_id) references employees (id)
);

insert into employees (id, name, department, salary, manager_id) values
(1, 'CEO', 'Executive', 200000.00, null),
(2, 'CTO', 'Engineering', 150000.00, 1),
(3, 'Alice', 'Engineering', 75000.00, 2),
(4, 'Bob', 'Engineering', 80000.00, 2),
(5, 'Charlie', 'Engineering', 70000.00, 2),
(6, 'Sales VP', 'Sales', 120000.00, 1),
(7, 'Diana', 'Sales', 60000.00, 6),
(8, 'Eve', 'Sales', 65000.00, 6),
(9, 'Frank', 'Sales', 55000.00, 6);

create table events (
    id bigint primary key,
    event_date date,
    event_time time,
    event_timestamp datetime
);

insert into events values
(1, date '2024-06-15', time '14:30:45', timestamp '2024-06-15 14:30:45');

create table books (
    isbn_prefix varchar(20) not null,
    isbn_suffix int not null,
    title varchar(200) not null,
    author varchar(100) not null,
    primary key (isbn_prefix, isbn_suffix)
);

create table converted_fields (
    id bigint primary key,
    name varchar(100) not null,
    active_flag varchar(1),
    verified_flag varchar(1),
    metadata_json varchar(1000)
);

create table enumerated_test (
    id bigint primary key,
    name varchar(100) not null,
    status varchar(50) not null,
    priority int not null,
    secondary_status varchar(50),
    secondary_priority int
);

create table audited_entities (
    id bigint primary key,
    name varchar(100) not null,
    created_at datetime,
    updated_at datetime,
    insert_counter int,
    update_counter int
);


create table basics_embedded(
    id bigint not null,
    dest_country varchar(255) not null,
    street varchar(255),
    postal_code varchar(255),
    city varchar(255),
    primary key (id)
);

-- Element collection test tables

create table articles (
    id bigint not null,
    title varchar(200) not null,
    primary key (id)
);

create table article_tags (
    article_id bigint not null,
    tag varchar(50) not null,
    primary key (article_id, tag),
    foreign key (article_id) references articles (id)
);

create table contacts (
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);

create table contact_phones (
    contact_id bigint not null,
    phone_type varchar(20) not null,
    phone_number varchar(30) not null,
    primary key (contact_id, phone_type, phone_number),
    foreign key (contact_id) references contacts (id)
);

create table feature_flags (
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);

create table feature_flag_values (
    feature_flag_id bigint not null,
    enabled varchar(1) not null,
    primary key (feature_flag_id, enabled),
    foreign key (feature_flag_id) references feature_flags (id)
);

-- One-to-many test tables

create table authors (
    id bigint not null,
    name varchar(100) not null,
    email varchar(100),
    primary key (id)
);

create table publications (
    id bigint not null,
    author_id bigint not null,
    title varchar(200) not null,
    publication_year int,
    primary key (id),
    foreign key (author_id) references authors (id)
);

-- Constraint violation test table

create table accounts (
    id bigint not null,
    username varchar(50) not null unique,
    email varchar(100) not null unique,
    age int,
    primary key (id),
    check (age >= 18)
);

-- One-to-one test tables

create table members (
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);

create table member_profiles (
    id bigint not null,
    member_id bigint not null,
    bio varchar(500),
    primary key (id),
    foreign key (member_id) references members (id)
);

-- Many-to-Many: students <-> courses

create table students (
    id bigint not null,
    name varchar(255) not null,
    primary key (id)
);

create table courses (
    id bigint not null,
    title varchar(255) not null,
    primary key (id)
);

create table student_courses (
    student_id bigint not null,
    course_id bigint not null,
    constraint uq_student_course unique (student_id, course_id),
    foreign key (student_id) references students (id),
    foreign key (course_id) references courses (id)
);

create table tutors (
    id bigint not null,
    title varchar(255) not null,
    primary key (id)
);

create table tutor_students (
    tutor_id bigint not null,
    student_id bigint not null,
    constraint uq_tutor_student unique (tutor_id, student_id),
    foreign key (tutor_id) references tutors (id),
    foreign key (student_id) references students (id)
);

create table custom_supplier_test (
    id bigint primary key,
    `value` varchar(255)
);

create table versioned_products(
    id bigint not null,
    name varchar(100) not null,
    sku varchar(50) not null,
    version bigint not null default 0,
    primary key (id)
);

create table versioned_products2(
    id bigint not null,
    name varchar(100) not null,
    sku varchar(50) not null,
    version bigint not null default 0,
    primary key (id)
);

create table json_test_entities(
    id bigint not null,
    data longtext,
    profile longtext,
    config longtext,
    primary key (id)
);

-- stored function/procedure support
CREATE FUNCTION multiply_value(a INT, b INT) RETURNS INT DETERMINISTIC NO SQL RETURN a * b;

CREATE PROCEDURE do_nothing() BEGIN END;

CREATE PROCEDURE get_basics_above(IN min_numero INT) SELECT id, name, numero FROM basics WHERE numero > min_numero ORDER BY numero;

CREATE PROCEDURE get_basics_flags(IN min_numero INT) SELECT name, CASE WHEN numero > min_numero THEN 'Y' ELSE 'N' END AS active_flag FROM basics ORDER BY name;

CREATE PROCEDURE get_enumerated_summary() SELECT name, status, priority, CASE WHEN status = 'ACTIVE' THEN 'Y' ELSE 'N' END AS active_flag FROM enumerated_test ORDER BY name;

-- view support
CREATE VIEW basics_summary AS
SELECT name, numero FROM basics;

CREATE VIEW enumerated_summary AS
SELECT name, status, priority, CASE WHEN status = 'ACTIVE' THEN 'Y' ELSE 'N' END AS active_flag FROM enumerated_test;


create table type_handler_test(
    id bigint primary key,
    name varchar(100) not null,
    duration_ms bigint not null,
    nullable_duration_ms bigint
);

CREATE VIEW type_handler_summary AS
SELECT name, duration_ms, nullable_duration_ms FROM type_handler_test;

CREATE PROCEDURE get_type_handler_summary() SELECT name, duration_ms, nullable_duration_ms FROM type_handler_test ORDER BY name;


create table type_handler_dialect_test(
    id bigint primary key,
    name varchar(100) not null,
    duration_val bigint not null
);

create table offset_date_time_handler_test(
    id bigint primary key,
    name varchar(100) not null,
    event_time datetime(6) not null,
    nullable_event_time datetime(6)
);

create table offset_date_time_test(
    id bigint primary key,
    name varchar(100) not null,
    event_time datetime(6) not null,
    nullable_event_time datetime(6)
);

create table column_strategy_test(
    id bigint primary key,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    phone_number varchar(50)
);

-- Nested embedded entity test table
create table nested_embedded_entity(
    id bigint not null,
    name varchar(100),
    street varchar(255),
    city varchar(255),
    latitude double precision,
    longitude double precision,
    primary key (id)
);

CREATE PROCEDURE count_all_basics() SELECT COUNT(*) AS total FROM basics;

-- H-3 bug test: @Enumerated(ORDINAL) on element collection type-use
create table tasks (
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);

create table task_priorities (
    task_id bigint not null,
    priority int not null,
    primary key (task_id, priority),
    foreign key (task_id) references tasks (id)
);

-- Camel case embedded entity test table
create table camel_case_embedded(
    id bigint primary key,
    firstName varchar(100) not null,
    street varchar(255),
    postal_code varchar(255),
    city varchar(255)
);

create table book_tags (
    isbn_prefix varchar(20) not null,
    isbn_suffix int not null,
    tag varchar(100) not null
);

create table book_chapters (
    id bigint primary key,
    isbn_prefix varchar(20) not null,
    isbn_suffix int not null,
    title varchar(200) not null
);

create table book_summaries (
    id bigint primary key,
    isbn_prefix varchar(20) not null,
    isbn_suffix int not null,
    summary varchar(1000) not null,
    unique (isbn_prefix, isbn_suffix)
);

create table bookstores (
    id bigint primary key,
    name varchar(100) not null
);

create table bookstore_books (
    isbn_prefix varchar(20) not null,
    isbn_suffix int not null,
    store_id bigint not null
);

create table libraries (
    id bigint primary key,
    name varchar(100) not null
);

create table library_books (
    library_id bigint not null,
    isbn_prefix varchar(20) not null,
    isbn_suffix int not null
);

create table shipments (
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);

create table shipment_labels (
    id bigint not null,
    shipment_id bigint not null,
    barcode varchar(100) not null,
    primary key (id),
    foreign key (shipment_id) references shipments (id)
);

create table shipment_events (
    id bigint not null,
    shipment_id bigint not null,
    description varchar(200) not null,
    primary key (id),
    foreign key (shipment_id) references shipments (id)
);

create table package_scoped_items (
    id bigint not null,
    name varchar(100) not null,
    primary key (id)
);
