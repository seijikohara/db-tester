---
title: "SPI Providers - DB Tester"
description: "Reference for DB Tester SPI provider interfaces: OperationProvider, AssertionProvider, ExpectationProvider, and more."
---

# SPI Providers

## API Module SPIs -- Provider Layer

### DataSetLoaderProvider

Provides the default `DataSetLoader` implementation.

**Location**: `io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider`

**Interface**:

```java
public interface DataSetLoaderProvider {
    DataSetLoader loader();
}
```

**Default Implementation**: `DefaultDataSetLoaderProvider` in `db-tester-core`

**Usage**: `Configuration.defaults()` calls this provider to obtain the loader.

### OperationProvider

Executes database operations on datasets.

**Location**: `io.github.seijikohara.dbtester.api.spi.OperationProvider`

**Interface**:

```java
public interface OperationProvider {
    void execute(
        Operation operation,
        TableSet tableSet,
        DataSource dataSource,
        TableOrderingStrategy tableOrderingStrategy,
        TransactionMode transactionMode,
        @Nullable Duration queryTimeout);

    // With batch size control (default delegates to base execute)
    default void execute(
        Operation operation,
        TableSet tableSet,
        DataSource dataSource,
        TableOrderingStrategy tableOrderingStrategy,
        TransactionMode transactionMode,
        @Nullable Duration queryTimeout,
        int batchSize);
}
```

**Default Implementation**: `DefaultOperationProvider` in `db-tester-core`

**Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `operation` | `Operation` | The database operation to execute |
| `tableSet` | `TableSet` | The table set containing tables and rows |
| `dataSource` | `DataSource` | The JDBC data source for connections |
| `tableOrderingStrategy` | `TableOrderingStrategy` | Strategy for table processing order |
| `transactionMode` | `TransactionMode` | Transaction behavior mode |
| `queryTimeout` | `@Nullable Duration` | Query timeout, or null for no timeout |
| `batchSize` | `int` | Rows per INSERT batch (0 = single batch), used by the batch overload |

**Operations**:

| Operation | Description |
|-----------|-------------|
| `NONE` | No operation |
| `INSERT` | Insert rows |
| `UPDATE` | Update by primary key |
| `DELETE` | Delete by primary key |
| `DELETE_ALL` | Delete all rows |
| `UPSERT` | Upsert (insert or update) |
| `TRUNCATE_TABLE` | Truncate tables |
| `CLEAN_INSERT` | Delete all then insert |
| `TRUNCATE_INSERT` | Truncate then insert |

### AssertionProvider

Performs database assertions for expectation verification.

**Location**: `io.github.seijikohara.dbtester.api.spi.AssertionProvider`

**Interface**:

```java
public interface AssertionProvider {
    // Core comparison methods
    void assertEquals(TableSet expected, TableSet actual);
    void assertEquals(TableSet expected, TableSet actual,
                      @Nullable AssertionFailureHandler failureHandler);
    void assertEquals(Table expected, Table actual);
    void assertEquals(Table expected, Table actual, Collection<String> additionalColumnNames);
    void assertEquals(Table expected, Table actual,
                      @Nullable AssertionFailureHandler failureHandler);

    // Comparison with column exclusion
    void assertEqualsIgnoreColumns(TableSet expected, TableSet actual, String tableName,
                                   Collection<String> ignoreColumnNames);
    void assertEqualsIgnoreColumns(Table expected, Table actual,
                                   Collection<String> ignoreColumnNames);

    // Comparison with column strategies
    void assertEqualsWithStrategies(Table expected, Table actual,
                                    Collection<ColumnStrategyMapping> columnStrategies);
}
```

**Default Implementation**: `DefaultAssertionProvider` in `db-tester-core`

**Key Methods**:

| Method | Description |
|--------|-------------|
| `assertEquals(TableSet, TableSet)` | Compare two table sets |
| `assertEquals(Table, Table)` | Compare two tables |
| `assertEqualsIgnoreColumns(...)` | Compare while ignoring specific columns |
| `assertEqualsWithStrategies(...)` | Compare with column-specific comparison strategies |

**Behavior**:
1. The provider compares expected and actual datasets or tables.
2. The provider applies comparison strategies per column (STRICT, IGNORE, NUMERIC, and others).
3. The provider collects all differences without fail-fast behavior.
4. The provider outputs a human-readable summary with YAML details on mismatch.

