# DB Tester Specification - Configuration

This document describes the configuration classes and options available in the DB Tester framework.

## Configuration Class

Aggregates runtime configuration for the database testing extension.

**Location**: `io.github.seijikohara.dbtester.api.config.Configuration`

**Type**: `record`

### Components

| Component | Type | Description |
|-----------|------|-------------|
| `conventions` | `ConventionSettings` | Dataset directory resolution rules |
| `operations` | `OperationDefaults` | Default database operations |
| `loader` | `TableSetLoader` | Dataset loading strategy |

### Factory Methods

| Method | Description |
|--------|-------------|
| `defaults()` | Creates configuration with all framework defaults |
| `withConventions(ConventionSettings)` | Custom conventions with default operations and loader |
| `withOperations(OperationDefaults)` | Custom operations with default conventions and loader |
| `withLoader(TableSetLoader)` | Custom loader with default conventions and operations |

### Default Behavior

When `Configuration.defaults()` is used:

1. Conventions: `ConventionSettings.standard()`
2. Operations: `OperationDefaults.standard()`
3. Loader: Loaded via ServiceLoader from `TableSetLoaderProvider`

### Usage Example

```java
// JUnit example - customize configuration in @BeforeAll
@BeforeAll
static void setup(ExtensionContext context) {
    var conventions = ConventionSettings.standard()
        .withDataFormat(DataFormat.TSV);
    var config = Configuration.withConventions(conventions);
    DatabaseTestExtension.setConfiguration(context, config);
}
```

## ConventionSettings

Defines naming conventions for dataset discovery and scenario filtering.

**Location**: `io.github.seijikohara.dbtester.api.config.ConventionSettings`

**Type**: `record`

### Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `baseDirectory` | `@Nullable String` | `null` | Absolute or relative base path; null for classpath-relative |
| `expectedDataSetSuffix` | `String` | `"/expected"` | Subdirectory for expected datasets |
| `scenarioMarker` | `String` | `"[Scenario]"` | Column name for scenario filtering |
| `dataFormat` | `DataFormat` | `CSV` | File format for dataset files |
| `tableMergeStrategy` | `TableMergeStrategy` | `UNION_ALL` | Strategy for merging duplicate tables |
| `loadOrderFileName` | `String` | `"load-order.txt"` | File name for table loading order specification |
| `globalExcludeColumns` | `Set<String>` | `Set.of()` | Column names to exclude from all verifications (case-insensitive) |

### Factory Methods

| Method | Description |
|--------|-------------|
| `standard()` | Creates settings with all defaults |
| `withDataFormat(DataFormat)` | Creates copy with specified format |
| `withTableMergeStrategy(TableMergeStrategy)` | Creates copy with specified merge strategy |
| `withLoadOrderFileName(String)` | Creates copy with specified load order file name |
| `withGlobalExcludeColumns(Set<String>)` | Creates copy with specified global exclude columns |

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

**Type**: `record`

### Fields

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| `dataSet` | `Operation` | `CLEAN_INSERT` | Default operation executed before test runs |
| `expectedDataSet` | `Operation` | `NONE` | Default operation executed after test completes |

### Factory Methods

| Method | Description |
|--------|-------------|
| `standard()` | Creates defaults with `CLEAN_INSERT` for data set and `NONE` for expected data set |

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
@Preparation(dataSets = {
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
TestContext context = new TestContext(
    testClass,
    testMethod,
    configuration,
    registry
);

// Used by loaders and executors
List<TableSet> tableSets = loader.loadDataSetTableSets(context);
```

## Related Specifications

- [Overview](01-overview) - Framework purpose and key concepts
- [Public API](03-public-api) - Annotations and interfaces
- [Data Formats](05-data-formats) - CSV and TSV file structure
- [Database Operations](06-database-operations) - Supported operations
- [Test Frameworks](07-test-frameworks) - JUnit, Spock, and Kotest integration
- [Error Handling](09-error-handling) - Error messages and exception types
