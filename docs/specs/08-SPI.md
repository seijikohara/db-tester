# DB Tester Specification - Service Provider Interface (SPI)

This document describes the SPI extension points in the DB Tester framework.


## SPI Overview

The framework uses Java ServiceLoader for loose coupling between modules:

```mermaid
flowchart TB
    subgraph API[db-tester-api]
        SPI[SPI Interfaces]
    end

    subgraph CORE[db-tester-core]
        IMPL[Implementations]
    end

    subgraph Frameworks[Test Frameworks]
        JUNIT[db-tester-junit]
        SPOCK[db-tester-spock]
    end

    API <-->|ServiceLoader| CORE
    Frameworks -->|Compile-time| API
    CORE -.->|Runtime via ServiceLoader| Frameworks
```

### Design Principles

1. **API Independence**: Test framework modules depend only on `db-tester-api`
2. **Runtime Discovery**: Core implementations loaded via ServiceLoader
3. **Extensibility**: Custom implementations can replace defaults


## API Module SPIs

### DataSetLoaderProvider

Provides the default `DataSetLoader` implementation.

**Location**: `io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider`

**Interface**:

```java
public interface DataSetLoaderProvider {
    DataSetLoader getLoader();
}
```

**Default Implementation**: `DefaultDataSetLoaderProvider` in `db-tester-core`

**Usage**: Called by `Configuration.defaults()` to obtain the loader


### OperationProvider

Executes database operations on datasets.

**Location**: `io.github.seijikohara.dbtester.api.spi.OperationProvider`

**Interface**:

```java
public interface OperationProvider {
    void execute(
        Operation operation,
        DataSet dataSet,
        DataSource dataSource,
        TableOrderingStrategy tableOrderingStrategy);
}
```

**Default Implementation**: `DefaultOperationProvider` in `db-tester-core`

**Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `operation` | `Operation` | The database operation to execute |
| `dataSet` | `DataSet` | The dataset containing tables and rows |
| `dataSource` | `DataSource` | The JDBC data source for connections |
| `tableOrderingStrategy` | `TableOrderingStrategy` | Strategy for table processing order |

**Operations**:
- `NONE` - No operation
- `INSERT` - Insert rows
- `UPDATE` - Update by primary key
- `DELETE` - Delete by primary key
- `DELETE_ALL` - Delete all rows
- `REFRESH` - Upsert (insert or update)
- `TRUNCATE_TABLE` - Truncate tables
- `CLEAN_INSERT` - Delete all then insert
- `TRUNCATE_INSERT` - Truncate then insert


### AssertionProvider

Performs database assertions for expectation verification.

**Location**: `io.github.seijikohara.dbtester.api.spi.AssertionProvider`

**Interface**:

```java
public interface AssertionProvider {
    // Core comparison methods
    void assertEquals(DataSet expected, DataSet actual);
    void assertEquals(DataSet expected, DataSet actual, AssertionFailureHandler failureHandler);
    void assertEquals(Table expected, Table actual);
    void assertEquals(Table expected, Table actual, Collection<String> additionalColumnNames);
    void assertEquals(Table expected, Table actual, AssertionFailureHandler failureHandler);

    // Comparison with column exclusion
    void assertEqualsIgnoreColumns(DataSet expected, DataSet actual, String tableName,
                                   Collection<String> ignoreColumnNames);
    void assertEqualsIgnoreColumns(Table expected, Table actual,
                                   Collection<String> ignoreColumnNames);

    // SQL query-based comparison
    void assertEqualsByQuery(DataSet expected, DataSource dataSource, String sqlQuery,
                             String tableName, Collection<String> ignoreColumnNames);
    void assertEqualsByQuery(Table expected, DataSource dataSource, String tableName,
                             String sqlQuery, Collection<String> ignoreColumnNames);
}
```

**Default Implementation**: `DefaultAssertionProvider` in `db-tester-core`

**Key Methods**:

| Method | Description |
|--------|-------------|
| `assertEquals(DataSet, DataSet)` | Compare two datasets |
| `assertEquals(Table, Table)` | Compare two tables |
| `assertEqualsIgnoreColumns(...)` | Compare while ignoring specific columns |
| `assertEqualsByQuery(...)` | Compare query results against expected data |

**Behavior**:
1. Compare expected vs actual datasets/tables
2. Apply comparison strategies per column (STRICT, IGNORE, NUMERIC, etc.)
3. Collect all differences (not fail-fast)
4. Output human-readable summary + YAML details on mismatch

