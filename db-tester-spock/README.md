# DB Tester - Spock Module

This module provides [Spock Framework](https://spockframework.org/) integration for the DB Tester framework through a global extension.

## Overview

- **Global Extension** - `DatabaseTestExtension` registers via Spock extension mechanism
- **Method Interceptor** - `DatabaseTestInterceptor` manages test lifecycle
- **Lifecycle Management** - `SpockPreparationExecutor` and `SpockExpectationVerifier` execute preparation and expectation phases

## Architecture

```
db-tester-api (compile-time dependency)
        ↑
db-tester-spock
        ↓
db-tester-core (runtime dependency, loaded via ServiceLoader)
```

This module depends **only on `db-tester-api`** at compile time. The `db-tester-core` module is loaded at runtime via Java ServiceLoader mechanism.

## Requirements

- Java 21 or later
- Groovy 5 or later
- Spock Framework 2 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-spock:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-spock</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-spock).

## Usage

### Basic Example

```groovy
class UserRepositorySpec extends Specification {

    @Shared
    DataSource dataSource

    @Shared
    DataSourceRegistry dbTesterRegistry

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Preparation
    @Expectation
    def "should create user"() {
        when:
        userRepository.create(new User("Alice", "alice@example.com"))

        then:
        noExceptionThrown()
    }
}
```

The extension is registered via Spock Global Extension mechanism (`META-INF/services`). No manual registration is required.

### DataSource Registration

Register data sources in `setupSpec()` using a `@Shared` field:

```groovy
@Shared
DataSourceRegistry dbTesterRegistry

def setupSpec() {
    dbTesterRegistry = new DataSourceRegistry()
    dbTesterRegistry.registerDefault(dataSource)
    dbTesterRegistry.register("secondary", secondaryDataSource)
}
```

### Class-Level Annotations

Apply annotations at the class level for all feature methods:

```groovy
@Preparation
@Expectation
class UserRepositorySpec extends Specification {

    def "should create user"() {
        // Uses class-level annotations
    }
}
```

### Method-Level Annotations

Override class-level annotations at the method level:

```groovy
@Preparation(dataSets = @DataSet(resourceLocation = "custom/path"))
def "should create user with custom data"() {
    // Uses custom data location
}
```

### Scenario Filtering

Use the `[Scenario]` column to share CSV files across multiple features:

```csv
[Scenario],ID,NAME,EMAIL
should create user,1,Alice,alice@example.com
should update user,1,Alice Updated,alice.updated@example.com
```

Feature method names with spaces map directly to `[Scenario]` column values.

### Configuration Customization

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
            TableMergeStrategy.UNION_ALL // tableMergeStrategy
        )
    )
}
```

## Java Platform Module System (JPMS)

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.spock`

This module provides JPMS compatibility via the `Automatic-Module-Name` manifest attribute.

## Key Classes

| Class | Description |
|-------|-------------|
| [`DatabaseTestExtension`](src/main/groovy/io/github/seijikohara/dbtester/spock/extension/DatabaseTestExtension.groovy) | Global extension for automatic registration |
| [`DatabaseTestInterceptor`](src/main/groovy/io/github/seijikohara/dbtester/spock/extension/DatabaseTestInterceptor.groovy) | Method interceptor for test lifecycle management |
| [`SpockPreparationExecutor`](src/main/groovy/io/github/seijikohara/dbtester/spock/lifecycle/SpockPreparationExecutor.groovy) | Executes data preparation phase |
| [`SpockExpectationVerifier`](src/main/groovy/io/github/seijikohara/dbtester/spock/lifecycle/SpockExpectationVerifier.groovy) | Verifies database state after test execution |
| `SpockScenarioNameResolver` | Resolves scenario names from Spock feature methods |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (loaded at runtime) |
| [`db-tester-spock-spring-boot-starter`](../db-tester-spock-spring-boot-starter/) | Spring Boot auto-configuration |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
