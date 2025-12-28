-- DDL for ComprehensiveDataTypesTest (H2 Database)
-- Comprehensive data types that are CSV-representable

CREATE TABLE DATA_TYPES (
    ID INTEGER PRIMARY KEY,

    -- Integer types
    TINYINT_COL TINYINT,              -- -128 to 127
    SMALLINT_COL SMALLINT,            -- -32768 to 32767
    INT_COL INTEGER,                  -- -2147483648 to 2147483647
    BIGINT_COL BIGINT,                -- -9223372036854775808 to 9223372036854775807

    -- Decimal/Numeric types
    DECIMAL_COL DECIMAL(10, 2),       -- Fixed precision decimal
    NUMERIC_COL NUMERIC(15, 5),       -- Synonym for DECIMAL

    -- Floating point types
    REAL_COL REAL,                    -- 32-bit floating point
    FLOAT_COL FLOAT,                  -- Synonym for DOUBLE
    DOUBLE_COL DOUBLE,                -- 64-bit floating point
    DOUBLE_PRECISION_COL DOUBLE PRECISION, -- Synonym for DOUBLE

    -- Character string types
    CHAR_COL CHAR(10),                -- Fixed-length string
    VARCHAR_COL VARCHAR(100),         -- Variable-length string
    VARCHAR_IGNORECASE_COL VARCHAR_IGNORECASE(50), -- Case-insensitive comparison
    LONGVARCHAR_COL LONGVARCHAR,      -- Long variable-length string
    CLOB_COL CLOB,                    -- Character Large Object
    TEXT_COL TEXT,                    -- Alias for CLOB

    -- Date/Time types
    DATE_COL DATE,                    -- Date only (YYYY-MM-DD)
    TIME_COL TIME,                    -- Time only (HH:MM:SS)
    TIMESTAMP_COL TIMESTAMP,          -- Date and time

    -- Boolean type
    BOOLEAN_COL BOOLEAN,              -- TRUE/FALSE
    BIT_COL BIT,                      -- Synonym for BOOLEAN

    -- Binary type (CSV representable with BASE64 encoding)
    BLOB_COL BLOB,                    -- Binary Large Object

    -- UUID type (stored as VARCHAR for CSV compatibility)
    UUID_COL VARCHAR(36)              -- Universally Unique Identifier (string format)
);
