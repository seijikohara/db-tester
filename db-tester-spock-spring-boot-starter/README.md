# DB Tester - Spock Spring Boot Starter Module

This module provides Spring Boot auto-configuration for the DB Tester framework with Spock Framework integration.

## Overview

- **Auto-Registration** - Spring-managed `DataSource` beans are registered with DB Tester
- **Property-Based Configuration** - Configure conventions via `application.properties`
- **Multiple DataSource Support** - Handles multiple DataSource beans with `@Primary` annotation support
- **Spock Integration** - Integrates with Spock Framework specification syntax

## Architecture

```
db-tester-api (compile-time dependency)
        ↑
db-tester-spock
        ↑
db-tester-spock-spring-boot-starter
        ↓
db-tester-core (runtime dependency, loaded via ServiceLoader)
```

This module extends `db-tester-spock` with Spring Boot auto-configuration.

## Requirements

- Java 21 or later
- Groovy 5 or later
- Spock Framework 2 or later
- Spring Boot 4 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-spock-spring-boot-starter:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-spock-spring-boot-starter</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-spock-spring-boot-starter).

## Usage

### Basic Example

```groovy
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositorySpec extends Specification {

    @Autowired
    UserRepository userRepository

    @Preparation
    @Expectation
    def "should save user"() {
        when:
        userRepository.save(new User("Alice", "alice@example.com"))

        then:
        noExceptionThrown()
    }
}
```

Add `@SpringBootDatabaseTest` annotation to enable the extension. DataSource is auto-registered from Spring context via auto-configuration.

### Multiple DataSources

For multiple DataSource beans, use bean names in `@DataSet`:

```groovy
@SpringBootTest
@SpringBootDatabaseTest
class MultiDataSourceSpec extends Specification {

    @Preparation(dataSets = @DataSet(dataSourceName = "primaryDataSource"))
    @Expectation(dataSets = @DataSet(dataSourceName = "primaryDataSource"))
    def "should test primary database"() {
        // Test with primary DataSource
    }
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

```groovy
@Shared
Configuration dbTesterConfiguration

def setupSpec() {
    dbTesterConfiguration = Configuration.withConventions(
        new ConventionSettings(
            null,                        // baseDirectory (null for classpath)
            '/expected',                 // expectationSuffix
            '[TestCase]',                // scenarioMarker
            DataFormat.CSV,              // dataFormat
            TableMergeStrategy.UNION_ALL, // tableMergeStrategy
            'load-order.txt'             // loadOrderFileName
        )
    )
}
```

## Java Platform Module System (JPMS)

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.spock.spring.autoconfigure`

This module provides JPMS compatibility via the `Automatic-Module-Name` manifest attribute.

## Key Classes

| Class | Description |
|-------|-------------|
| [`SpringBootDatabaseTest`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/SpringBootDatabaseTest.groovy) | Annotation to enable database testing with Spring Boot |
| [`SpringBootDatabaseTestExtension`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/SpringBootDatabaseTestExtension.groovy) | Annotation-driven extension activated by `@SpringBootDatabaseTest` |
| [`SpringBootDatabaseTestInterceptor`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/SpringBootDatabaseTestInterceptor.groovy) | Method interceptor for test lifecycle management |
| [`DbTesterSpockAutoConfiguration`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/DbTesterSpockAutoConfiguration.groovy) | Spring Boot auto-configuration class |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (loaded at runtime) |
| [`db-tester-spock`](../db-tester-spock/) | Spock Framework integration (base module) |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
