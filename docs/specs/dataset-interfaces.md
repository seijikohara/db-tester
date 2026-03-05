---
title: "Dataset Interfaces - DB Tester"
description: "Reference for DB Tester dataset interfaces: TableSet, Table, Row, and domain value objects."
---

# Dataset Interfaces

## TableSet

Represents a collection of database tables.

**Location**: `io.github.seijikohara.dbtester.api.dataset.TableSet`

**Factory Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `of(List<Table>)` | `TableSet` | Creates a table set with the specified tables |
| `of(Table...)` | `TableSet` | Creates a table set with the specified tables (varargs) |

**Instance Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getTables()` | `List<Table>` | Returns immutable list of tables in declaration order |
| `getTable(TableName)` | `Optional<Table>` | Finds table by name |
| `getDataSource()` | `Optional<DataSource>` | Returns bound DataSource if specified |

**Guarantees**:

- Table order is preserved (insertion order)
- All returned collections are immutable
- Table names are unique within a table set

## Table

Represents the structure and data of a database table.

**Location**: `io.github.seijikohara.dbtester.api.dataset.Table`

**Factory Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `of(TableName, List<ColumnName>, List<Row>)` | `Table` | Creates a table with type-safe names |
| `of(String, List<String>, List<Row>)` | `Table` | Creates a table with string names (convenience) |
| `ofValues(String, List<String>, List<List<?>>)` | `Table` | Creates a table from raw values without wrapping (convenience) |

**Instance Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getName()` | `TableName` | Returns table identifier |
| `getColumns()` | `List<ColumnName>` | Returns column names in definition order |
| `getRows()` | `List<Row>` | Returns all rows (may be empty) |
| `getRowCount()` | `int` | Returns number of rows |

**Guarantees**:

- Column order is consistent across all rows
- All returned collections are immutable
- Row count equals `getRows().size()`

## Row

Represents a single database record.

**Location**: `io.github.seijikohara.dbtester.api.dataset.Row`

**Factory Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `of(Map<ColumnName, CellValue>)` | `Row` | Creates a row with the specified column-value pairs |
| `of(List<String>, List<?>)` | `Row` | Creates a row by pairing column names with raw values (convenience) |

**Instance Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getValues()` | `Map<ColumnName, CellValue>` | Returns immutable column-value mapping |
| `getValue(ColumnName)` | `CellValue` | Returns value for column; `CellValue.NULL` if absent |

## Domain Value Objects

### CellValue

Wraps a cell value with explicit null handling.

**Location**: `io.github.seijikohara.dbtester.api.domain.CellValue`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `value` | `@Nullable Object` | The wrapped value |

**Constants**:

| Constant | Description |
|----------|-------------|
| `CellValue.NULL` | Singleton representing SQL NULL |

**Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `isNull()` | `boolean` | Returns `true` if value is null |

### TableName

Immutable identifier for a database table.

**Location**: `io.github.seijikohara.dbtester.api.domain.TableName`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `value` | `String` | Table name string |

### ColumnName

Immutable identifier for a table column.

**Location**: `io.github.seijikohara.dbtester.api.domain.ColumnName`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `value` | `String` | Column name string |

### DataSourceName

Immutable identifier for a registered DataSource.

**Location**: `io.github.seijikohara.dbtester.api.domain.DataSourceName`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `value` | `String` | DataSource name string |

### Column

Represents a database column with metadata and comparison strategy.

**Location**: `io.github.seijikohara.dbtester.api.domain.Column`

**Type**: `final class` (implements `Comparable<Column>`)

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `name` | `ColumnName` | Column identifier |
| `metadata` | `@Nullable ColumnMetadata` | Optional column metadata for schema validation |
| `comparisonStrategy` | `ComparisonStrategy` | Comparison strategy for this column (default: `STRICT`) |

**Factory Methods**:

| Method | Description |
|--------|-------------|
| `of(String)` | Creates a column with default strategy and no metadata |
| `of(ColumnName)` | Creates a column from an existing ColumnName |
| `builder(String)` | Creates a builder for custom properties |
| `builder(ColumnName)` | Creates a builder from an existing ColumnName |

**Instance Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getName()` | `ColumnName` | Returns column name |
| `getNameValue()` | `String` | Returns column name as a string |
| `getMetadata()` | `Optional<ColumnMetadata>` | Returns metadata if available |
| `getComparisonStrategy()` | `ComparisonStrategy` | Returns comparison strategy |
| `hasMetadata()` | `boolean` | Returns `true` if metadata is present |
| `isIgnored()` | `boolean` | Returns `true` if strategy is IGNORE |
| `withComparisonStrategy(ComparisonStrategy)` | `Column` | Returns new Column with updated strategy |
| `withMetadata(ColumnMetadata)` | `Column` | Returns new Column with updated metadata |

### Cell

Represents a single cell value within a database row.

**Location**: `io.github.seijikohara.dbtester.api.domain.Cell`

**Type**: `final class`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `column` | `Column` | Column definition |
| `value` | `CellValue` | Cell value |

**Factory Methods**:

| Method | Description |
|--------|-------------|
| `of(Column, CellValue)` | Creates a cell with the specified column and value |
| `of(String, CellValue)` | Creates a cell with a column name and value |
| `of(String, @Nullable Object)` | Creates a cell with a column name and raw value |
| `nullCell(Column)` | Creates a NULL cell for the specified column |

