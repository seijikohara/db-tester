# DB Tester - JUnit Spring Boot Starter Module

This module provides Spring Boot auto-configuration for the DB Tester framework with JUnit integration.

## Overview

- **Auto-Registration** - Spring-managed `DataSource` beans are automatically registered with DB Tester
- **Property-Based Configuration** - Configure conventions via `application.properties` or `application.yml`
- **Multiple DataSource Support** - Handles multiple DataSource beans with `@Primary` annotation support
- **JUnit Integration** - Extends `DatabaseTestExtension` with Spring Boot auto-configuration

## Architecture

```
db-tester-api (compile-time dependency)
        ↑
db-tester-junit
        ↑
db-tester-junit-spring-boot-starter
        ↓
db-tester-core (runtime dependency, loaded via ServiceLoader)
```

This module extends `db-tester-junit` with Spring Boot auto-configuration.

## Requirements

- Java 21 or later
- JUnit 6 or later
- Spring Boot 4 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-junit-spring-boot-starter</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit-spring-boot-starter).

## Usage

### Basic Example

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Preparation
    @Expectation
    void shouldSaveUser() {
        userRepository.save(new User("Alice", "alice@example.com"));
    }
}
```

Register `SpringBootDatabaseTestExtension` using `@ExtendWith`. DataSource is auto-registered from Spring context.

### Multiple DataSources

For multiple DataSource beans, use bean names in `@DataSet`:

```java
@Test
@Preparation(dataSets = @DataSet(dataSourceName = "primaryDataSource"))
@Expectation(dataSets = @DataSet(dataSourceName = "primaryDataSource"))
void testPrimaryDatabase() {
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

```java
@BeforeAll
static void setup(ExtensionContext context) {
    ConventionSettings conventions = ConventionSettings.standard()
        .withScenarioMarker("[TestCase]")
        .withDataFormat(DataFormat.TSV);
    Configuration config = Configuration.withConventions(conventions);
    SpringBootDatabaseTestExtension.setConfiguration(context, config);
}
```

## Java Platform Module System (JPMS)

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.junit.spring.autoconfigure`

This module provides JPMS compatibility via the `Automatic-Module-Name` manifest attribute.

## Key Classes

| Class | Description |
|-------|-------------|
| [`SpringBootDatabaseTestExtension`](src/main/java/io/github/seijikohara/dbtester/junit/spring/boot/autoconfigure/SpringBootDatabaseTestExtension.java) | JUnit extension with Spring Boot integration |
| [`DbTesterJUnitAutoConfiguration`](src/main/java/io/github/seijikohara/dbtester/junit/spring/boot/autoconfigure/DbTesterJUnitAutoConfiguration.java) | Spring Boot auto-configuration class |
| [`DbTesterProperties`](src/main/java/io/github/seijikohara/dbtester/junit/spring/boot/autoconfigure/DbTesterProperties.java) | Configuration properties binding |
| [`DataSourceRegistrar`](src/main/java/io/github/seijikohara/dbtester/junit/spring/boot/autoconfigure/DataSourceRegistrar.java) | Registers Spring-managed DataSources |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (loaded at runtime) |
| [`db-tester-junit`](../db-tester-junit/) | JUnit integration (base module) |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
