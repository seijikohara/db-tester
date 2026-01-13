# DB Tester Specification - Configuration

This document describes the configuration classes and options available in the DB Tester framework.

## Configuration Class

Aggregates runtime configuration for the database testing extension.

**Location**: `io.github.seijikohara.dbtester.api.config.Configuration`

**Type**: `final class` with builder pattern

### Components

| Component | Type | Description |
|-----------|------|-------------|
| `conventions` | `ConventionSettings` | Dataset directory resolution rules |
| `operations` | `OperationDefaults` | Default database operations |
| `loader` | `DataSetLoader` | Dataset loading strategy |

### Factory Methods

| Method | Description |
|--------|-------------|
| `builder()` | Creates a new builder for constructing Configuration instances |
| `defaults()` | Creates configuration with all framework defaults |

### Instance Methods

| Method | Description |
|--------|-------------|
| `toBuilder()` | Creates a new builder initialized with values from this instance |

### Builder Methods

| Method | Description |
|--------|-------------|
| `conventions(ConventionSettings)` | Sets the resolution rules for locating datasets |
| `operations(OperationDefaults)` | Sets the default database operations |
| `loader(DataSetLoader)` | Sets the strategy for constructing datasets |
| `build()` | Builds a new Configuration instance |

### Default Behavior

When `Configuration.defaults()` is used:

1. Conventions: `ConventionSettings.standard()`
2. Operations: `OperationDefaults.standard()`
3. Loader: Loaded via ServiceLoader from `DataSetLoaderProvider`

### Usage Example

```java
// Using defaults
var config = Configuration.defaults();

// Customizing with builder
var config = Configuration.builder()
    .conventions(ConventionSettings.builder()
        .dataFormat(DataFormat.TSV)
        .build())
    .operations(OperationDefaults.builder()
        .preparation(Operation.TRUNCATE_INSERT)
        .build())
    .build();

// JUnit example - customize configuration in @BeforeAll
@BeforeAll
static void setup(ExtensionContext context) {
    var config = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build();
    DatabaseTestExtension.setConfiguration(context, config);
}
```

## ConventionSettings

Defines naming conventions for dataset discovery and scenario filtering.

**Location**: `io.github.seijikohara.dbtester.api.config.ConventionSettings`

**Type**: `final class` with builder pattern

### Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `baseDirectory` | `@Nullable String` | `null` | Absolute or relative base path; null for classpath-relative |
| `expectationSuffix` | `String` | `"/expected"` | Subdirectory for expected datasets |
| `scenarioMarker` | `String` | `"[Scenario]"` | Column name for scenario filtering |
| `dataFormat` | `DataFormat` | `CSV` | File format for dataset files |
| `tableMergeStrategy` | `TableMergeStrategy` | `UNION_ALL` | Strategy for merging duplicate tables |
| `loadOrderFileName` | `String` | `"load-order.txt"` | File name for table loading order specification |
| `globalExcludeColumns` | `Set<String>` | `Set.of()` | Column names to exclude from all verifications (case-insensitive) |
| `globalColumnStrategies` | `Map<String, ColumnStrategyMapping>` | `Map.of()` | Column comparison strategies for all verifications |
| `rowOrdering` | `RowOrdering` | `ORDERED` | Default row comparison strategy |
| `queryTimeout` | `@Nullable Duration` | `null` | Maximum query wait time; null for no timeout |
| `retryCount` | `int` | `0` | Retry attempts for verification (0 = no retry) |
| `retryDelay` | `Duration` | `100ms` | Delay between retry attempts |
| `transactionMode` | `TransactionMode` | `SINGLE_TRANSACTION` | Transaction behavior for operations |

### Factory Methods

| Method | Description |
|--------|-------------|
| `builder()` | Creates a new builder for constructing ConventionSettings instances |
| `standard()` | Creates settings with all defaults |

### Instance Methods

| Method | Description |
|--------|-------------|
| `toBuilder()` | Creates a new builder initialized with values from this instance |

### Builder Methods

