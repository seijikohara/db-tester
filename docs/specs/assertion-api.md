---
title: "Programmatic API - DB Tester"
description: "Reference for DB Tester programmatic APIs: DatabaseAssertion, DatabaseQueryAssertion, DataSetExporter, and DatabasePreparation."
---

# Programmatic API

## Assertion API

### DatabaseAssertion

Static facade for programmatic database assertions. This utility class delegates to the underlying assertion provider loaded via SPI.

**Location**: `io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion`

**Type**: Utility class (non-instantiable, static methods only)

**Static Methods**:

| Method | Description |
|--------|-------------|
| `assertEquals(TableSet, TableSet)` | Asserts two table sets are equal |
| `assertEquals(TableSet, TableSet, AssertionFailureHandler)` | Asserts with custom failure handler |
| `assertEquals(Table, Table)` | Asserts two tables are equal |
| `assertEquals(Table, Table, Collection<String>)` | Asserts tables with additional columns to include |
| `assertEquals(Table, Table, AssertionFailureHandler)` | Asserts tables with custom failure handler |
| `assertEqualsIgnoreColumns(TableSet, TableSet, String, Collection<String>)` | Asserts table in table sets, ignoring specified columns |
| `assertEqualsIgnoreColumns(Table, Table, Collection<String>)` | Asserts tables, ignoring specified columns |
| `assertEqualsWithStrategies(Table, Table, Collection<ColumnStrategyMapping>)` | Asserts tables with column-specific comparison strategies |

**Varargs Overloads**: Methods accepting `Collection<String>` for column names also have `String...` varargs overloads for convenience.

**Example**:

```java
// Basic table set comparison
DatabaseAssertion.assertEquals(expectedTableSet, actualTableSet);

// With custom failure handler
DatabaseAssertion.assertEquals(expectedTableSet, actualTableSet, (message, expected, actual) -> {
    // Custom failure handling
});

// Ignoring specific columns
DatabaseAssertion.assertEqualsIgnoreColumns(expectedTableSet, actualTableSet, "USERS", "CREATED_AT", "UPDATED_AT");

// Using column-specific comparison strategies
DatabaseAssertion.assertEqualsWithStrategies(expectedTable, actualTable,
    ColumnStrategyMapping.ignore("CREATED_AT"),
    ColumnStrategyMapping.caseInsensitive("EMAIL"),
    ColumnStrategyMapping.regex("TOKEN", "[a-f0-9-]{36}"));
```

### DatabaseQueryAssertion

Static facade for query-based database assertions. This utility class executes SQL queries and compares results with expected datasets. It separates query execution concerns from pure data comparison in `DatabaseAssertion`.

**Location**: `io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion`

**Type**: Utility class (non-instantiable, static methods only)

**Static Methods**:

| Method | Description |
|--------|-------------|
| `assertEqualsByQuery(TableSet, DataSource, String, String, Collection<String>)` | Asserts SQL query results against expected table set |
| `assertEqualsByQuery(Table, DataSource, String, String, Collection<String>)` | Asserts SQL query results against expected table |

**Varargs Overloads**: Methods accepting `Collection<String>` for column names also have `String...` varargs overloads for convenience.

**Example**:

```java
// Compare SQL query results against expected dataset
DatabaseQueryAssertion.assertEqualsByQuery(
    expectedTableSet, dataSource, "USERS",
    "SELECT * FROM USERS WHERE status = 'ACTIVE'");

// With columns to ignore
DatabaseQueryAssertion.assertEqualsByQuery(
    expectedTableSet, dataSource, "USERS",
    "SELECT * FROM USERS", "CREATED_AT", "UPDATED_AT");
```

### AssertionFailureHandler

Strategy interface for reacting to assertion mismatches. Implementations can translate individual failures into domain-specific actions such as raising custom exceptions, logging diagnostics, or aggregating differences.

**Location**: `io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler`

**Type**: `@FunctionalInterface`

**Methods**:

| Method | Description |
|--------|-------------|
| `handleFailure(String, @Nullable Object, @Nullable Object)` | Handles a comparison failure between expected and actual values |

**Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `message` | `String` | Descriptive failure message including context (table name, row number, column name) |
| `expected` | `@Nullable Object` | Expected value; may be null |
| `actual` | `@Nullable Object` | Actual value found in database; may be null |

**Example**:

