# DB Tester - Spock Module

This module provides [Spock Framework](https://spockframework.org/) integration for the DB Tester framework. It provides annotation-driven database testing in Spock specifications using the same annotations as JUnit.

## Overview

The Spock module includes:

- **Global Extension** - `DatabaseTestExtension` for automatic registration via Spock's extension mechanism
- **Method Interceptor** - `DatabaseTestInterceptor` for test lifecycle management
- **Lifecycle Management** - `SpockPreparationExecutor` and `SpockExpectationVerifier` for automatic execution of preparation and expectation phases

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

Replace `VERSION` with the latest version from [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-spock).

## Usage

### Basic Example

```groovy
class UserRepositorySpec extends Specification {

    @Shared
    DataSource dataSource

    @Shared
    DataSourceRegistry dbTesterRegistry

    def setupSpec() {
        // Initialize dataSource and registry
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Preparation
    @Expectation
    def "should create user"() {
        when:
        userRepository.create(new User("Alice", "alice@example.com"))

        then:
        // Database state is verified by @Expectation
        noExceptionThrown()
    }
}
```

### Automatic Extension Registration

The extension is automatically registered via Spock's Global Extension mechanism (META-INF/services). No manual registration is required.

### Annotation Support

Use annotations from `db-tester-core`:

```groovy
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.DataSet
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

    def "should update user"() {
        // Uses class-level annotations
    }
}
```

### Method-Level Annotations

Override class-level annotations at the method level:

```groovy
class UserRepositorySpec extends Specification {

    @Preparation
    @Expectation
    def "should create user"() {
        // Uses method-level annotations
    }

    @Preparation(dataSets = @DataSet(resourceLocation = "custom/path"))
    def "should create user with custom data"() {
        // Uses custom data location
    }
}
```

### DataSource Registration

Register data sources in `setupSpec()` using a `@Shared` field:

```groovy
@Shared
DataSourceRegistry dbTesterRegistry

def setupSpec() {
    dbTesterRegistry = new DataSourceRegistry()
    dbTesterRegistry.registerDefault(dataSource)
    // Or with a name for multiple data sources
    dbTesterRegistry.register("secondary", secondaryDataSource)
}
```

## Configuration

### Convention-Based Loading

Test data files are resolved based on specification class and feature method names:

```
src/test/resources/
  com/example/
    UserRepositorySpec/               # Specification class name
      should create user/             # Feature method name (spaces replaced)
        USERS.csv                     # Preparation data
        expected/                     # Expectation directory
          USERS.csv                   # Expected data
```

### Scenario Filtering

Use the `[Scenario]` column to share CSV files across multiple features:

```csv
[Scenario],ID,NAME,EMAIL
should create user,1,Alice,alice@example.com
should update user,1,Alice Updated,alice.updated@example.com
```

Each feature loads only rows matching its method name.

### Configuration Customization

Customize conventions via Configuration API using a `@Shared` field:

```groovy
@Shared
Configuration dbTesterConfiguration

def setupSpec() {
    dbTesterConfiguration = Configuration.builder()
        .conventionSettings(
            ConventionSettings.builder()
                .scenarioMarker(ScenarioMarker.of("[TestCase]"))
                .expectationSuffix("expected")
                .build()
        )
        .build()
}
```

### Java Platform Module System (JPMS)

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

- [db-tester-core](../db-tester-core/) - Core API and implementation
- [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) - Spring Boot auto-configuration

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
