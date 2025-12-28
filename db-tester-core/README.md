# DB Tester - Core Module

This module provides the internal implementation of the DB Tester framework. It implements the SPI interfaces defined in `db-tester-api` and contains database operations, dataset loaders, and format providers.

## Overview

- **SPI Implementations** - Default providers for operations, assertions, and data loading
- **Database Operations** - Pure JDBC implementation for database setup and verification
- **Dataset Loaders** - Convention-based and custom data loading implementations
- **Format Providers** - CSV and TSV format support with scenario filtering

## Architecture

```
db-tester-api (public API)
        ↑
db-tester-core (SPI implementations)
        ↑
db-tester-junit / db-tester-spock / db-tester-kotest (test framework integration)
```

- **Depends on**: `db-tester-api`
- **Is loaded at runtime** by `db-tester-junit`, `db-tester-spock`, and `db-tester-kotest` via ServiceLoader

## Requirements

- Java 21 or later

## Installation

This module is loaded automatically at runtime by the integration modules. Direct dependency is not required for typical usage.

Use the integration modules instead:

- [db-tester-junit](../db-tester-junit/) for JUnit
- [db-tester-spock](../db-tester-spock/) for Spock
- [db-tester-kotest](../db-tester-kotest/) for Kotest
- [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) for Spring Boot with JUnit
- [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) for Spring Boot with Spock
- [db-tester-kotest-spring-boot-starter](../db-tester-kotest-spring-boot-starter/) for Spring Boot with Kotest

### Gradle

```kotlin
dependencies {
    runtimeOnly("io.github.seijikohara:db-tester-core:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-core</artifactId>
    <version>VERSION</version>
    <scope>runtime</scope>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-core).

## Package Structure

| Package | Description |
|---------|-------------|
| `internal.assertion` | Database assertion and comparison implementations |
| `internal.dataset` | DataSet, Table, Row implementations |
| `internal.domain` | Internal type-safe domain value objects |
| `internal.format` | Format parsing framework |
| `internal.format.csv` | CSV format provider implementation |
| `internal.format.tsv` | TSV format provider implementation |
| `internal.format.parser` | Delimited text parsing utilities |
| `internal.format.spi` | Format provider SPI (`FormatProvider`, `FormatRegistry`) |
| `internal.jdbc` | JDBC utilities and connection handling |
| `internal.jdbc.read` | Database read operations (table data retrieval, type conversion) |
| `internal.jdbc.write` | Database write operations (INSERT, UPDATE, DELETE, TRUNCATE) |
| `internal.loader` | Data loading and merging implementations |
| `internal.scenario` | Scenario filtering logic |
| `internal.spi` | Default SPI implementations |
| `internal.util` | Utility classes (topological sorting) |

## SPI Providers

This module provides implementations for the SPI interfaces defined in `db-tester-api`:

| SPI Interface | Implementation |
|---------------|----------------|
| `OperationProvider` | `DefaultOperationProvider` |
| `ExpectationProvider` | `DefaultExpectationProvider` |
| `AssertionProvider` | `DefaultAssertionProvider` |
| `DataSetLoaderProvider` | `DefaultDataSetLoaderProvider` |

Internal format providers:

| Internal SPI | Implementations |
|--------------|-----------------|
| `FormatProvider` | `CsvFormatProvider`, `TsvFormatProvider` |

These are registered via `META-INF/services/` and loaded via ServiceLoader.

## JPMS Support

**Module name**: `io.github.seijikohara.dbtester.core`

```java
requires io.github.seijikohara.dbtester.core;
```

## Key Classes

| Class | Description |
|-------|-------------|
| `DefaultOperationProvider` | Executes database operations (INSERT, DELETE) |
| `DefaultExpectationProvider` | Verifies database state against expected datasets |
| `DefaultAssertionProvider` | Provides assertion logic for dataset comparison |
| `DefaultDataSetLoaderProvider` | Loads datasets from file system or classpath |
| `DataSetComparator` | Compares expected and actual datasets |
| `OperationExecutor` | Coordinates JDBC operation execution |
| `TableReader` | Reads table data from database |
| `FormatRegistry` | Manages format providers (CSV, TSV) |
| `DataSetMerger` | Merges multiple datasets using configured strategy |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-junit`](../db-tester-junit/) | JUnit extension |
| [`db-tester-spock`](../db-tester-spock/) | Spock extension |
| [`db-tester-kotest`](../db-tester-kotest/) | Kotest extension |

## Documentation

For usage examples and configuration details, refer to the [main README](../README.md).
