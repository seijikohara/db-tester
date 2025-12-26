-- Cassandra Integration Test DDL

DROP TABLE IF EXISTS table1;
CREATE TABLE table1 (
    id INT PRIMARY KEY,
    column1 TEXT,
    column2 TIMESTAMP,
    column3 BOOLEAN
);
