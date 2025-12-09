# DB Tester Specification - Service Provider Interface (SPI)

This document describes the SPI extension points in the DB Tester framework.

---

## Table of Contents

1. [SPI Overview](#spi-overview)
2. [API Module SPIs](#api-module-spis)
3. [Core Module SPIs](#core-module-spis)
4. [ServiceLoader Registration](#serviceloader-registration)
5. [Custom Implementations](#custom-implementations)

---

## SPI Overview

The framework uses Java ServiceLoader for loose coupling between modules:

```
┌─────────────────┐          ServiceLoader          ┌─────────────────┐
│                 │ ◄────────────────────────────── │                 │
│  db-tester-api  │    Defines SPI interfaces       │  db-tester-core │
│                 │ ──────────────────────────────► │                 │
│                 │    Provides implementations     │                 │
└─────────────────┘                                 └─────────────────┘
         ▲                                                   │
         │                                                   │
         │ Compile-time dependency                           │
         │                                                   │
┌─────────────────┐                                          │
│                 │      Runtime via ServiceLoader           │
│ db-tester-junit │ ◄────────────────────────────────────────┘
│ db-tester-spock │
│                 │
└─────────────────┘
```

### Design Principles

1. **API Independence**: Test framework modules depend only on `db-tester-api`
2. **Runtime Discovery**: Core implementations loaded via ServiceLoader
3. **Extensibility**: Custom implementations can replace defaults

---

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

---

### OperationProvider

Executes database operations on datasets.

**Location**: `io.github.seijikohara.dbtester.api.spi.OperationProvider`

**Interface**:

```java
public interface OperationProvider {
    void execute(Operation operation, DataSet dataSet, DataSource dataSource);
}
```

**Default Implementation**: `DefaultOperationProvider` in `db-tester-core`

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

---

### AssertionProvider

Performs database assertions for expectation verification.

**Location**: `io.github.seijikohara.dbtester.api.spi.AssertionProvider`

**Interface**:

```java
public interface AssertionProvider {
    void assertEquals(DataSet expected, DataSource dataSource);
}
```

**Default Implementation**: `DefaultAssertionProvider` in `db-tester-core`

**Behavior**:
1. Read actual data from database
2. Compare expected vs actual datasets
3. Throw `ValidationException` on mismatch

---

### ExpectationProvider

Orchestrates the expectation verification phase.

**Location**: `io.github.seijikohara.dbtester.api.spi.ExpectationProvider`

**Interface**:

```java
public interface ExpectationProvider {
    void verify(TestContext context, Expectation expectation);
}
```

**Default Implementation**: `DefaultExpectationProvider` in `db-tester-core`

**Process**:
1. Load expected datasets from configuration
2. Filter by scenario
3. Delegate to `AssertionProvider` for comparison

---

### ScenarioNameResolver

Resolves scenario names from test method context.

**Location**: `io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver`

**Interface**:

```java
public interface ScenarioNameResolver {
    boolean supports(Method testMethod);
    ScenarioName resolve(Method testMethod);
}
```

**Implementations**:

| Implementation | Module | Description |
|----------------|--------|-------------|
| `JUnitScenarioNameResolver` | `db-tester-junit` | Resolves from JUnit method name |
| `SpockScenarioNameResolver` | `db-tester-spock` | Resolves from Spock feature name |

**Resolution Logic**:
1. Query all registered resolvers via `supports()`
2. Use first resolver that returns `true`
3. Call `resolve()` to obtain scenario name

---

## Core Module SPIs

### FormatProvider

Parses dataset files in specific formats.

**Location**: `io.github.seijikohara.dbtester.internal.format.spi.FormatProvider`

**Interface**:

```java
public interface FormatProvider {
    boolean canHandle(String fileExtension);
    DataSet parseDataSet(Path filePath, ConventionSettings conventions);
}
```

**Implementations**:

| Implementation | Extension | Delimiter |
|----------------|-----------|-----------|
| `CsvFormatProvider` | `.csv` | Comma |
| `TsvFormatProvider` | `.tsv` | Tab |

**Note**: This is an internal SPI not intended for external implementation.

---

## ServiceLoader Registration

### META-INF/services Files

**db-tester-core**:

```
# META-INF/services/io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
io.github.seijikohara.dbtester.internal.spi.DefaultDataSetLoaderProvider

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
        with io.github.seijikohara.dbtester.internal.spi.DefaultDataSetLoaderProvider;
    provides io.github.seijikohara.dbtester.api.spi.OperationProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider;
    // ... other providers
}
```

---

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
    @Override
    public boolean supports(Method testMethod) {
        // Return true for supported methods
    }

    @Override
    public ScenarioName resolve(Method testMethod) {
        // Extract scenario name from method
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
| `ScenarioNameResolver` | First that `supports()` returns true |
| `FormatProvider` | First that `canHandle()` returns true |

---

## Related Specifications

- [Architecture](02-ARCHITECTURE.md) - Module structure
- [Configuration](04-CONFIGURATION.md) - Configuration classes
- [Test Frameworks](07-TEST-FRAMEWORKS.md) - Framework integration
