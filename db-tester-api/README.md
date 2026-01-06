# DB Tester - API Module

This module defines the public API for DB Tester, including annotations, configuration types, domain objects, and SPI interfaces.

## Overview

- **Annotations** - `@DataSet`, `@ExpectedDataSet`, and `@DataSetSource` for declarative test configuration
- **Configuration** - `Configuration`, `ConventionSettings`, and `DataSourceRegistry` for framework setup
- **Domain Objects** - Type-safe wrappers (`TableName`, `ColumnName`, `CellValue`) for database identifiers
- **SPI Interfaces** - Extension points for custom implementations (`OperationProvider`, `AssertionProvider`)

## Architecture

```
db-tester-api (public API)
    ↑
    ├── db-tester-core (implements SPI)
    ├── db-tester-junit (compile-time dependency)
    ├── db-tester-spock (compile-time dependency)
    └── db-tester-kotest (compile-time dependency)
```

The API module provides the stable public contract. All other modules depend on this module.

## Requirements

- Java 21 or later

## Installation

Integration modules include this module transitively. Direct dependency is not required.

Use the integration modules:

- [db-tester-junit](../db-tester-junit/) for JUnit
- [db-tester-spock](../db-tester-spock/) for Spock
- [db-tester-kotest](../db-tester-kotest/) for Kotest
- [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) for Spring Boot with JUnit
- [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) for Spring Boot with Spock
- [db-tester-kotest-spring-boot-starter](../db-tester-kotest-spring-boot-starter/) for Spring Boot with Kotest

### Gradle

```kotlin
dependencies {
    implementation("io.github.seijikohara:db-tester-api:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-api</artifactId>
    <version>VERSION</version>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-api).

## Package Structure

| Package | Description |
|---------|-------------|
| `api.annotation` | Test annotations (`@DataSet`, `@ExpectedDataSet`, `@DataSetSource`) |
| `api.assertion` | Programmatic assertion API (`DatabaseAssertion`, `AssertionFailureHandler`) |
| `api.config` | Configuration classes (`Configuration`, `ConventionSettings`, `DataSourceRegistry`) |
| `api.context` | Test execution context (`TestContext`) |
| `api.dataset` | Dataset interfaces (`TableSet`, `Table`, `Row`) |
| `api.domain` | Type-safe domain objects (`TableName`, `ColumnName`, `CellValue`, `ComparisonStrategy`) |
| `api.exception` | Exception hierarchy (`DatabaseTesterException`, `DataSetLoadException`) |
| `api.loader` | Dataset loader interface (`DataSetLoader`) |
| `api.operation` | Database operations enum (`Operation`) |
| `api.scenario` | Scenario resolution (`ScenarioName`, `ScenarioNameResolver`) |
| `api.spi` | Service Provider Interfaces (`OperationProvider`, `AssertionProvider`) |

## JPMS Support

**Module name**: `io.github.seijikohara.dbtester.api`

```java
module your.module {
    requires io.github.seijikohara.dbtester.api;
}
```

## Key Classes

| Class | Description |
|-------|-------------|
| [`@DataSet`](src/main/java/io/github/seijikohara/dbtester/api/annotation/DataSet.java) | Configures test data setup before execution |
| [`@ExpectedDataSet`](src/main/java/io/github/seijikohara/dbtester/api/annotation/ExpectedDataSet.java) | Configures database state verification after execution |
| [`@DataSetSource`](src/main/java/io/github/seijikohara/dbtester/api/annotation/DataSetSource.java) | Specifies dataset location and filtering options |
| [`Configuration`](src/main/java/io/github/seijikohara/dbtester/api/config/Configuration.java) | Main framework configuration |
| [`ConventionSettings`](src/main/java/io/github/seijikohara/dbtester/api/config/ConventionSettings.java) | Dataset resolution conventions |
| [`DataSourceRegistry`](src/main/java/io/github/seijikohara/dbtester/api/config/DataSourceRegistry.java) | Registry for managing multiple data sources |
| [`DataFormat`](src/main/java/io/github/seijikohara/dbtester/api/config/DataFormat.java) | Dataset file format (CSV, TSV) |
| [`TableMergeStrategy`](src/main/java/io/github/seijikohara/dbtester/api/config/TableMergeStrategy.java) | Strategy for merging multiple datasets |
| [`ComparisonStrategy`](src/main/java/io/github/seijikohara/dbtester/api/domain/ComparisonStrategy.java) | Value comparison modes for assertions |
| [`Operation`](src/main/java/io/github/seijikohara/dbtester/api/operation/Operation.java) | Database operations (CLEAN_INSERT, INSERT, UPDATE) |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-core`](../db-tester-core/) | Internal implementation (SPI providers) |
| [`db-tester-junit`](../db-tester-junit/) | JUnit extension |
| [`db-tester-spock`](../db-tester-spock/) | Spock extension |
| [`db-tester-kotest`](../db-tester-kotest/) | Kotest extension |
| [`db-tester-bom`](../db-tester-bom/) | Bill of Materials |

## Documentation

For usage examples and configuration details, see the [main README](../README.md).
