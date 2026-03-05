---
title: "SPI Registration - DB Tester"
description: "ServiceLoader registration, JPMS module declarations, and custom SPI implementations for DB Tester."
---

# SPI Registration

## ServiceLoader Registration

### META-INF/services Files

**db-tester-core**:

```text
# Tier 2 — Provider Layer
# META-INF/services/io.github.seijikohara.dbtester.api.spi.OperationProvider
io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.AssertionProvider
io.github.seijikohara.dbtester.internal.spi.DefaultAssertionProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExpectationProvider
io.github.seijikohara.dbtester.internal.spi.DefaultExpectationProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider
io.github.seijikohara.dbtester.internal.spi.DefaultQueryAssertionProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExportProvider
io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider$Csv
io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider$Tsv
io.github.seijikohara.dbtester.internal.export.JsonExportProvider
io.github.seijikohara.dbtester.internal.export.YamlExportProvider

# META-INF/services/io.github.seijikohara.dbtester.api.spi.TypeHandler
io.github.seijikohara.dbtester.internal.jdbc.type.UuidTypeHandler
io.github.seijikohara.dbtester.internal.jdbc.type.JsonTypeHandler
io.github.seijikohara.dbtester.internal.jdbc.type.ArrayTypeHandler

# Tier 1 — Support Layer
# META-INF/services/io.github.seijikohara.dbtester.api.spi.PreparationSupport
io.github.seijikohara.dbtester.internal.lifecycle.DefaultPreparationSupport

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExpectationSupport
io.github.seijikohara.dbtester.internal.lifecycle.DefaultExpectationSupport

# META-INF/services/io.github.seijikohara.dbtester.api.spi.ExportSupport
io.github.seijikohara.dbtester.internal.lifecycle.DefaultExportSupport

# Standalone SPIs
# META-INF/services/io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider

# META-INF/services/io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
io.github.seijikohara.dbtester.internal.format.csv.CsvFormatProvider
io.github.seijikohara.dbtester.internal.format.tsv.TsvFormatProvider
io.github.seijikohara.dbtester.internal.format.json.JsonFormatProvider
io.github.seijikohara.dbtester.internal.format.yaml.YamlFormatProvider
```

**db-tester-junit**:

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.junit.jupiter.spi.JUnitScenarioNameResolver
```

**db-tester-spock**:

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.spock.spi.SpockScenarioNameResolver
```

**db-tester-kotest**:

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
io.github.seijikohara.dbtester.kotest.spi.KotestScenarioNameResolver
```

## JPMS Module Declarations

**db-tester-api module-info.java**:

```java
module io.github.seijikohara.dbtester.api {
    // Standalone SPIs
    uses io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
    uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;

    // Provider Layer (Tier 2)
    uses io.github.seijikohara.dbtester.api.spi.OperationProvider;
    uses io.github.seijikohara.dbtester.api.spi.AssertionProvider;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
    uses io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider;
    uses io.github.seijikohara.dbtester.api.spi.ExportProvider;
    uses io.github.seijikohara.dbtester.api.spi.TypeHandler;

    // Support Layer (Tier 1)
    uses io.github.seijikohara.dbtester.api.spi.PreparationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExportSupport;
}
```

**db-tester-junit module-info.java**:

```java
module io.github.seijikohara.dbtester.junit {
    // Public API exports
    exports io.github.seijikohara.dbtester.junit.jupiter.extension;
    exports io.github.seijikohara.dbtester.junit.jupiter.lifecycle;
    exports io.github.seijikohara.dbtester.junit.jupiter.spi;

    // Required dependencies
    requires transitive io.github.seijikohara.dbtester.api;
    requires transitive org.junit.jupiter.api;

    // SPI service consumers (Support Layer)
    uses io.github.seijikohara.dbtester.api.spi.PreparationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
    uses io.github.seijikohara.dbtester.api.spi.ExportSupport;

    // SPI service providers
    provides io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver with
        io.github.seijikohara.dbtester.junit.jupiter.spi.JUnitScenarioNameResolver;
}
```

**db-tester-core module-info.java**:

```java
module io.github.seijikohara.dbtester.core {
    // API module dependency
    requires transitive io.github.seijikohara.dbtester.api;

    // Internal SPI uses
    uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
    uses io.github.seijikohara.dbtester.api.spi.TypeHandler;
    uses io.github.seijikohara.dbtester.internal.format.spi.FormatProvider;