```java
// Fail-fast strategy (default behavior)
AssertionFailureHandler failFast = (message, expected, actual) -> {
    throw new AssertionError(message);
};

// Collect all failures
List<String> failures = new ArrayList<>();
AssertionFailureHandler collector = (message, expected, actual) -> {
    failures.add(String.format("%s: expected=%s, actual=%s", message, expected, actual));
};

DatabaseAssertion.assertEquals(expectedTableSet, actualTableSet, collector);
if (!failures.isEmpty()) {
    throw new AssertionError("Multiple failures:\n" + String.join("\n", failures));
}
```

## Export API

### DataSetExporter

Static facade for exporting database content to files. This utility class delegates to format-specific implementations loaded via the `ExportProvider` SPI.

**Location**: `io.github.seijikohara.dbtester.api.export.DataSetExporter`

**Type**: Utility class (non-instantiable, static methods only)

**Static Methods**:

| Method | Description |
|--------|-------------|
| `export(DataSource, List<String>, Path, DataFormat)` | Exports tables to files in the specified format with default configuration; throws `IllegalArgumentException` for `AUTO` |
| `export(DataSource, List<String>, Path, DataFormat, ExportConfiguration)` | Exports tables to files with custom configuration; throws `IllegalArgumentException` for `AUTO` |
| `exportQuery(DataSource, String, String, Path, DataFormat)` | Exports SQL query results to a file with default configuration; throws `IllegalArgumentException` for `AUTO` |
| `exportQuery(DataSource, String, String, Path, DataFormat, ExportConfiguration)` | Exports SQL query results to a file with custom configuration; throws `IllegalArgumentException` for `AUTO` |
| `csv(DataSource, List<String>, Path)` | Exports tables to CSV files (convenience method) |
| `tsv(DataSource, List<String>, Path)` | Exports tables to TSV files (convenience method) |
| `json(DataSource, List<String>, Path)` | Exports tables to JSON files (convenience method) |
| `yaml(DataSource, List<String>, Path)` | Exports tables to YAML files (convenience method) |

**Example**:

```java
// Export tables to CSV files
DataSetExporter.csv(dataSource, List.of("USERS", "ORDERS"), Paths.get("export"));

// Export with custom configuration
var config = ExportConfiguration.builder()
    .lobHandling(LobHandling.OMIT)
    .writeLoadOrderFile(true)
    .build();
DataSetExporter.export(dataSource, List.of("USERS"), Paths.get("export"), DataFormat.JSON, config);

// Export query result
DataSetExporter.exportQuery(
    dataSource,
    "SELECT * FROM USERS WHERE active = true",
    "ACTIVE_USERS",
    Paths.get("export"),
    DataFormat.CSV);
```

### ExportConfiguration

Configuration for data export operations.

**Location**: `io.github.seijikohara.dbtester.api.export.ExportConfiguration`

**Factory Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `defaults()` | `ExportConfiguration` | Creates a configuration with default values |
| `builder()` | `Builder` | Creates a new builder for custom configuration |

**Configuration Properties**:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `nullValue` | `String` | `""` | String representation for null values in delimited formats |
| `dateFormatter` | `DateTimeFormatter` | `ISO_LOCAL_DATE` | Formatter for date values (`yyyy-MM-dd`) |
| `timeFormatter` | `DateTimeFormatter` | `ISO_LOCAL_TIME` | Formatter for time values (`HH:mm:ss`) |
| `timestampFormatter` | `DateTimeFormatter` | `yyyy-MM-dd HH:mm:ss` | Formatter for timestamp values |
| `lobHandling` | `LobHandling` | `BASE64` | Handling strategy for LOB columns |
| `writeLoadOrderFile` | `boolean` | `false` | Whether to generate a load order file |
| `loadOrderFileName` | `String` | `load-order.txt` | Name of the load order file |

**Example**:

```java
// Using defaults
var config = ExportConfiguration.defaults();

// Custom configuration
var config = ExportConfiguration.builder()
    .nullValue("NULL")
    .lobHandling(LobHandling.OMIT)
    .writeLoadOrderFile(true)
    .build();
```

### LobHandling

Enum defining how LOB (Large Object) columns are handled during export.

**Location**: `io.github.seijikohara.dbtester.api.export.LobHandling`

**Values**:

| Value | Description |
|-------|-------------|
| `BASE64` | Exports LOB values as Base64-encoded strings with `[BASE64]` prefix. Supports round-trip export and import. |
| `OMIT` | Excludes LOB columns from export. Use when binary data is not needed or to reduce file size. |

