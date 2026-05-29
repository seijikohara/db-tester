---
title: "Data Formats - DB Tester"
description: "Reference for AUTO format detection, CSV, TSV, JSON, and YAML data formats, value syntax, and special handling."
---

# DB Tester Specification - Data Formats

## Supported Formats

The framework supports automatic format detection and four data formats:
two delimited text formats (CSV, TSV) and two structured formats (JSON, YAML).

| Format | Extension | Delimiter | Default |
|--------|-----------|-----------|---------|
| AUTO | All supported | -- | Yes |
| CSV | `.csv` | Comma (`,`) | No |
| TSV | `.tsv` | Tab (`\t`) | No |
| JSON | `.json` | -- | No |
| YAML | `.yaml` | -- | No |

### Format Selection

Configure the format in `ConventionSettings`:

```java
var conventions = ConventionSettings.builder()
    .dataFormat(DataFormat.TSV)
    .build();
```

When using a concrete format (CSV, TSV, JSON, or YAML), the framework processes only files
matching the configured extension. When using `AUTO` (the default), the framework processes
all supported file extensions.

## Automatic Format Detection

`DataFormat.AUTO` is the default format. It detects and loads all supported file formats
(CSV, TSV, JSON, and YAML) from a dataset directory.

### Behavior

When `AUTO` is active:

1. The framework scans the dataset directory for files with any supported extension
   (`.csv`, `.tsv`, `.json`, `.yaml`)
2. Each file is parsed according to its extension
3. Table names derive from filenames without extensions, as with concrete formats

### Mixed Format Loading

A single dataset directory can contain files in different formats:

```
src/test/resources/com/example/UserRepositoryTest/
├── USERS.csv
├── ORDERS.json
├── CATEGORIES.yaml
└── AUDIT_LOG.tsv
```

Each file is parsed using the format parser matching its extension.

### Table Name Conflict Detection

If the same table name appears in multiple file formats, the framework throws a
`DataSetLoadException` to prevent ambiguous dataset definitions.

**Example conflict**:

```
src/test/resources/com/example/UserRepositoryTest/
├── USERS.csv
└── USERS.yaml
```

**Error message**:

```text
Table name conflict detected in AUTO format mode.
The following table names are defined in multiple files with different formats:

  Table 'USERS':
    - USERS.csv
    - USERS.yaml

Each table name must be unique across all file formats in a directory.
To resolve, remove duplicate files or specify a concrete format:
  DataFormat.CSV, DataFormat.TSV, DataFormat.JSON, or DataFormat.YAML
```

### Export Restriction

`DataSetExporter` does not support `DataFormat.AUTO` for export operations.
Specify a concrete format (CSV, TSV, JSON, or YAML) when exporting.
Using `AUTO` throws `IllegalArgumentException`.

### API Details

| Method | AUTO Behavior |
|--------|---------------|
| `hasExtension()` | Returns `false` |
| `getExtension()` | Throws `UnsupportedOperationException` |

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

### Example JSON

File: `USERS.json`

```json
[
  {"id": 1, "name": "Alice", "email": "alice@example.com", "created_at": "2024-01-01 00:00:00"},
  {"id": 2, "name": "Bob", "email": "bob@example.com", "created_at": "2024-01-02 00:00:00"}
]
```

Each JSON file contains an array of objects.
Each object represents a row, with key-value pairs representing column-value mappings.

### Example YAML

File: `USERS.yaml`

```yaml
- id: 1
  name: Alice
  email: alice@example.com
  created_at: "2024-01-01 00:00:00"
- id: 2
  name: Bob
  email: bob@example.com
  created_at: "2024-01-02 00:00:00"
```

Each YAML file contains a list of mappings.
Each mapping represents a row, with key-value pairs representing column-value mappings.

## Scenario Filtering

### Scenario Marker Column

The scenario marker column enables multiple test methods to share a dataset file:

| Column Name | Configurable | Default |
|-------------|--------------|---------|
| `[Scenario]` | Yes | `[Scenario]` |

### Scenario Column Behavior

When a dataset file contains the scenario marker column, the framework performs these steps:

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

The scenario name resolves in this order:

1. Explicit `scenarioNames` in `@DataSetSource` annotation
2. Test method name (via `ScenarioNameResolver` SPI)

### Multiple Scenarios

A single test can use multiple scenarios:

```java
@DataSet(sources = @DataSetSource(scenarioNames = {"scenario1", "scenario2"}))
void testMultipleScenarios() { }
```

The framework includes rows matching any of the specified scenarios.

### Scenario Filtering in JSON

Include the scenario marker in each JSON object (any key position is supported;
first key is recommended):

```json
[
  {"[Scenario]": "testCreate", "id": 1, "name": "Alice", "email": "alice@example.com"},
  {"[Scenario]": "testCreate", "id": 2, "name": "Bob", "email": "bob@example.com"},
  {"[Scenario]": "testUpdate", "id": 3, "name": "Charlie", "email": "charlie@example.com"},
  {"[Scenario]": "testDelete", "id": 4, "name": "Diana", "email": "diana@example.com"}
]
```

For test method `testCreate`, the framework filters to rows with `id` 1 and 2.

### Scenario Filtering in YAML

Include the scenario marker in each YAML mapping (any key position is supported;
first key is recommended):

```yaml
- "[Scenario]": testCreate
  id: 1
  name: Alice
  email: alice@example.com
- "[Scenario]": testCreate
  id: 2
  name: Bob
  email: bob@example.com
- "[Scenario]": testUpdate
  id: 3
  name: Charlie
  email: charlie@example.com
- "[Scenario]": testDelete
  id: 4
  name: Diana
  email: diana@example.com
```

For test method `testCreate`, the framework filters to rows with `id` 1 and 2.

### Empty Scenario Values

Rows with empty, blank, or null scenario values are always included regardless of the
active scenario filter. This applies to all formats (CSV, TSV, JSON, YAML).

This behavior is useful for common reference data that every scenario requires.

#### CSV

```csv
[Scenario],id,name
testCreate,1,Alice
,2,Bob
testCreate,3,Charlie
```

#### JSON

```json
[
  {"[Scenario]": "testCreate", "id": 1, "name": "Alice"},
  {"[Scenario]": "", "id": 2, "name": "Bob"},
  {"[Scenario]": "testCreate", "id": 3, "name": "Charlie"}
]
```

#### YAML

```yaml
- "[Scenario]": testCreate
  id: 1
  name: Alice
- "[Scenario]": ""
  id: 2
  name: Bob
- "[Scenario]": testCreate
  id: 3
  name: Charlie
```

For test method `testCreate`, rows 1, 2, and 3 are all included in every format:

- Row 1 and 3: match the scenario name `testCreate`
- Row 2: included because the scenario value is empty (shared data)

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

The delimited formats (CSV, TSV) cannot express a non-null empty string. Both an empty field and a
quoted empty field materialize as SQL NULL:

| File Content | Interpretation |
|--------------|----------------|
| Empty field | NULL |
| Empty quoted field (`""`) | NULL |

Example:

```csv
id,nullable_col,quoted_empty_col
1,,""
```

- `nullable_col` = NULL
- `quoted_empty_col` = NULL

To distinguish an empty string from NULL, use JSON or YAML. These formats provide distinct syntax
for `null` and `""`:

```json
[
  {"id": 1, "nullable_col": null, "empty_string_col": ""}
]
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

Table names derive from filenames:

| Filename | Table Name |
|----------|------------|
| `USERS.csv` | `USERS` |
| `order_items.csv` | `order_items` |
| `CamelCase.csv` | `CamelCase` |

Case sensitivity depends on the database configuration.

## Load Order

### Load Order File

The `load-order.txt` file controls the order in which tables are processed during
database operations. This is important for tables with foreign key relationships
where parent tables must be populated before child tables.

### File Location

The load order file resides in the dataset directory:

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

```text
# Parent tables first
USERS
CATEGORIES

