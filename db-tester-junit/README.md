# DB Tester - JUnit Module

This module provides [JUnit](https://junit.org/) integration for the DB Tester framework through `DatabaseTestExtension`.

## Overview

- **JUnit Extension** - `DatabaseTestExtension` implements `BeforeEachCallback` and `AfterEachCallback` for test lifecycle integration
- **Lifecycle Management** - `PreparationExecutor` and `ExpectationVerifier` execute preparation and expectation phases
- **ExtensionContext Integration** - Stores `DataSourceRegistry` and `Configuration` in JUnit `ExtensionContext`

## Architecture

```
db-tester-api (compile-time dependency)
        ↑
db-tester-junit
        ↓
db-tester-core (runtime dependency, loaded via ServiceLoader)
```

This module depends **only on `db-tester-api`** at compile time. The `db-tester-core` module is loaded at runtime via Java ServiceLoader mechanism.

## Requirements

- Java 21 or later
- JUnit 6 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-junit:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-junit</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit).

## Usage

### Basic Example

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setupDataSource(ExtensionContext context) {
        DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Test
    @DataSet
    @ExpectedDataSet
    void testCreateUser() {
        // Test implementation
    }
}
```

Register `DatabaseTestExtension` using `@ExtendWith`. DataSource registration is required in `@BeforeAll`.

### DataSource Registration

Register data sources in `@BeforeAll`:

```java
@BeforeAll
static void setup(ExtensionContext context) {
    DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(dataSource);
    registry.register("secondary", secondaryDataSource);
}
```

### Class-Level Annotations

Apply annotations at the class level for all test methods:

```java
@ExtendWith(DatabaseTestExtension.class)
@DataSet
@ExpectedDataSet
class UserRepositoryTest {

    @Test
    void testCreateUser() {
        // Uses class-level annotations
    }
}
```

### Method-Level Annotations

Override class-level annotations at the method level:

```java
@Test
@DataSet(sources = @DataSetSource(resourceLocation = "custom/path"))
void testWithCustomDataSet() {
    // Uses custom data location
}
```

### Scenario Filtering

Use the `[Scenario]` column to share CSV files across multiple tests:

```csv
[Scenario],ID,NAME,EMAIL
testCreateUser,1,Alice,alice@example.com
testUpdateUser,1,Alice Updated,alice.updated@example.com
```

Each test loads only rows matching its method name.

### Configuration Customization

```java
@BeforeAll
static void setup(ExtensionContext context) {
    ConventionSettings conventions = ConventionSettings.standard()
        .withScenarioMarker("[TestCase]")
        .withDataFormat(DataFormat.TSV);
    Configuration config = Configuration.withConventions(conventions);
    DatabaseTestExtension.setConfiguration(context, config);
}
```

## JPMS Support

**Module name**: `io.github.seijikohara.dbtester.junit`

```java
requires io.github.seijikohara.dbtester.junit;
```

## Key Classes

| Class | Description |
|-------|-------------|
| [`DatabaseTestExtension`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/extension/DatabaseTestExtension.java) | JUnit extension for test lifecycle integration |
| [`PreparationExecutor`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/lifecycle/PreparationExecutor.java) | Executes data preparation phase |
| [`ExpectationVerifier`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/lifecycle/ExpectationVerifier.java) | Verifies database state after test execution |
| [`JUnitScenarioNameResolver`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/spi/JUnitScenarioNameResolver.java) | Resolves scenario names from JUnit test methods |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (loaded at runtime) |
| [`db-tester-junit-spring-boot-starter`](../db-tester-junit-spring-boot-starter/) | Spring Boot auto-configuration |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