### ExportProvider (SPI)

SPI for implementing format-specific export logic.

**Location**: `io.github.seijikohara.dbtester.api.spi.ExportProvider`

**Type**: `interface`

**Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `supportedFormat()` | `DataFormat` | Returns the data format this provider handles |
| `export(DataSource, List<String>, Path, ExportConfiguration)` | `void` | Exports tables to files |
| `exportQuery(DataSource, String, String, Path, ExportConfiguration)` | `void` | Exports SQL query results to a file |

**Discovery**: Providers are discovered via `java.util.ServiceLoader`. Register implementations in `META-INF/services/io.github.seijikohara.dbtester.api.spi.ExportProvider`.

**Thread Safety**: Implementations must be thread-safe and stateless.

## Preparation API

### DatabasePreparation

Static facade for programmatic database preparation. This utility class delegates to the underlying operation provider loaded via SPI.

**Location**: `io.github.seijikohara.dbtester.api.preparation.DatabasePreparation`

**Type**: Utility class (non-instantiable, static methods only)

**Static Methods**:

| Method | Description |
|--------|-------------|
| `cleanInsert(DataSource, TableSet)` | Executes CLEAN_INSERT with standard configuration |
| `cleanInsert(DataSource, TableSet, PreparationConfig)` | Executes CLEAN_INSERT with custom configuration |
| `execute(DataSource, TableSet, Operation)` | Executes the specified operation with standard configuration |
| `execute(DataSource, TableSet, Operation, PreparationConfig)` | Executes the specified operation with custom configuration |

**Example**:

```java
// Build dataset programmatically
var users = Table.ofValues("USERS",
    List.of("ID", "NAME", "EMAIL"),
    List.of(
        List.of(1, "Alice", "alice@example.com"),
        List.of(2, "Bob", "bob@example.com")));

// Clean insert with standard defaults
DatabasePreparation.cleanInsert(dataSource, TableSet.of(users));

// Execute a specific operation
DatabasePreparation.execute(dataSource, TableSet.of(users), Operation.INSERT);

// Execute with custom configuration
var config = PreparationConfig.standard()
    .withTransactionMode(TransactionMode.AUTO_COMMIT)
    .withBatchSize(1000);
DatabasePreparation.execute(dataSource, TableSet.of(users), Operation.CLEAN_INSERT, config);
```

### PreparationConfig

Configuration record for programmatic database preparation operations. Use `standard()` to obtain an instance with default values, then use `with*()` methods to customize.

**Location**: `io.github.seijikohara.dbtester.api.preparation.PreparationConfig`

**Type**: `record`

**Factory Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `standard()` | `PreparationConfig` | Creates an instance with standard default values |

**Properties**:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `tableOrderingStrategy` | `TableOrderingStrategy` | `AUTO` | Strategy for determining table processing order |
| `transactionMode` | `TransactionMode` | `SINGLE_TRANSACTION` | Transaction behavior mode |
| `queryTimeout` | `@Nullable Duration` | `null` | Query timeout; null uses driver default |
| `batchSize` | `int` | `0` | Rows per batch; zero means single-batch execution |

**Immutable Copy Methods**:

| Method | Description |
|--------|-------------|
| `withTableOrderingStrategy(TableOrderingStrategy)` | Returns a new instance with the specified strategy |
| `withTransactionMode(TransactionMode)` | Returns a new instance with the specified mode |
| `withQueryTimeout(@Nullable Duration)` | Returns a new instance with the specified timeout |
| `withBatchSize(int)` | Returns a new instance with the specified batch size |

**Example**:

```java
// Standard defaults
var config = PreparationConfig.standard();

// Custom configuration
var config = PreparationConfig.standard()
    .withTableOrderingStrategy(TableOrderingStrategy.FOREIGN_KEY)
    .withTransactionMode(TransactionMode.AUTO_COMMIT)
    .withBatchSize(500);
```

## Related Specifications

- [API Overview](public-api) - API layers and module organization
- [Annotations](annotations) - @DataSet, @ExpectedDataSet, @ColumnStrategy
- [Dataset Interfaces](dataset-interfaces) - TableSet, Table, Row, and value objects
- [Exceptions](exceptions) - Exception hierarchy and default values
- [SPI](spi) - Service Provider Interface extension points
