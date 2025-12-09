# DB Tester Specification - Error Handling

This document describes the error handling and error output in the DB Tester framework.

---

## Table of Contents

1. [Exception Hierarchy](#exception-hierarchy)
2. [Validation Errors](#validation-errors)
3. [Dataset Load Errors](#dataset-load-errors)
4. [DataSource Errors](#datasource-errors)
5. [Database Operation Errors](#database-operation-errors)
6. [Configuration Errors](#configuration-errors)

---

## Exception Hierarchy

All framework exceptions extend `DatabaseTesterException`:

```
RuntimeException
└── DatabaseTesterException
    ├── ValidationException
    ├── DataSetLoadException
    ├── DataSourceNotFoundException
    ├── DatabaseOperationException
    └── ConfigurationException
```

**Location**: `io.github.seijikohara.dbtester.api.exception`

| Exception | Cause |
|-----------|-------|
| `ValidationException` | Expected vs actual data mismatch |
| `DataSetLoadException` | Dataset file read/parse failure |
| `DataSourceNotFoundException` | DataSource not registered |
| `DatabaseOperationException` | SQL execution failure |
| `ConfigurationException` | Framework initialization failure |

---

## Validation Errors

Thrown when expectation verification fails (`@Expectation` phase).

### Table Count Mismatch

When expected and actual table counts differ:

```
Table count mismatch: expected 2 tables, but got 1
```

### Table Not Found

When an expected table does not exist in actual data:

```
Table not found: USERS
```

### Row Count Mismatch

When a table has different row counts:

```
Row count mismatch in table 'USERS': expected 3 rows, but got 2
```

### Value Mismatch

When a cell value differs (most detailed error):

```
Value mismatch in table 'USERS', row 0, column 'NAME': expected 'Alice', but got 'Bob'
```

**Format**: `Value mismatch in table '{table}', row {index}, column '{column}': expected '{expected}', but got '{actual}'`

### Value Comparison Rules

The comparator applies the following rules before reporting mismatches:

| Rule | Description |
|------|-------------|
| NULL handling | Both NULL = match, one NULL = mismatch |
| Numeric comparison | String "123" matches Integer 123 |
| Floating point | Epsilon comparison (precision 1e-6) |
| Boolean | "1"/"0"/"true"/"false"/"yes"/"no"/"y"/"n" supported |
| Timestamp precision | "2024-01-01 10:00:00" matches "2024-01-01 10:00:00.0" |
| CLOB | Compared as string |

---

## Dataset Load Errors

Thrown when dataset files cannot be loaded or parsed.

### Directory Not Found (Classpath)

When dataset directory does not exist on classpath:

```
Dataset directory not found on classpath: 'com/example/UserRepositoryTest'
Expected location: src/test/resources/com/example/UserRepositoryTest
Hint: Create the directory and add dataset files...
```

### Directory Not Found (Filesystem)

When dataset directory does not exist on filesystem:

```
Dataset directory does not exist: '/path/to/datasets'
Hint: Create the directory and add dataset files...
```

### Path Is Not Directory

When the path exists but is a file:

```
Path exists but is not a directory: '/path/to/file.csv'
Hint: Ensure the path points to a directory, not a file.
```

### No Supported Files

When directory exists but contains no supported data files:

```
Dataset directory exists but contains no supported data files: '/path/to/datasets'
Supported file extensions: .csv, .tsv
Hint: Add at least one data file (for example, TABLE_NAME.csv)...
```

### Empty File

When a data file is empty:

```
File is empty: /path/to/USERS.csv
```

### Parse Failure

When file parsing fails:

```
Failed to parse file: /path/to/USERS.csv
```

### Load Order File Error

When `load-order.txt` file cannot be read or written:

```
Failed to read load order file: /path/to/load-order.txt
```

```
Failed to write load order file: /path/to/load-order.txt
```

For details about the load order file format and usage, see [Data Formats - Load Order](05-DATA-FORMATS.md#load-order).

---

## DataSource Errors

Thrown when DataSource lookup fails.

### Default DataSource Not Registered

When no default DataSource is registered:

```
No default data source registered
```

**Solution**: Register a default DataSource in `@BeforeAll` or `setupSpec()`:

```java
registry.registerDefault(dataSource);
```

### Named DataSource Not Found

When a named DataSource is not registered:

```
No data source registered for name: secondary_db
```

**Solution**: Register the named DataSource:

```java
registry.register("secondary_db", dataSource);
```

---

## Database Operation Errors

Thrown when SQL operations fail during preparation phase.

### Wrapped SQL Exception

Database operation errors wrap the underlying `SQLException`:

```
DatabaseOperationException: Failed to execute INSERT on table USERS
Caused by: SQLException: Duplicate entry '1' for key 'PRIMARY'
```

### Common Causes

| Error | Cause | Solution |
|-------|-------|----------|
| Duplicate key | INSERT with existing primary key | Use CLEAN_INSERT or REFRESH |
| Foreign key violation | INSERT child before parent | Check table ordering |
| Column not found | CSV column name typo | Verify column names match schema |
| Data truncation | Value exceeds column size | Check data fits column definition |

---

## Configuration Errors

Thrown during framework initialization.

### Invalid Configuration

When configuration values are invalid:

```
ConfigurationException: Invalid data format: XML
```

### Missing Required Setting

When a required setting is missing:

```
ConfigurationException: Convention settings cannot be null
```

---

## Error Context in Test Output

### JUnit Error Output

```
org.example.UserRepositoryTest > shouldCreateUser FAILED
    io.github.seijikohara.dbtester.api.exception.ValidationException:
        Value mismatch in table 'USERS', row 0, column 'EMAIL':
        expected 'john@example.com', but got 'jane@example.com'

        at io.github.seijikohara.dbtester.internal.assertion.DataSetComparator.compare(DataSetComparator.java:85)
        at io.github.seijikohara.dbtester.junit.jupiter.lifecycle.ExpectationVerifier.verify(ExpectationVerifier.java:42)
```

### Spock Error Output

```
example.UserRepositorySpec > should create user FAILED
    io.github.seijikohara.dbtester.api.exception.ValidationException:
        Row count mismatch in table 'USERS': expected 2 rows, but got 1

Condition not satisfied:
    Expectation verification failed
```

### Test Method Context

Errors include the test method name for context:

```
Failed to verify expectation dataset for testUserCreation
```

---

## Debugging Tips

| Symptom | Check |
|---------|-------|
| Table not found | Verify CSV filename matches table name (case-sensitive) |
| Row count mismatch | Check `[Scenario]` column filtering |
| Value mismatch | Compare expected CSV with actual database state |
| Directory not found | Verify path matches `{package}/{TestClassName}/` convention |
| DataSource not found | Ensure registration in `@BeforeAll` or `setupSpec()` |

### Logging

Enable DEBUG logging for detailed operation output:

```properties
logging.level.io.github.seijikohara.dbtester=DEBUG
```

---

## Related Specifications

- [Public API](03-PUBLIC-API.md) - Exception classes
- [Database Operations](06-DATABASE-OPERATIONS.md) - Operation failures
- [Test Frameworks](07-TEST-FRAMEWORKS.md) - Test lifecycle and error handling