    // SPI implementations for API module
    provides io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider
        with io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider;
    provides io.github.seijikohara.dbtester.api.spi.OperationProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider;
    provides io.github.seijikohara.dbtester.api.spi.AssertionProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultAssertionProvider;
    provides io.github.seijikohara.dbtester.api.spi.ExpectationProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultExpectationProvider;
    provides io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider
        with io.github.seijikohara.dbtester.internal.spi.DefaultQueryAssertionProvider;
    provides io.github.seijikohara.dbtester.api.spi.PreparationSupport
        with io.github.seijikohara.dbtester.internal.lifecycle.DefaultPreparationSupport;
    provides io.github.seijikohara.dbtester.api.spi.ExpectationSupport
        with io.github.seijikohara.dbtester.internal.lifecycle.DefaultExpectationSupport;
    provides io.github.seijikohara.dbtester.api.spi.ExportSupport
        with io.github.seijikohara.dbtester.internal.lifecycle.DefaultExportSupport;

    // Export providers
    provides io.github.seijikohara.dbtester.api.spi.ExportProvider
        with io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider.Csv,
             io.github.seijikohara.dbtester.internal.export.DelimitedExportProvider.Tsv,
             io.github.seijikohara.dbtester.internal.export.JsonExportProvider,
             io.github.seijikohara.dbtester.internal.export.YamlExportProvider;

    // Internal format providers
    provides io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
        with io.github.seijikohara.dbtester.internal.format.csv.CsvFormatProvider,
             io.github.seijikohara.dbtester.internal.format.tsv.TsvFormatProvider,
             io.github.seijikohara.dbtester.internal.format.json.JsonFormatProvider,
             io.github.seijikohara.dbtester.internal.format.yaml.YamlFormatProvider;

    // Type handlers
    provides io.github.seijikohara.dbtester.api.spi.TypeHandler
        with io.github.seijikohara.dbtester.internal.jdbc.type.UuidTypeHandler,
             io.github.seijikohara.dbtester.internal.jdbc.type.JsonTypeHandler,
             io.github.seijikohara.dbtester.internal.jdbc.type.ArrayTypeHandler;
}
```

## Custom Implementations

### Custom DataSetLoader

To provide a custom dataset loader:

1. Implement the `DataSetLoader` interface:

```java
public class CustomDataSetLoader implements DataSetLoader {
    @Override
    public List<TableSet> loadPreparationDataSets(TestContext context) {
        // Custom loading logic
    }

    @Override
    public List<TableSet> loadExpectationDataSets(TestContext context) {
        // Custom loading logic
    }
}
```

2. Register via `Configuration`:

```java
var config = Configuration.builder()
    .loader(new CustomDataSetLoader())
    .build();
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

```text
# META-INF/services/io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver
com.example.CustomScenarioNameResolver
```

### Custom FormatProvider

::: warning
FormatProvider is an internal SPI. The interface is not part of the public API contract
and may change without notice. Custom implementations depend on internal packages.
:::

To support additional file formats:

1. Implement `FormatProvider`:

```java
public class XmlFormatProvider implements FormatProvider {
    @Override
    public FileExtension supportedFileExtension() {
        return new FileExtension("xml");
    }

    @Override
    public TableSet parse(Path directory) {
        // Parse all XML files in directory
    }
}
```

2. Register via ServiceLoader:

```text
# META-INF/services/io.github.seijikohara.dbtester.internal.format.spi.FormatProvider
com.example.XmlFormatProvider
```

## Provider Priority

The framework selects providers as follows when multiple providers exist:

**Support Layer (Tier 1)**:

| SPI | Selection |
|-----|-----------|
| `PreparationSupport` | First found |
| `ExpectationSupport` | First found |
| `ExportSupport` | First found |

**Provider Layer (Tier 2)**:

| SPI | Selection |
|-----|-----------|
| `OperationProvider` | First found |
| `AssertionProvider` | First found |
| `ExpectationProvider` | First found |
| `QueryAssertionProvider` | First found |
| `ExportProvider` | First matching `supportedFormat()` |

**Standalone SPIs**:

| SPI | Selection |
|-----|-----------|
| `DataSetLoaderProvider` | First found |
| `ScenarioNameResolver` | Sorted by `priority()`, first where `canResolve()` returns true |
| `TypeHandler` | By SQL type, then `getPriority()` (highest wins); database-specific match preferred |
| `FormatProvider` | First matching `supportedFileExtension()` |

## Related Specifications

- [SPI Overview](spi) - Architecture and Support Layer
- [SPI Providers](spi-providers) - Provider interface details
- [Architecture](architecture) - Module structure
- [Test Frameworks](test-frameworks) - Framework integration
