-- PostgreSQL ARRAY type handler integration test DDL

DROP TABLE IF EXISTS array_table;
CREATE TABLE array_table (
    id INT PRIMARY KEY,
    tags TEXT[]
);
