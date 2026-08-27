create table fts_articles (
    id bigint not null,
    title varchar(200) not null,
    body varchar(2000) not null,
    primary key (id)
);

insert into fts_articles (id, title, body) values
(1, 'Introduction to PostgreSQL', 'PostgreSQL is an advanced open-source relational database system with strong support for complex queries.'),
(2, 'Full-Text Search in Databases', 'Full-text search enables searching through large volumes of text data efficiently using inverted indexes.'),
(3, 'Java ORM Comparison', 'Comparing Hibernate, jOOQ, and other Java ORM frameworks for database access and query building.'),
(4, 'PostgreSQL Advanced Features', 'PostgreSQL supports advanced features like JSONB, full-text search, lateral joins, and window functions.'),
(5, 'Introduction to MySQL', 'MySQL is a popular open-source relational database known for its speed and ease of use.');

CREATE ALIAS IF NOT EXISTS FT_INIT FOR 'org.h2.fulltext.FullText.init';
CALL FT_INIT();
CALL FT_CREATE_INDEX('PUBLIC', 'FTS_ARTICLES', NULL);
