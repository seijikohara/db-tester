# DB Tester - Kotest Spring Boot Starter Module

This module provides Spring Boot auto-configuration for the DB Tester framework with Kotest integration.

## Overview

- **Auto-Registration** - Spring-managed `DataSource` beans are automatically registered with DB Tester
- **Property-Based Configuration** - Configure conventions via `application.properties` or `application.yml`
- **Multiple DataSource Support** - Handles multiple DataSource beans with `@Primary` annotation support
- **Kotest Integration** - Extends `DatabaseTestExtension` with Spring Boot auto-configuration

## Architecture

```
db-tester-api (compile-time dependency)
        ↑
db-tester-kotest
        ↑
db-tester-kotest-spring-boot-starter
        ↓
db-tester-core (runtime dependency, loaded via ServiceLoader)
```

This module extends `db-tester-kotest` with Spring Boot auto-configuration.

## Requirements

- Java 21 or later
- Kotlin 2 or later
- Kotest 6 or later
- Spring Boot 4 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-kotest-spring-boot-starter:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-kotest-spring-boot-starter</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-kotest-spring-boot-starter).

## Usage

### Basic Example

```kotlin
@SpringBootTest
class UserRepositorySpec : AnnotationSpec() {

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        extensions(SpringBootDatabaseTestExtension())
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should save user`() {
        userRepository.save(User("Alice", "alice@example.com"))
    }
}
```

Register `SpringBootDatabaseTestExtension` in the `init` block. DataSource is auto-registered from Spring context.

### Multiple DataSources

For multiple DataSource beans, use bean names in `@DataSetSource`:

```kotlin
@Test
@DataSet(sources = [DataSetSource(dataSourceName = "primaryDataSource")])
@ExpectedDataSet(sources = [DataSetSource(dataSourceName = "primaryDataSource")])
fun `should test primary database`() {
    // Test with primary DataSource
}
```

## Configuration

### Application Properties

Configure in `application.properties` or `application.yml`:

```yaml
db-tester:
  enabled: true
  auto-register-data-sources: true

  convention:
    base-directory:
    expectation-suffix: /expected
    scenario-marker: "[Scenario]"
    data-format: CSV
    table-merge-strategy: UNION_ALL

  operation:
    preparation: CLEAN_INSERT
    expectation: NONE
```

### Property Reference

| Property | Description | Default |
|----------|-------------|---------|
| `db-tester.enabled` | Enable/disable auto-configuration | `true` |
| `db-tester.auto-register-data-sources` | Auto-register Spring DataSources | `true` |
| `db-tester.convention.base-directory` | Base directory for datasets | `null` (classpath) |
| `db-tester.convention.expectation-suffix` | Suffix for expectation datasets | `/expected` |
| `db-tester.convention.scenario-marker` | Column name for scenario filtering | `[Scenario]` |
| `db-tester.convention.data-format` | Dataset file format (`CSV`, `TSV`) | `CSV` |
| `db-tester.convention.table-merge-strategy` | Table merge strategy | `UNION_ALL` |
| `db-tester.operation.preparation` | Default preparation operation | `CLEAN_INSERT` |
| `db-tester.operation.expectation` | Default expectation operation | `NONE` |

### Programmatic Configuration

Override properties via Configuration API:

```kotlin
init {
    val conventions = ConventionSettings.standard()
        .withScenarioMarker("[TestCase]")
        .withDataFormat(DataFormat.TSV)
    val config = Configuration.withConventions(conventions)

    extensions(SpringBootDatabaseTestExtension(configuration = config))
}
```

## JPMS Support

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.kotest.spring.autoconfigure`

## Key Classes

| Class | Description |
|-------|-------------|
| [`SpringBootDatabaseTestExtension`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/spring/boot/autoconfigure/SpringBootDatabaseTestExtension.kt) | TestCaseExtension with Spring Boot integration |
| [`DbTesterKotestAutoConfiguration`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/spring/boot/autoconfigure/DbTesterKotestAutoConfiguration.kt) | Spring Boot auto-configuration class |
| [`DbTesterProperties`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/spring/boot/autoconfigure/DbTesterProperties.kt) | Configuration properties binding |
| [`DataSourceRegistrar`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/spring/boot/autoconfigure/DataSourceRegistrar.kt) | Registers Spring-managed DataSources |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (loaded at runtime) |
| [`db-tester-kotest`](../db-tester-kotest/) | Kotest integration (base module) |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
