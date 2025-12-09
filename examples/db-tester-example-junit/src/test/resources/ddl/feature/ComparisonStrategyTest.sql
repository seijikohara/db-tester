-- DDL for ComparisonStrategyTest
-- Tests different comparison strategies for database assertions

CREATE TABLE IF NOT EXISTS COMPARISON_TEST (
    ID INTEGER PRIMARY KEY,
    NAME VARCHAR(100),
    AMOUNT DECIMAL(10, 2),
    EMAIL VARCHAR(255),
    TIMESTAMP TIMESTAMP,
    GENERATED_ID VARCHAR(100)
);