**Instance Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getColumn()` | `Column` | Returns the column |
| `getColumnName()` | `ColumnName` | Returns the column name |
| `getValue()` | `CellValue` | Returns the cell value |
| `getRawValue()` | `Optional<Object>` | Returns the raw value, or empty if NULL |
| `isNull()` | `boolean` | Returns `true` if value is NULL |
| `shouldIgnore()` | `boolean` | Returns `true` if column strategy is IGNORE |
| `getValueAsString()` | `Optional<String>` | Returns value as String, or empty if NULL |
| `getValueAsNumber()` | `Optional<Number>` | Returns value as Number, or empty if not applicable |

### ColumnMetadata

Immutable metadata describing the schema properties of a database column.

**Location**: `io.github.seijikohara.dbtester.api.domain.ColumnMetadata`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `jdbcType` | `@Nullable JDBCType` | The SQL type of the column, or null if unknown |
| `nullable` | `boolean` | Whether the column allows NULL values |
| `primaryKey` | `boolean` | Whether the column is part of the primary key |
| `ordinalPosition` | `int` | 1-based position in the table (0 if unknown) |
| `precision` | `int` | Numeric precision or character length (0 if not applicable) |
| `scale` | `int` | Numeric scale (0 if not applicable) |
| `defaultValue` | `@Nullable String` | Default value as a string, or null if none |

**Factory Methods**:

| Method | Description |
|--------|-------------|
| `of(JDBCType, boolean)` | Creates metadata with minimal information |
| `primaryKey(JDBCType)` | Creates metadata for a primary key column |

**Instance Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `isNumeric()` | `boolean` | Returns `true` if column type is numeric |
| `isTextual()` | `boolean` | Returns `true` if column type is text-based |
| `isTemporal()` | `boolean` | Returns `true` if column type is date/time |
| `isBinary()` | `boolean` | Returns `true` if column type is binary |
| `isBoolean()` | `boolean` | Returns `true` if column type is boolean |
| `isLikelyAutoIncrement()` | `boolean` | Returns `true` if column appears to be auto-increment |

### ColumnStrategyMapping

Represents programmatic column comparison strategy configuration.

**Location**: `io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `columnName` | `String` | Column name normalized to uppercase |
| `strategy` | `ComparisonStrategy` | Comparison strategy for this column |

**Factory Methods**:

| Method | Description |
|--------|-------------|
| `of(String, ComparisonStrategy)` | Creates mapping with specified strategy |
| `strict(String)` | Creates mapping with STRICT strategy |
| `ignore(String)` | Creates mapping with IGNORE strategy |
| `caseInsensitive(String)` | Creates mapping with CASE_INSENSITIVE strategy |
| `numeric(String)` | Creates mapping with NUMERIC strategy |
| `timestampFlexible(String)` | Creates mapping with TIMESTAMP_FLEXIBLE strategy |
| `notNull(String)` | Creates mapping with NOT_NULL strategy |
| `regex(String, String)` | Creates mapping with REGEX strategy and pattern |
| `dateFlexible(String)` | Creates mapping with DATE_FLEXIBLE strategy |
| `jsonEquivalent(String)` | Creates mapping with JSON_EQUIVALENT strategy |

**Example**:

```java
// Programmatic column strategy configuration
var strategies = List.of(
    ColumnStrategyMapping.ignore("CREATED_AT"),
    ColumnStrategyMapping.caseInsensitive("EMAIL"),
    ColumnStrategyMapping.regex("TOKEN", "[a-f0-9-]{36}"),
    ColumnStrategyMapping.dateFlexible("BIRTH_DATE"),
    ColumnStrategyMapping.jsonEquivalent("METADATA")
);

DatabaseAssertion.assertEqualsWithStrategies(expectedTable, actualTable, strategies);
```

### ComparisonStrategy

Defines value comparison behavior during assertion.

**Location**: `io.github.seijikohara.dbtester.api.domain.ComparisonStrategy`

**Predefined Strategies**:

| Strategy | Description |
|----------|-------------|
| `STRICT` | Exact match using `equals()` (default) |
| `IGNORE` | Skip comparison entirely |
| `NUMERIC` | Type-aware numeric comparison using BigDecimal |
| `CASE_INSENSITIVE` | Case-insensitive string comparison |
| `TIMESTAMP_FLEXIBLE` | Converts to UTC and ignores sub-second precision |
| `DATE_FLEXIBLE` | Multi-format date comparison (ISO-8601 `yyyy-MM-dd`, slashed `yyyy/MM/dd`, dot `yyyy.MM.dd`) |
| `JSON_EQUIVALENT` | JSON structural comparison (ignores key order and whitespace) |
| `NOT_NULL` | Verifies value is not null |

**Factory Methods**:

| Method | Description |
|--------|-------------|
| `regex(String)` | Creates regex pattern matcher with the specified pattern |

**Comparison Behavior**:

| Strategy | null/null | null/value | value/null | value/value |
|----------|-----------|------------|------------|-------------|
| `STRICT` | true | false | false | equals() |
| `IGNORE` | true | true | true | true |
| `NUMERIC` | true | false | false | BigDecimal comparison |
| `CASE_INSENSITIVE` | true | false | false | equalsIgnoreCase() |
| `TIMESTAMP_FLEXIBLE` | true | false | false | UTC epoch comparison |
| `DATE_FLEXIBLE` | true | false | false | LocalDate comparison |
| `JSON_EQUIVALENT` | true | false | false | Normalized JSON comparison |
| `NOT_NULL` | false | false | false | true |
| `REGEX` | false | false | false | Pattern.matches() |

**Architecture Note**: `ComparisonStrategy` serves as a descriptor (what to compare).
Comparison execution (how to compare) is handled by `ComparisonEngine` in the core module.

## Related Specifications

- [API Overview](public-api) - API layers and module organization
- [Annotations](annotations) - @DataSet, @ExpectedDataSet, @ColumnStrategy
- [Programmatic API](assertion-api) - DatabaseAssertion, Export, and Preparation APIs
- [Exceptions](exceptions) - Exception hierarchy and default values