# Child tables after their parents
ORDERS
ORDER_ITEMS
```

### Default Behavior

When `load-order.txt` does not exist in the dataset directory:

1. Tables sort alphabetically by filename
2. The framework does not auto-generate the file

To require the load order file explicitly, use:

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

The `TableOrderingStrategy` enum controls how table ordering is determined.
See [Database Operations](database-operations#table-ordering-strategy) for full details.

| Strategy | Behavior |
|----------|----------|
| `AUTO` (default) | Use `load-order.txt` if it exists, then FK metadata, then alphabetical |
| `LOAD_ORDER_FILE` | Require `load-order.txt` (error if not found) |
| `FOREIGN_KEY` | Use JDBC metadata for FK-based ordering |
| `ALPHABETICAL` | Sort tables alphabetically by name |

### Best Practices

1. **Commit the ordering file**: Include `load-order.txt` in version control for reproducible tests
2. **Parent tables first**: List parent tables before child tables to satisfy foreign key constraints
3. **Use comments**: Document the reasoning for non-obvious ordering decisions
4. **Consider FK strategy**: For databases with FK constraints, `TableOrderingStrategy.FOREIGN_KEY`
   provides automatic ordering without manual file maintenance

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

### JSON Parsing

The framework uses Jackson `ObjectMapper` to parse JSON files.

| Rule | Description |
|------|-------------|
| Structure | Array of objects |
| Column order | Determined by key order of the first object |
| Null handling | JSON `null` maps to SQL NULL |
| Empty string handling | JSON `""` maps to a non-null empty string, distinct from NULL |
| Value conversion | All non-null values convert to strings |
| Scenario filtering | Supported; include the scenario marker in each object (any key position) |

### YAML Parsing

The framework uses Jackson YAML module (`YAMLMapper`) to parse YAML files.

| Rule | Description |
|------|-------------|
| Structure | List of mappings |
| Column order | Determined by key order of the first mapping |
| Null handling | YAML `null` or `~` maps to SQL NULL |
| Empty string handling | YAML `""` maps to a non-null empty string, distinct from NULL |
| Value conversion | All non-null values convert to strings |
| Comments | Supported and ignored during parsing |
| Scenario filtering | Supported; include the scenario marker in each mapping (any key position) |

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

## Template Expressions

Dataset values support template expressions that generate dynamic values at load time.
Expressions resolve during dataset parsing, before values are inserted into the database.

### Supported Expressions

| Expression | Description | Example Output |
|------------|-------------|----------------|
| `${uuid}` | Random UUID | `550e8400-e29b-41d4-a716-446655440000` |
| `${sequence:N}` | Set sequence counter to N and return N | `1` |
| `${sequence}` | Increment and return next sequence value | `2`, `3`, `4`, ... |
| `${now}` | Current timestamp in ISO-8601 format | `2024-01-15T10:30:00` |
| `${now+Xd}` | Relative future date (d=days, h=hours, m=minutes, s=seconds) | `2024-01-22T10:30:00` |
| `${now-Xd}` | Relative past date | `2024-01-08T10:30:00` |
| `${faker.xxx.yyy}` | Datafaker expression (optional dependency) | Varies |

### Example CSV

```csv
ID,NAME,EMAIL,CREATED_AT
${sequence:1},${faker.name.fullName},user_${sequence}@example.com,${now}
```

### Datafaker Integration

The `${faker.xxx.yyy}` template requires [Datafaker](https://www.datafaker.net/) as a
runtime dependency:

```kotlin
testRuntimeOnly("net.datafaker:datafaker:VERSION")
```

If Datafaker is not on the classpath, `${faker....}` expressions remain unprocessed.

## Convention-Based vs Explicit Paths

### Convention-Based (Default)

```java
@DataSet           // src/test/resources/com/example/MyTest/
@ExpectedDataSet   // src/test/resources/com/example/MyTest/expected/
```

### Explicit Resource Location

```java
@DataSet(sources = @DataSetSource(resourceLocation = "classpath:shared/common-data/"))
void testWithSharedData() { }
```

Explicit paths are useful for sharing datasets across test classes.

## Related Specifications

- [Overview](overview) - Framework purpose and key concepts
- [Configuration](configuration) - DataFormat and ConventionSettings
- [Database Operations](database-operations) - Table ordering and operations
- [Annotations](annotations) - Annotation attributes
- [Error Handling](error-handling) - Dataset load errors
