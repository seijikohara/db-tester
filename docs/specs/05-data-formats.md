# DB Tester Specification - Data Formats

This document describes the file formats supported by the DB Tester framework and their parsing rules.

## Supported Formats

The framework supports two delimited text formats:

| Format | Extension | Delimiter | Default |
|--------|-----------|-----------|---------|
| CSV | `.csv` | Comma (`,`) | Yes |
| TSV | `.tsv` | Tab (`\t`) | No |

### Format Selection

Configure the format in `ConventionSettings`:

```java
var conventions = ConventionSettings.standard()
    .withDataFormat(DataFormat.TSV);
```

When loading datasets from a directory, only files matching the configured extension are processed.

## File Structure

### Basic Structure

Each file represents one database table:

- Filename (without extension) = Table name
- First row = Column headers
- Subsequent rows = Data records

### Example CSV

File: `USERS.csv`

```csv
id,name,email,created_at
1,Alice,alice@example.com,2024-01-01 00:00:00
2,Bob,bob@example.com,2024-01-02 00:00:00
```

Represents:

| Column | Values |
|--------|--------|
| `id` | 1, 2 |
| `name` | Alice, Bob |
| `email` | alice@example.com, bob@example.com |
| `created_at` | 2024-01-01 00:00:00, 2024-01-02 00:00:00 |

### Example TSV

File: `ORDERS.tsv`

```tsv
order_id	user_id	amount	status
1001	1	99.99	PENDING
1002	2	149.50	COMPLETED
```

## Scenario Filtering

### Scenario Marker Column

The scenario marker column enables multiple test methods to share dataset files:

| Column Name | Configurable | Default |
|-------------|--------------|---------|
| `[Scenario]` | Yes | `[Scenario]` |

### Scenario Column Behavior

When a dataset file contains the scenario marker column:

1. Filter rows where the marker matches the current scenario
2. Remove the scenario marker column from the resulting dataset
3. Pass remaining columns and data to the database operation

### Example with Scenarios

File: `USERS.csv`

```csv
[Scenario],id,name,email
testCreate,1,Alice,alice@example.com
testCreate,2,Bob,bob@example.com
testUpdate,3,Charlie,charlie@example.com
testDelete,4,Diana,diana@example.com
```

For test method `testCreate`, the framework filters to:

| id | name | email |
|----|------|-------|
| 1 | Alice | alice@example.com |
| 2 | Bob | bob@example.com |

### Scenario Resolution

The scenario name is resolved in the following order:

1. Explicit `scenarioNames` in `@DataSetSource` annotation
2. Test method name (via `ScenarioNameResolver` SPI)

### Multiple Scenarios

A single test can use multiple scenarios:

```java
@DataSet(sources = @DataSetSource(scenarioNames = {"scenario1", "scenario2"}))
void testMultipleScenarios() { }
```

Rows matching any of the specified scenarios are included.

## Special Values

### NULL Values

Represent SQL NULL using an empty field:

```csv
id,name,description
1,Alice,
2,Bob,A description
```

Row 1: `description` is NULL
Row 2: `description` is "A description"

### Empty Strings vs NULL

| File Content | Interpretation |
|--------------|----------------|
| Empty field | NULL |
| Empty quoted field (`""`) | Empty string |

Example:

```csv
id,nullable_col,empty_string_col
1,,""
```

- `nullable_col` = NULL
- `empty_string_col` = "" (empty string)

### Quoted Values

Values containing delimiters or special characters must be quoted:

| Value | Encoding |
|-------|----------|
| Contains comma | `"value,with,commas"` |
| Contains quotes | `"value ""with"" quotes"` |
| Contains newline | `"line1\nline2"` |
| Starts with whitespace | `" leading space"` |

## Directory Convention

### Standard Directory Structure

```
src/test/resources/
└── {package}/
    └── {TestClassName}/
        ├── TABLE1.csv          # DataSet data
        ├── TABLE2.csv
        ├── load-order.txt      # Table ordering (optional)
        └── expected/           # ExpectedDataSet data
            ├── TABLE1.csv
            └── TABLE2.csv
```

### Package Path Resolution

The package path mirrors the test class package:

| Test Class | Package Path |
|------------|--------------|
| `com.example.UserRepositoryTest` | `com/example/UserRepositoryTest/` |
| `org.app.service.OrderServiceTest` | `org/app/service/OrderServiceTest/` |

### Nested Test Classes

For JUnit nested test classes:

| Test Class | Directory |
|------------|-----------|
| `UserTest$NestedTest` | `{package}/UserTest$NestedTest/` |

### Table Name Derivation

Table names are derived from filenames:

| Filename | Table Name |
|----------|------------|
| `USERS.csv` | `USERS` |
| `order_items.csv` | `order_items` |
| `CamelCase.csv` | `CamelCase` |

Case sensitivity depends on the database configuration.

## Load Order

### Overview

