-- DDL for TableMergeStrategyTest
-- Tests different table merge strategies

CREATE TABLE IF NOT EXISTS MERGE_TABLE (
    ID INTEGER PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL
);

-- MERGE_TABLE_NO_PK has no PRIMARY KEY so UNION_ALL can append duplicate rows
-- without primary key violations, which is required to prove that UNION_ALL
-- preserves duplicates whereas UNION removes them.
CREATE TABLE IF NOT EXISTS MERGE_TABLE_NO_PK (
    ID INTEGER NOT NULL,
    NAME VARCHAR(100) NOT NULL
);