See [Error Handling - Validation Errors](error-handling#validation-errors) for output format details.

### ExpectationProvider

Verifies database state against expected datasets.

**Location**: `io.github.seijikohara.dbtester.api.spi.ExpectationProvider`

**Interface**:

```java
public interface ExpectationProvider {
    // Basic verification (abstract)
    void verifyExpectation(TableSet expectedTableSet, DataSource dataSource);

    // With ExpectationContext parameter object (default)
    default void verifyExpectation(TableSet expectedTableSet, DataSource dataSource,
                                   ExpectationContext context);
}
```

**Default Implementation**: `DefaultExpectationProvider` in `db-tester-core`

**Methods**:

| Method | Description |
|--------|-------------|
| `verifyExpectation(TableSet, DataSource)` | Basic database state verification |
| `verifyExpectation(TableSet, DataSource, ExpectationContext)` | Verify with full context (exclusions, strategies, ordering, defaults) |

**ExpectationContext** (`io.github.seijikohara.dbtester.api.config.ExpectationContext`):

A parameter object that encapsulates all optional verification parameters:

| Field | Type | Description |
|-------|------|-------------|
| `excludeColumns` | `Set<String>` | Column names to exclude from comparison (case-insensitive) |
| `columnStrategies` | `Map<String, ColumnStrategyMapping>` | Column comparison strategies keyed by column name |
| `rowOrdering` | `RowOrdering` | Row comparison strategy (ORDERED or UNORDERED) |
| `operationDefaults` | `OperationDefaults` | Operation defaults containing comparison settings (floating-point epsilon) |
| `tableOrdering` | `TableOrderingStrategy` | Table processing order (`AUTO` or `FOREIGN_KEY`) |

```java
// Default context (no exclusions, ordered, standard defaults, AUTO table ordering)
var context = ExpectationContext.defaults();

// Custom context using with*() copy methods
var context = ExpectationContext.defaults()
    .withExcludeColumns(Set.of("CREATED_AT", "UPDATED_AT"))
    .withRowOrdering(RowOrdering.UNORDERED)
    .withTableOrdering(TableOrderingStrategy.FOREIGN_KEY);

// Factory method with 4 parameters (table ordering defaults to AUTO)
var context = ExpectationContext.of(
    excludeColumns, columnStrategies, rowOrdering, operationDefaults);

// Factory method with 5 parameters (explicit table ordering)
var context = ExpectationContext.of(
    excludeColumns, columnStrategies, rowOrdering, operationDefaults,
    TableOrderingStrategy.FOREIGN_KEY);
```

**Process**:
1. The provider iterates each table in the expected dataset and fetches actual data.
2. The provider filters actual data to include only columns present in the expected table.
3. The provider applies column exclusions and comparison strategies from the context.
4. The provider compares filtered actual data against expected data.
5. The provider throws `ValidationException` (wrapping `AssertionError`) if verification fails.

### ScenarioNameResolver

Resolves scenario names from test method context.

**Location**: `io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver`

**Interface**:

```java
public interface ScenarioNameResolver {
    int DEFAULT_PRIORITY = 0;

    ScenarioName resolve(Method testMethod);

    default boolean canResolve(Method testMethod) {
        return true;
    }

    default int priority() {
        return DEFAULT_PRIORITY;
    }
}
```

**Methods**:

| Method | Return Type | Default | Description |
|--------|-------------|---------|-------------|
| `resolve(Method)` | `ScenarioName` | - | Resolves scenario name from test method |
| `canResolve(Method)` | `boolean` | `true` | Returns whether this resolver handles the method |
| `priority()` | `int` | `0` | Returns priority for resolver selection (higher = preferred) |

**Implementations**:

| Implementation | Module | Description |
|----------------|--------|-------------|
| `JUnitScenarioNameResolver` | `db-tester-junit` | Resolves from JUnit method name |
| `SpockScenarioNameResolver` | `db-tester-spock` | Resolves from Spock feature name |
| `KotestScenarioNameResolver` | `db-tester-kotest` | Resolves from Kotest test case name |

**Resolution Logic**:
1. The framework sorts all registered resolvers by `priority()` in descending order.
2. The framework queries each resolver via `canResolve()`.
3. The framework selects the first resolver that returns `true`.
4. The framework calls `resolve()` to obtain the scenario name.

### ExportProvider

Exports database content to files in specific formats.

**Location**: `io.github.seijikohara.dbtester.api.spi.ExportProvider`

**Interface**:

```java
public interface ExportProvider {
    DataFormat supportedFormat();
    void export(DataSource dataSource, List<String> tableNames,
                Path outputDirectory, ExportConfiguration config);
    void exportQuery(DataSource dataSource, String query, String tableName,
                     Path outputDirectory, ExportConfiguration config);
}
```

**Methods**:

| Method | Description |
|--------|-------------|
| `supportedFormat()` | Returns the data format this provider handles |
| `export(...)` | Exports specified tables to files in the output directory |
| `exportQuery(...)` | Exports a SQL query result to a file |

**Selection**: The framework selects the provider whose `supportedFormat()` matches
the configured `DataFormat`.

### QueryAssertionProvider

Executes SQL queries and compares results with expected datasets.

**Location**: `io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider`

**Interface**:

```java
public interface QueryAssertionProvider {
    void assertEqualsByQuery(TableSet expected, DataSource dataSource,
                             String tableName, String sqlQuery,
                             Collection<String> ignoreColumnNames);
    void assertEqualsByQuery(Table expected, DataSource dataSource,
                             String tableName, String sqlQuery,
                             Collection<String> ignoreColumnNames);
}
```

**Default Implementation**: `DefaultQueryAssertionProvider` in `db-tester-core`

**Loaded by**: `DatabaseQueryAssertion` facade class

**Difference from `AssertionProvider`**: `AssertionProvider` compares in-memory datasets.
`QueryAssertionProvider` executes SQL queries against the database and compares the results.

### TypeHandler

Handles custom database type conversion for reading, writing, and formatting values.

**Location**: `io.github.seijikohara.dbtester.api.spi.TypeHandler`

**Interface**:

```java
public interface TypeHandler<T> {
    Class<T> javaType();
    List<Integer> sqlTypes();
    default List<String> supportedDatabases();
    default int priority();
    T read(ResultSet resultSet, int columnIndex) throws SQLException;
    void write(PreparedStatement ps, int parameterIndex, T value) throws SQLException;
    String format(T value);
    T parse(String value);
}
```

**Methods**:

| Method | Description |
|--------|-------------|
| `javaType()` | Returns the Java type this handler produces |
| `sqlTypes()` | Returns SQL type codes (`java.sql.Types`) this handler supports |
| `supportedDatabases()` | Returns database product names, or empty list for all databases |
| `priority()` | Priority for handler selection (higher = preferred); default 0 |
| `read(...)` | Reads a value from a `ResultSet` |
| `write(...)` | Writes a value to a `PreparedStatement` |
| `format(...)` | Converts a value to string for export |
| `parse(...)` | Parses a string value from import |

**Selection**: When multiple handlers support the same SQL type, the handler with the
highest `priority()` is selected. Database-specific handlers (non-empty
`supportedDatabases()`) take precedence over generic handlers when the database
product name matches.

## Core Module SPIs

### FormatProvider

Parses dataset files in specific formats.

**Location**: `io.github.seijikohara.dbtester.internal.format.spi.FormatProvider`

**Interface**:

```java
public interface FormatProvider {
    FileExtension supportedFileExtension();
    TableSet parse(Path directory);
}
```

**Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `supportedFileExtension()` | `FileExtension` | Returns the file extension without leading dot (for example, "csv") |
| `parse(Path)` | `TableSet` | Parses all files in directory into a TableSet |

**Implementations**:

| Implementation | Extension | Delimiter |
|----------------|-----------|-----------|
| `CsvFormatProvider` | `.csv` | Comma |
| `TsvFormatProvider` | `.tsv` | Tab |
| `JsonFormatProvider` | `.json` | JSON structure |
| `YamlFormatProvider` | `.yaml` | YAML structure |

This internal SPI is not part of the public API contract and may change without notice.

## Related Specifications

- [SPI Overview](spi) - Architecture and Support Layer
- [SPI Registration](spi-registration) - ServiceLoader and JPMS configuration
- [Programmatic API](assertion-api) - DatabaseAssertion, Export, and Preparation APIs
- [Configuration](configuration) - Configuration classes
