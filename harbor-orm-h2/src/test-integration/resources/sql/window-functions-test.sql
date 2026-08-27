CREATE TABLE employees (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    department VARCHAR(50),
    salary DECIMAL(10, 2)
);

INSERT INTO employees (id, name, department, salary) VALUES
(1, 'Alice', 'Engineering', 75000.00),
(2, 'Bob', 'Engineering', 80000.00),
(3, 'Charlie', 'Engineering', 70000.00),
(4, 'Diana', 'Sales', 60000.00),
(5, 'Eve', 'Sales', 65000.00),
(6, 'Frank', 'Sales', 55000.00);
