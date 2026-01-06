# DB Tester - Spock Spring Boot Starter Module

This module provides Spring Boot auto-configuration for the DB Tester framework with Spock integration.

## Overview

- **Auto-Registration** - Spring-managed `DataSource` beans are automatically registered with DB Tester
- **Property-Based Configuration** - Configure conventions via `application.properties` or `application.yml`
- **Multiple DataSource Support** - Handles multiple DataSource beans with `@Primary` annotation support
- **Spock Integration** - Extends `DatabaseTestExtension` with Spring Boot auto-configuration

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
- Spock 2 or later
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

    @DataSet
    @ExpectedDataSet
    def "should save user"() {
        when:
        userRepository.save(new User("Alice", "alice@example.com"))

        then:
        noExceptionThrown()
    }
}
```

Add `@SpringBootDatabaseTest` annotation to enable the extension. DataSource is auto-registered from Spring context.

### Multiple DataSources

For multiple DataSource beans, use bean names in `@DataSetSource`:

```groovy
@DataSet(dataSets = @DataSetSource(dataSourceName = "primaryDataSource"))
@ExpectedDataSet(dataSets = @DataSetSource(dataSourceName = "primaryDataSource"))
def "should test primary database"() {
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

```groovy
@Shared
Configuration dbTesterConfiguration

def setupSpec() {
    def conventions = ConventionSettings.standard()
        .withScenarioMarker('[TestCase]')
        .withDataFormat(DataFormat.TSV)
    dbTesterConfiguration = Configuration.withConventions(conventions)
}
```

## JPMS Support

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.spock.spring.autoconfigure`

## Key Classes

| Class | Description |
|-------|-------------|
| [`SpringBootDatabaseTest`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/SpringBootDatabaseTest.groovy) | Annotation to enable database testing with Spring Boot |
| [`SpringBootDatabaseTestExtension`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/SpringBootDatabaseTestExtension.groovy) | Annotation-driven extension activated by `@SpringBootDatabaseTest` |
| [`SpringBootDatabaseTestInterceptor`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/SpringBootDatabaseTestInterceptor.groovy) | Method interceptor for test lifecycle management |
| [`DbTesterSpockAutoConfiguration`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/DbTesterSpockAutoConfiguration.groovy) | Spring Boot auto-configuration class |
| [`DbTesterProperties`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/DbTesterProperties.groovy) | Configuration properties binding |
| [`DataSourceRegistrar`](src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/DataSourceRegistrar.groovy) | Registers Spring-managed DataSources |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (loaded at runtime) |
| [`db-tester-spock`](../db-tester-spock/) | Spock integration (base module) |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
