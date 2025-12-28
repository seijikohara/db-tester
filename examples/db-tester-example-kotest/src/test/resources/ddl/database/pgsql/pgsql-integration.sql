-- PostgreSQL Integration Test DDL

DROP TABLE IF EXISTS table1;
CREATE TABLE table1 (
    id INT PRIMARY KEY,
    column1 VARCHAR(100) NOT NULL,
    column2 TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    -- PostgreSQL-specific types (CSV-compatible)
    column3 BOOLEAN NOT NULL,
    column4 NUMERIC(3, 1),
    column5 TEXT
);