The `load-order.txt` file controls the order in which tables are processed during database operations. This is important for tables with foreign key relationships where parent tables must be populated before child tables.

### File Location

The load order file is located in the dataset directory:

```
src/test/resources/
└── {package}/
    └── {TestClassName}/
        ├── load-order.txt    # Load order specification
        ├── PARENT_TABLE.csv
        └── CHILD_TABLE.csv
```

### File Format

The `load-order.txt` file uses a line-based format:

| Element | Description |
|---------|-------------|
| Table name | One table name per line (without file extension) |
| Comments | Lines starting with `#` are ignored |
| Empty lines | Ignored |
| Whitespace | Leading and trailing whitespace is trimmed |

### Example

File: `load-order.txt`

```
# Parent tables first
USERS
CATEGORIES

# Child tables after their parents
ORDERS
ORDER_ITEMS
```

### Default Behavior

When `load-order.txt` does not exist in the dataset directory:

1. Tables are sorted alphabetically by filename
2. The framework does not automatically generate the file

To explicitly require the load order file, use:

```java
@DataSet(tableOrdering = TableOrderingStrategy.LOAD_ORDER_FILE)
```

This throws a `DataSetLoadException` if `load-order.txt` is not found.

### Processing Order by Operation

The table ordering interacts with database operations as follows:

| Operation | Processing Order |
|-----------|------------------|
| INSERT | Tables processed in file order (top to bottom) |
| DELETE, DELETE_ALL | Tables processed in reverse file order (bottom to top) |
| TRUNCATE_TABLE | Tables processed in reverse file order |
| CLEAN_INSERT | DELETE in reverse order, then INSERT in forward order |
| TRUNCATE_INSERT | TRUNCATE in reverse order, then INSERT in forward order |

### Relationship with TableOrderingStrategy

The `TableOrderingStrategy` enum controls how table ordering is determined. See [Database Operations](06-database-operations#table-ordering-strategy) for full details.

| Strategy | Behavior |
|----------|----------|
| `AUTO` (default) | Use `load-order.txt` if exists, then FK metadata, then alphabetical |
| `LOAD_ORDER_FILE` | Require `load-order.txt` (error if not found) |
| `FOREIGN_KEY` | Use JDBC metadata for FK-based ordering |
| `ALPHABETICAL` | Sort tables alphabetically by name |

### Best Practices

1. **Commit the ordering file**: Include `load-order.txt` in version control for reproducible tests
2. **Parent tables first**: List parent tables before child tables to satisfy foreign key constraints
3. **Use comments**: Document the reasoning for non-obvious ordering decisions
4. **Consider FK strategy**: For databases with proper FK constraints, `TableOrderingStrategy.FOREIGN_KEY` provides automatic ordering without manual file maintenance

### Error Handling

| Error | Exception |
|-------|-----------|
| Cannot read ordering file | `DataSetLoadException` |
| File required but not found (`LOAD_ORDER_FILE` strategy) | `DataSetLoadException` |

## Parsing Rules

### CSV Parsing

Follows RFC 4180 with extensions:

| Rule | Description |
|------|-------------|
| Delimiter | Comma (`,`) |
| Quote character | Double quote (`"`) |
| Escape sequence | `""` for embedded quotes |
| Newline handling | CRLF and LF supported |
| Leading/trailing whitespace | Preserved unless quoted |

### TSV Parsing

| Rule | Description |
|------|-------------|
| Delimiter | Tab (`\t`) |
| Quote character | Double quote (`"`) |
| Escape sequence | `""` for embedded quotes |
| Newline handling | CRLF and LF supported |

### Header Row Requirements

- First row must contain column names
- Column names must be unique within a table
- Empty column names are not permitted
- Scenario marker column is optional

### Data Type Handling

All values are parsed as strings and converted during database operations:

| Database Type | String Conversion |
|---------------|-------------------|
| INTEGER, BIGINT | Parse as integer |
| DECIMAL, NUMERIC | Parse as BigDecimal |
| VARCHAR, TEXT | Use as-is |
| DATE | Parse ISO format (YYYY-MM-DD) |
| TIMESTAMP | Parse ISO format (YYYY-MM-DD HH:MM:SS) |
| BOOLEAN | Parse "true" or "false" (case-insensitive) |
| BLOB | Base64 decode |
| CLOB | Use as-is |

### Encoding

- File encoding: UTF-8
- BOM (Byte Order Mark): Supported but optional

### Error Handling

| Error | Behavior |
|-------|----------|
| Missing file | `DataSetLoadException` |
| Invalid format | `DataSetLoadException` with line number |
| Mismatched column count | `DataSetLoadException` |
| Parse error | `DataSetLoadException` with details |

## Related Specifications

- [Overview](01-overview) - Framework purpose and key concepts
- [Configuration](04-configuration) - DataFormat and ConventionSettings
- [Database Operations](06-database-operations) - Table ordering and operations
- [Public API](03-public-api) - Annotation attributes
- [Error Handling](09-error-handling) - Dataset load errors
