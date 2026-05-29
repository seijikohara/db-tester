-- DDL for DataSetExportSpec
-- Tests dataset export functionality

CREATE TABLE IF NOT EXISTS EXPORT_DATA (
    ID INTEGER PRIMARY KEY,
    NAME VARCHAR(100) NOT NULL,
    AMOUNT DECIMAL(10,2) NOT NULL
);
