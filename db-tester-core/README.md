# DB Tester - Core Module

This module provides both the public API and internal implementation of the DB Tester framework. It contains the annotations, configuration interfaces, DbUnit bridge, dataset loaders, and other components.

## Overview

The core module includes:

- **Public API** - Annotations (`@Preparation`, `@Expectation`), configuration, and assertion interfaces
- **DbUnit Bridge** - Complete isolation of DbUnit dependencies using the Bridge pattern
- **Dataset Loaders** - Convention-based and custom data loading implementations
- **Format Providers** - CSV format support with scenario-based filtering

## Requirements

- Java 21 or later
- DbUnit 3 or later (transitive dependency)

## Installation

> **Note**: This module is typically not used directly. Instead, use the integration modules:
> - [db-tester-junit](../db-tester-junit/) for JUnit
> - [db-tester-spock](../db-tester-spock/) for Spock Framework
> - [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) for Spring Boot with JUnit
> - [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) for Spring Boot with Spock

### Gradle

```kotlin
dependencies {
    implementation("io.github.seijikohara:db-tester-core:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-core</artifactId>
    <version>VERSION</version>
</dependency>
```

Replace `VERSION` with the latest version from [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-core).

## Usage

### Convention-Based Loading

The default loader resolves data files based on test class and method names:

```
src/test/resources/
  com/example/
    UserServiceTest/           # Test class name
      testCreateUser/          # Test method name (optional)
        USERS.csv              # Preparation data
        expected/              # Expectation directory
          USERS.csv            # Expected data
        table-ordering.txt     # Optional: explicit table order
```

### Supported Data Formats

| Format | Extension | Description |
|--------|-----------|-------------|
| CSV | `.csv` | Comma-separated values with scenario filtering support |

## Key Classes

### Public API (`io.github.seijikohara.dbtester.api`)

| Package | Description |
|---------|-------------|
| `annotation` | `@Preparation`, `@Expectation`, `@DataSet` annotations |
| `assertion` | `DatabaseAssertion` for programmatic assertions |
| `config` | `Configuration`, `DataSourceRegistry` for setup |
| `exception` | Exception types for error handling |
| `loader` | `DataSetLoader` interface for custom loaders |
| `operation` | `Operation` enum (CLEAN_INSERT, INSERT, UPDATE, etc.) |

### Internal Implementation (`io.github.seijikohara.dbtester.internal`)

| Package | Description |
|---------|-------------|
| `assertion` | Database assertion implementations |
| `context` | Test execution context |
| `dataset` | DataSet, Table, Row interfaces and implementations |
| `dbunit` | DbUnit bridge layer (complete isolation) |
| `domain` | Type-safe domain value objects |
| `loader` | Data loading implementations |
| `spi` | Service provider interfaces |

## Configuration

### DbUnit Isolation via Bridge Pattern

The framework completely isolates DbUnit dependencies using the Bridge pattern. This ensures framework code remains DbUnit-independent and allows future migration to other database testing libraries.

**Key Principles**:

1. **Complete isolation** - All DbUnit dependencies consolidated in `internal.dbunit`
2. **Single entry point** - `DatabaseBridge` (Singleton) is the primary interface
3. **Type safety** - DbUnit types never leak outside bridge package hierarchy
4. **Path-based API** - Framework uses `java.nio.file.Path` throughout
5. **Exception isolation** - DbUnit exceptions wrapped in framework exceptions

### SPI Registration

This module registers implementations via `META-INF/services/`:

- `io.github.seijikohara.dbtester.internal.spi.DatabaseBridgeProvider` - Database bridge implementation
- `io.github.seijikohara.dbtester.internal.spi.DataSetLoaderProvider` - Convention-based data loader
- `io.github.seijikohara.dbtester.internal.dataset.DataSetFormatProvider` - CSV format provider

### Java Platform Module System (JPMS)

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.core`

This module provides JPMS compatibility via the `Automatic-Module-Name` manifest attribute.
Full `module-info.java` support is not available because DbUnit does not support JPMS.

## Related Modules

- [db-tester-junit](../db-tester-junit/) - JUnit extension
- [db-tester-spock](../db-tester-spock/) - Spock Framework extension
- [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) - Spring Boot auto-configuration for JUnit
- [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) - Spring Boot auto-configuration for Spock

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
