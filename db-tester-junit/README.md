# DB Tester - JUnit Module

This module provides JUnit integration for the DB Tester framework. It provides annotation-driven data preparation and validation using convention-based test data management.

## Overview

The JUnit module includes:

- **JUnit Extension** - `DatabaseTestExtension` for test lifecycle integration
- **Lifecycle Management** - `PreparationExecutor` and `ExpectationVerifier` for automatic execution of preparation and expectation phases
- **ExtensionContext Integration** - Integration with JUnit extension model

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

Replace `VERSION` with the latest version from [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit).

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
    @Preparation
    @Expectation
    void testCreateUser() {
        // Test implementation
    }
}
```

### Extension Registration

Register the extension at the class level:

```java
@ExtendWith(DatabaseTestExtension.class)
class MyDatabaseTest {
    // ...
}
```

### DataSource Registration

Register data sources in `@BeforeAll`:

```java
@BeforeAll
static void setup(ExtensionContext context) {
    DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(dataSource);
    // Or with a name for multiple data sources
    registry.register("secondary", secondaryDataSource);
}
```

### Annotation Usage

Use annotations at class or method level:

```java
// Class-level: applies to all test methods
@Preparation
@Expectation
class UserRepositoryTest {

    @Test
    void testCreateUser() {
        // Uses class-level @Preparation and @Expectation
    }

    @Test
    @Preparation  // Method-level override
    void testWithCustomPreparation() {
        // Uses method-level @Preparation
    }
}
```

## Configuration

### Convention-Based Loading

Test data files are resolved based on test class and method names:

```
src/test/resources/
  com/example/
    UserRepositoryTest/           # Test class name
      testCreateUser/             # Test method name (optional)
        USERS.csv                 # Preparation data
        expected/                 # Expectation directory
          USERS.csv               # Expected data
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

Customize conventions via Configuration API:

```java
@BeforeAll
static void setup(ExtensionContext context) {
    Configuration config = Configuration.builder()
        .conventionSettings(
            ConventionSettings.builder()
                .scenarioMarker(ScenarioMarker.of("[TestCase]"))
                .expectationSuffix("expected")
                .build()
        )
        .build();
    DatabaseTestExtension.setConfiguration(context, config);
}
```

### Java Platform Module System (JPMS)

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.junit`

This module provides JPMS compatibility via the `Automatic-Module-Name` manifest attribute.

## Key Classes

| Class | Description |
|-------|-------------|
| [`DatabaseTestExtension`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/extension/DatabaseTestExtension.java) | JUnit extension for test lifecycle integration |
| [`PreparationExecutor`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/lifecycle/PreparationExecutor.java) | Executes data preparation phase |
| [`ExpectationVerifier`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/lifecycle/ExpectationVerifier.java) | Verifies database state after test execution |
| `JUnitScenarioNameResolver` | Resolves scenario names from JUnit test methods |

## Related Modules

- [db-tester-core](../db-tester-core/) - Core API and implementation
- [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) - Spring Boot auto-configuration

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