| Method | Description |
|--------|-------------|
| `baseDirectory(String)` | Sets the base directory (null for classpath-relative) |
| `expectationSuffix(String)` | Sets the expectation suffix |
| `scenarioMarker(String)` | Sets the scenario marker |
| `dataFormat(DataFormat)` | Sets the data format |
| `tableMergeStrategy(TableMergeStrategy)` | Sets the merge strategy |
| `loadOrderFileName(String)` | Sets the load order file name |
| `globalExcludeColumns(Set<String>)` | Sets the global exclude columns |
| `globalColumnStrategies(Map<String, ColumnStrategyMapping>)` | Sets the global column strategies |
| `rowOrdering(RowOrdering)` | Sets the row ordering strategy |
| `queryTimeout(Duration)` | Sets the query timeout (null for no timeout) |
| `retryCount(int)` | Sets the retry count |
| `retryDelay(Duration)` | Sets the retry delay |
| `transactionMode(TransactionMode)` | Sets the transaction mode |
| `build()` | Builds a new ConventionSettings instance |

### With Methods (Fluent Copy)

| Method | Description |
|--------|-------------|
| `withBaseDirectory(String)` | Creates copy with specified base directory (null for classpath-relative) |
| `withExpectationSuffix(String)` | Creates copy with specified expectation suffix |
| `withScenarioMarker(String)` | Creates copy with specified scenario marker |
| `withDataFormat(DataFormat)` | Creates copy with specified format |
| `withTableMergeStrategy(TableMergeStrategy)` | Creates copy with specified merge strategy |
| `withLoadOrderFileName(String)` | Creates copy with specified load order file name |
| `withGlobalExcludeColumns(Set<String>)` | Creates copy with specified global exclude columns |
| `withGlobalColumnStrategies(Map<String, ColumnStrategyMapping>)` | Creates copy with specified global column strategies |
| `withRowOrdering(RowOrdering)` | Creates copy with specified row ordering strategy |
| `withQueryTimeout(Duration)` | Creates copy with specified query timeout (null for no timeout) |
| `withRetryCount(int)` | Creates copy with specified retry count |
| `withRetryDelay(Duration)` | Creates copy with specified retry delay |
| `withTransactionMode(TransactionMode)` | Creates copy with specified transaction mode |

### Directory Resolution

When `baseDirectory` is null (default), datasets are resolved relative to the test class:

```
src/test/resources/
└── {test.class.package}/{TestClassName}/
    ├── TABLE1.csv           # Preparation dataset
    ├── TABLE2.csv
    ├── load-order.txt       # Table ordering (optional)
    └── expected/            # Expectation datasets (suffix configurable)
        ├── TABLE1.csv
        └── TABLE2.csv
```

When `baseDirectory` is specified:

```
{baseDirectory}/
├── TABLE1.csv
├── load-order.txt
└── expected/
    └── TABLE1.csv
```

### ExpectedDataSet Suffix

The `expectedDataSetSuffix` is appended to the data set path:

| DataSet Path | Suffix | ExpectedDataSet Path |
|-----------------|--------|------------------|
| `com/example/UserTest` | `/expected` | `com/example/UserTest/expected` |
| `/data/test` | `/expected` | `/data/test/expected` |
| `custom/path` | `/verify` | `custom/path/verify` |

### Advanced Configuration Example

```java
// Configure retry, timeout, and unordered comparison
var config = Configuration.builder()
    .conventions(ConventionSettings.builder()
        .rowOrdering(RowOrdering.UNORDERED)
        .queryTimeout(Duration.ofSeconds(30))
        .retryCount(3)
        .retryDelay(Duration.ofMillis(500))
        .transactionMode(TransactionMode.AUTO_COMMIT)
        .globalExcludeColumns(Set.of("CREATED_AT", "UPDATED_AT"))
        .globalColumnStrategies(Map.of(
            "EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"),
            "VERSION", ColumnStrategyMapping.ignore("VERSION")
        ))
        .build())
    .build();
```

## DataSourceRegistry

Thread-safe registry for `javax.sql.DataSource` instances.

**Location**: `io.github.seijikohara.dbtester.api.config.DataSourceRegistry`

### Thread Safety

- Uses `ConcurrentHashMap` for named data sources
- Uses `volatile` field for default data source
- `registerDefault()` and `clear()` are `synchronized`

