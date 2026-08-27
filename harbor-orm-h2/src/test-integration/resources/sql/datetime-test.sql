CREATE TABLE events (
    id BIGINT PRIMARY KEY,
    event_date DATE,
    event_time TIME,
    event_timestamp TIMESTAMP
);

INSERT INTO events VALUES
(1, DATE '2024-06-15', TIME '14:30:45', TIMESTAMP '2024-06-15 14:30:45');
