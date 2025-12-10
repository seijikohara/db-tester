# DB Tester Specification - Public API

This document describes the public API provided by the `db-tester-api` module.

---

## Table of Contents

1. [Annotations](#annotations)
2. [DataSet Interfaces](#dataset-interfaces)
3. [Domain Value Objects](#domain-value-objects)
4. [Exceptions](#exceptions)

---

## Annotations

### @Preparation

Declares datasets to be applied before a test method executes.

**Location**: `io.github.seijikohara.dbtester.api.annotation.Preparation`

**Target**: `METHOD`, `TYPE`

**Attributes**:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `dataSets` | `DataSet[]` | `{}` | Datasets to execute; empty triggers convention-based discovery |
| `operation` | `Operation` | `CLEAN_INSERT` | Database operation to apply |
| `tableOrdering` | `TableOrderingStrategy` | `AUTO` | Strategy for determining table processing order |

**Annotation Inheritance**:

- Class-level annotations are inherited by subclasses
- Method-level annotations override class-level declarations
- Annotated with `@Inherited`

**Example**:

```java
@Preparation
void testMethod() { }

@Preparation(operation = Operation.INSERT)
void testWithInsertOnly() { }

@Preparation(tableOrdering = TableOrderingStrategy.FOREIGN_KEY)
void testWithForeignKeyOrdering() { }

@Preparation(dataSets = @DataSet(resourceLocation = "custom/path"))
void testWithCustomPath() { }
```

---

### @Expectation

Declares datasets that define the expected database state after test execution.

**Location**: `io.github.seijikohara.dbtester.api.annotation.Expectation`

**Target**: `METHOD`, `TYPE`

**Attributes**:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `dataSets` | `DataSet[]` | `{}` | Datasets for verification; empty triggers convention-based discovery |
| `tableOrdering` | `TableOrderingStrategy` | `AUTO` | Strategy for determining table processing order during verification |

**Verification Behavior**:

- Read-only comparison (no data modification)
- Validates actual database state against expected datasets
- Assertion failures reported via test framework

**Example**:

```java
@Preparation
@Expectation
void testWithVerification() { }

@Expectation(dataSets = @DataSet(resourceLocation = "expected/custom"))
void testWithCustomExpectation() { }

@Expectation(tableOrdering = TableOrderingStrategy.ALPHABETICAL)
void testWithAlphabeticalOrdering() { }
```

---

### @DataSet

Configures individual dataset parameters within `@Preparation` or `@Expectation`.

**Location**: `io.github.seijikohara.dbtester.api.annotation.DataSet`

**Target**: Used within annotation arrays only

**Attributes**:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `resourceLocation` | `String` | `""` | Dataset directory path; empty uses convention-based discovery |
| `dataSourceName` | `String` | `""` | Named DataSource identifier; empty uses default |
| `scenarioNames` | `String[]` | `{}` | Scenario filters; empty uses test method name |

**Resource Location Formats**:

| Format | Example | Resolution |
|--------|---------|------------|
| Classpath relative | `data/users` | From test classpath root |
| Classpath prefix | `classpath:data/users` | Explicit classpath resolution |
| Absolute path | `/tmp/testdata` | File system absolute path |
| Empty string | `""` | Convention-based discovery |

**Example**:

```java
@Preparation(dataSets = {
    @DataSet(dataSourceName = "primary"),
    @DataSet(dataSourceName = "secondary", resourceLocation = "secondary-data")
})
void testMultipleDataSources() { }

@Preparation(dataSets = @DataSet(scenarioNames = {"scenario1", "scenario2"}))
void testMultipleScenarios() { }
```

---

## DataSet Interfaces

### DataSet

Represents a logical collection of database tables.

**Location**: `io.github.seijikohara.dbtester.api.dataset.DataSet`

**Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getTables()` | `List<Table>` | Returns immutable list of tables in declaration order |
| `getTable(TableName)` | `Optional<Table>` | Finds table by name |
| `getDataSource()` | `Optional<DataSource>` | Returns bound DataSource if specified |

**Guarantees**:

- Table order is preserved (insertion order)
- All returned collections are immutable
- Table names are unique within a dataset

---

### Table

Represents the structure and data of a database table.

**Location**: `io.github.seijikohara.dbtester.api.dataset.Table`

**Methods**:

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

---

### Row

Represents a single database record.

**Location**: `io.github.seijikohara.dbtester.api.dataset.Row`

**Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getValues()` | `Map<ColumnName, CellValue>` | Returns immutable column-value mapping |
| `getValue(ColumnName)` | `CellValue` | Returns value for column; `CellValue.NULL` if absent |

---

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

---

### TableName

Immutable identifier for a database table.

**Location**: `io.github.seijikohara.dbtester.api.domain.TableName`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `value` | `String` | Table name string |

---

### ColumnName

Immutable identifier for a table column.

**Location**: `io.github.seijikohara.dbtester.api.domain.ColumnName`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `value` | `String` | Column name string |

---

### DataSourceName

Immutable identifier for a registered DataSource.

**Location**: `io.github.seijikohara.dbtester.api.domain.DataSourceName`

**Type**: `record`

**Fields**:

| Field | Type | Description |
|-------|------|-------------|
| `value` | `String` | DataSource name string |

---

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
| `TIMESTAMP_FLEXIBLE` | Ignores sub-second precision and timezone |
| `NOT_NULL` | Verifies value is not null |

**Factory Methods**:

| Method | Description |
|--------|-------------|
| `regex(String)` | Creates regex pattern matcher |

**Comparison Behavior**:

| Strategy | null/null | null/value | value/null | value/value |
|----------|-----------|------------|------------|-------------|
| `STRICT` | true | false | false | equals() |
| `IGNORE` | true | true | true | true |
| `NUMERIC` | true | false | false | BigDecimal comparison |
| `CASE_INSENSITIVE` | true | false | false | equalsIgnoreCase() |
| `TIMESTAMP_FLEXIBLE` | true | false | false | Normalized comparison |
| `NOT_NULL` | false | false | false | true |
| `REGEX` | false | false | false | Pattern.matches() |

---

## Exceptions

All exceptions extend `DatabaseTesterException`.

### Exception Hierarchy

```
DatabaseTesterException (base)
├── ConfigurationException
├── DataSetLoadException
├── DataSourceNotFoundException
├── DatabaseOperationException
└── ValidationException
```

### DatabaseTesterException

Base exception for all framework errors.

**Location**: `io.github.seijikohara.dbtester.api.exception.DatabaseTesterException`

**Constructors**:

| Constructor | Description |
|-------------|-------------|
| `DatabaseTesterException(String)` | Message only |
| `DatabaseTesterException(String, Throwable)` | Message with cause |

---

### ConfigurationException

Indicates invalid framework configuration.

**Typical Causes**:

- Missing required configuration values
- Invalid file paths
- Incompatible settings combination

---

### DataSetLoadException

Indicates failure to load dataset files.

**Typical Causes**:

- File not found
- Invalid file format
- Parse errors in CSV/TSV content

---

### DataSourceNotFoundException

Indicates requested DataSource is not registered.

**Typical Causes**:

- Named DataSource not registered in `DataSourceRegistry`
- Default DataSource not set when required

---

### DatabaseOperationException

Indicates database operation failure.

**Typical Causes**:

- SQL execution errors
- Constraint violations
- Connection failures

---

### ValidationException

Indicates assertion or validation failure.

**Typical Causes**:

- Expected/actual data mismatch
- Row count differences
- Column value mismatches

**Output Format**: Validation errors output a human-readable summary followed by YAML details. See [Error Handling - Validation Errors](09-ERROR-HANDLING.md#validation-errors) for format details.

---

## Related Specifications

- [Overview](01-OVERVIEW.md) - Framework introduction
- [Configuration](04-CONFIGURATION.md) - Configuration classes
- [Database Operations](06-DATABASE-OPERATIONS.md) - Operation enum details