### Registration Methods

| Method | Description |
|--------|-------------|
| `registerDefault(DataSource)` | Registers the default data source |
| `register(String, DataSource)` | Registers a named data source; if name is empty, delegates to `registerDefault()` |

### Retrieval Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getDefault()` | `DataSource` | Returns default; throws if not registered |
| `get(String)` | `DataSource` | Returns named or default; throws if not found |
| `find(String)` | `Optional<DataSource>` | Returns named data source as Optional |

### Query Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `hasDefault()` | `boolean` | Checks if default is registered |
| `has(String)` | `boolean` | Checks if named data source exists |

### Management Methods

| Method | Description |
|--------|-------------|
| `clear()` | Removes all registered data sources |

### Resolution Priority

When calling `get(name)`:

1. If name is non-empty, look up by name
2. If name is empty or not found, fall back to default
3. If neither found, throw `DataSourceNotFoundException`

### Usage Example

```java
@BeforeAll
static void setup(ExtensionContext context) {
    var registry = DatabaseTestExtension.getRegistry(context);

    // Single database
    registry.registerDefault(primaryDataSource);

    // Multiple databases
    registry.register("primary", primaryDataSource);
    registry.register("secondary", secondaryDataSource);
}
```

## OperationDefaults

Defines default database operations for preparation and expectation phases.

**Location**: `io.github.seijikohara.dbtester.api.config.OperationDefaults`

**Type**: `final class` with builder pattern

### Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `preparation` | `Operation` | `CLEAN_INSERT` | Default operation executed before test runs |
| `expectation` | `Operation` | `NONE` | Default operation executed after test completes |

### Factory Methods

| Method | Description |
|--------|-------------|
| `builder()` | Creates a new builder for constructing OperationDefaults instances |
| `standard()` | Creates defaults with `CLEAN_INSERT` for preparation and `NONE` for expectation |

### Instance Methods

| Method | Description |
|--------|-------------|
| `toBuilder()` | Creates a new builder initialized with values from this instance |

### Builder Methods

| Method | Description |
|--------|-------------|
| `preparation(Operation)` | Sets the default operation for preparation phase |
| `expectation(Operation)` | Sets the default operation for expectation phase |
| `build()` | Builds a new OperationDefaults instance |

### With Methods (Fluent Copy)

| Method | Description |
|--------|-------------|
| `withPreparation(Operation)` | Creates copy with specified preparation operation |
| `withExpectation(Operation)` | Creates copy with specified expectation operation |

## DataFormat

Defines supported file formats for dataset files.

**Location**: `io.github.seijikohara.dbtester.api.config.DataFormat`

**Type**: `enum`

### Values

| Value | Extension | Field Separator |
|-------|-----------|-----------------|
| `CSV` | `.csv` | Comma (`,`) |
| `TSV` | `.tsv` | Tab (`\t`) |

### Methods

| Method | Return Type | Description |
|--------|-------------|-------------|
| `getExtension()` | `String` | Returns file extension including dot |

### File Discovery

When loading datasets from a directory:

1. List all files matching the configured format extension
2. Parse each file as a table (filename without extension = table name)
3. Ignore files with other extensions

## TableMergeStrategy

Defines how tables from multiple datasets merge.

**Location**: `io.github.seijikohara.dbtester.api.config.TableMergeStrategy`

**Type**: `enum`

### Values

| Value | Description | Example |
|-------|-------------|---------|
| `FIRST` | Keep only first occurrence | [A,B] + [C,D] = [A,B] |
| `LAST` | Keep only last occurrence | [A,B] + [C,D] = [C,D] |
| `UNION` | Merge and deduplicate | [A,B] + [B,C] = [A,B,C] |
| `UNION_ALL` | Merge and keep duplicates (default) | [A,B] + [B,C] = [A,B,B,C] |

### Merge Behavior

Datasets are processed in annotation declaration order:

```java
@Preparation(sources = {
    @DataSet(resourceLocation = "dataset1"),  // Processed first
    @DataSet(resourceLocation = "dataset2")   // Processed second
})
```