See [Error Handling - Validation Errors](09-error-handling#validation-errors) for output format details.


### ExpectationProvider

Verifies database state against expected datasets.

**Location**: `io.github.seijikohara.dbtester.api.spi.ExpectationProvider`

**Interface**:

```java
public interface ExpectationProvider {
    void verifyExpectation(DataSet expectedDataSet, DataSource dataSource);
}
```

**Default Implementation**: `DefaultExpectationProvider` in `db-tester-core`

**Parameters**:

| Parameter | Type | Description |
|-----------|------|-------------|
| `expectedDataSet` | `DataSet` | The expected dataset containing expected table data |
| `dataSource` | `DataSource` | The database connection source for retrieving actual data |

**Process**:
1. For each table in the expected dataset, fetch actual data from the database
2. Filter actual data to only include columns present in expected table
3. Compare filtered actual data against expected data
4. Throw `AssertionError` if verification fails


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
| `canResolve(Method)` | `boolean` | `true` | Returns whether this resolver can handle the method |
| `priority()` | `int` | `0` | Returns priority for resolver selection (higher = preferred) |

**Implementations**:

| Implementation | Module | Description |
|----------------|--------|-------------|
| `JUnitScenarioNameResolver` | `db-tester-junit` | Resolves from JUnit method name |
| `SpockScenarioNameResolver` | `db-tester-spock` | Resolves from Spock feature name |

**Resolution Logic**:
1. Sort all registered resolvers by `priority()` (descending)
2. Query each resolver via `canResolve()`
3. Use first resolver that returns `true`
4. Call `resolve()` to obtain scenario name


## Core Module SPIs

### FormatProvider

Parses dataset files in specific formats.

**Location**: `io.github.seijikohara.dbtester.internal.format.spi.FormatProvider`

**Interface**:

```java
public interface FormatProvider {
    FileExtension supportedFileExtension();
    DataSet parse(Path directory);
}
```

**Methods**:

| Method | Return Type | Description |
|--------|-------------|-------------|
| `supportedFileExtension()` | `FileExtension` | Returns the file extension without leading dot (e.g., "csv") |
| `parse(Path)` | `DataSet` | Parses all files in directory into a DataSet |

**Implementations**:

| Implementation | Extension | Delimiter |
|----------------|-----------|-----------|
| `CsvFormatProvider` | `.csv` | Comma |
| `TsvFormatProvider` | `.tsv` | Tab |

**Note**: This is an internal SPI not intended for external implementation.


## ServiceLoader Registration

### META-INF/services Files

**db-tester-core**:

```
# META-INF/services/io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.OperationProvider
io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.AssertionProvider
io.github.seijikohara.dbtester.internal.spi.DefaultAssertionProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExpectationProvider
io.github.seijikohara.dbtester.internal.spi.DefaultExpectationProvider

# META-INF/services/io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
io.github.seijikohara.dbtester.internal.format.csv.CsvFormatProvider
io.github.seijikohara.dbtester.internal.format.tsv.TsvFormatProvider
```

**db-tester-junit**:

```
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.junit.jupiter.spi.JUnitScenarioNameResolver
```

**db-tester-spock**:

```
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.spock.spi.SpockScenarioNameResolver
```

### JPMS Module Declarations

**db-tester-api module-info.java**:

```java
module io.github.seijikohara.dbtester.api {
    uses io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
    uses io.github.seijikohara.dbtester.api.spi.OperationProvider;
    uses io.github.seijikohara.dbtester.api.spi.AssertionProvider;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
    uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
}
```

**db-tester-core module-info.java**:

```java
module io.github.seijikohara.dbtester.core {
    provides io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
        with io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider;
    provides io.github.seijikohara.dbtester.api.spi.OperationProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider;
    // ... other providers
}
```


## Custom Implementations

### Custom DataSetLoader

To provide a custom dataset loader:

1. Implement `DataSetLoader` interface:

```java
public class CustomDataSetLoader implements DataSetLoader {
    @Override
    public List<DataSet> loadPreparationDataSets(TestContext context) {
        // Custom loading logic
    }

    @Override
    public List<DataSet> loadExpectationDataSets(TestContext context) {
        // Custom loading logic
    }
}
```

2. Register via `Configuration`:

```java
var config = Configuration.withLoader(new CustomDataSetLoader());
DatabaseTestExtension.setConfiguration(context, config);
```

### Custom ScenarioNameResolver

To provide a custom scenario resolver:

1. Implement `ScenarioNameResolver`:

```java
public class CustomScenarioNameResolver implements ScenarioNameResolver {
    private static final int HIGH_PRIORITY = 100;

    @Override
    public ScenarioName resolve(Method testMethod) {
        // Extract scenario name from method
    }

    @Override
    public boolean canResolve(Method testMethod) {
        // Return true for supported methods
    }

    @Override
    public int priority() {
        return HIGH_PRIORITY;  // Higher priority than default resolvers
    }
}
```

2. Register via ServiceLoader:

```
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
com.example.CustomScenarioNameResolver
```

### Custom FormatProvider

To support additional file formats (internal SPI):

1. Implement `FormatProvider`:

```java
public class XmlFormatProvider implements FormatProvider {
    @Override
    public boolean canHandle(String fileExtension) {
        return ".xml".equals(fileExtension);
    }

    @Override
    public DataSet parseDataSet(Path filePath, ConventionSettings conventions) {
        // Parse XML file
    }
}
```

2. Register via ServiceLoader:

```
# META-INF/services/io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
com.example.XmlFormatProvider
```

### Provider Priority

When multiple providers are registered:

| SPI | Selection |
|-----|-----------|
| `DataSetLoaderProvider` | First found |
| `OperationProvider` | First found |
| `AssertionProvider` | First found |
| `ExpectationProvider` | First found |
| `ScenarioNameResolver` | Sorted by `priority()`, first that `canResolve()` returns true |
| `FormatProvider` | First that `canHandle()` returns true |


## Related Specifications

- [Architecture](02-architecture) - Module structure
- [Configuration](04-configuration) - Configuration classes
- [Test Frameworks](07-test-frameworks) - Framework integration
