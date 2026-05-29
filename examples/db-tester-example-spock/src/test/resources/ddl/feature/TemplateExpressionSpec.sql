-- DDL for TemplateExpressionSpec
-- Tests template expression processing in dataset values

CREATE TABLE IF NOT EXISTS TEMPLATE_DATA (
    ID INTEGER PRIMARY KEY,
    UUID_VALUE VARCHAR(36),
    NAME VARCHAR(200),
    CREATED_AT VARCHAR(30)
);