When both datasets contain the same table:

| Strategy | Result |
|----------|--------|
| `FIRST` | Use table from dataset1 only |
| `LAST` | Use table from dataset2 only |
| `UNION` | Combine rows, remove exact duplicates |
| `UNION_ALL` | Combine all rows, keep duplicates |

## RowOrdering

Defines how rows should be compared during expectation verification.

**Location**: `io.github.seijikohara.dbtester.api.config.RowOrdering`

**Type**: `enum`

### Values

| Value | Description |
|-------|-------------|
| `ORDERED` | Positional comparison (row-by-row by index). Default behavior. |
| `UNORDERED` | Set-based comparison (rows matched regardless of position) |

### When to Use

| Mode | Use Case |
|------|----------|
| `ORDERED` | Query includes ORDER BY; row order is significant; maximum performance |
| `UNORDERED` | No ORDER BY; row order not significant; database may return rows in unpredictable order |

### Configuration

Row ordering can be configured:

1. **Annotation-level**: Per-test via `@ExpectedDataSet(rowOrdering = ...)`
2. **Global**: Via `ConventionSettings.withRowOrdering()`

Annotation-level configuration takes precedence over global settings.

### Performance Considerations

Unordered comparison has O(n*m) complexity in the worst case, where n is the expected row count and m is the actual row count. For large datasets, consider:

- Using `ORDERED` with ORDER BY in queries
- Limiting the dataset size
- Using primary key columns for deterministic ordering

## TransactionMode

Defines transaction behavior for database operations.

**Location**: `io.github.seijikohara.dbtester.api.config.TransactionMode`

**Type**: `enum`

### Values

| Value | Description |
|-------|-------------|
| `AUTO_COMMIT` | Each statement committed immediately (autoCommit = true) |
| `SINGLE_TRANSACTION` | All statements in single transaction (default) |
| `NONE` | No transaction management (connection state unchanged) |

### When to Use

| Mode | Use Case |
|------|----------|
| `AUTO_COMMIT` | Foreign key constraints prevent transactional insertion; debugging |
| `SINGLE_TRANSACTION` | Atomic all-or-nothing operations (recommended) |
| `NONE` | External transaction management (Spring's @Transactional) |

### Configuration Example

```java
var config = Configuration.builder()
    .conventions(ConventionSettings.builder()
        .transactionMode(TransactionMode.AUTO_COMMIT)
        .build())
    .build();
```

### Rollback Behavior

| Mode | On Failure |
|------|------------|
| `AUTO_COMMIT` | Partial data may remain; cannot rollback |
| `SINGLE_TRANSACTION` | Complete rollback; no partial data |
| `NONE` | Depends on external transaction manager |

## TestContext

Immutable snapshot of test execution context.

**Location**: `io.github.seijikohara.dbtester.api.context.TestContext`

**Type**: `record`

### Fields

| Field | Type | Description |
|-------|------|-------------|
| `testClass` | `Class<?>` | Test class containing the method |
| `testMethod` | `Method` | Currently executing test method |
| `configuration` | `Configuration` | Active framework configuration |
| `registry` | `DataSourceRegistry` | Registered data sources |

### Purpose

`TestContext` provides a framework-agnostic representation of test execution state. Test framework extensions (JUnit, Spock, and Kotest) create `TestContext` instances from their native context objects.

### Usage

```java
// Created by framework extensions
var configuration = Configuration.builder()
    .conventions(ConventionSettings.standard())
    .operations(OperationDefaults.standard())
    .loader(loader)
    .build();

TestContext context = new TestContext(
    testClass,
    testMethod,
    configuration,
    registry
);

// Used by loaders and executors
List<TableSet> tableSets = loader.loadPreparationDataSets(context);
```

## Related Specifications

- [Overview](01-overview) - Framework purpose and key concepts
- [Public API](03-public-api) - Annotations and interfaces
- [Data Formats](05-data-formats) - CSV and TSV file structure
- [Database Operations](06-database-operations) - Supported operations
- [Test Frameworks](07-test-frameworks) - JUnit, Spock, and Kotest integration
- [Error Handling](09-error-handling) - Error messages and exception types
