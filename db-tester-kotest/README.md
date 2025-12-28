# DB Tester - Kotest Module

This module provides [Kotest](https://kotest.io/) integration for the DB Tester framework through `DatabaseTestExtension`.

## Overview

- **TestCaseExtension** - `DatabaseTestExtension` implements `TestCaseExtension` for test lifecycle integration
- **Lifecycle Management** - `KotestPreparationExecutor` and `KotestExpectationVerifier` execute preparation and expectation phases
- **AnnotationSpec Support** - Works with Kotest `AnnotationSpec` style for annotation-based testing

## Architecture

```
db-tester-api (compile-time dependency)
        ↑
db-tester-kotest
        ↓
db-tester-core (runtime dependency, loaded via ServiceLoader)
```

This module depends **only on `db-tester-api`** at compile time. The `db-tester-core` module is loaded at runtime via Java ServiceLoader mechanism.

## Requirements

- Java 21 or later
- Kotlin 2 or later
- Kotest 6 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-kotest:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-kotest</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-kotest).

## Usage

### Basic Example

```kotlin
class UserRepositorySpec : AnnotationSpec() {

    private val registry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    @BeforeAll
    fun setupSpec() {
        dataSource = createDataSource()
        registry.registerDefault(dataSource)
    }

    @Test
    @Preparation
    @Expectation
    fun `should create user`() {
        userRepository.create(User("Alice", "alice@example.com"))
    }
}
```

Register `DatabaseTestExtension` in the `init` block. DataSource registration is required in `@BeforeAll`.

### DataSource Registration

Register data sources using a `registryProvider` lambda:

```kotlin
private val registry = DataSourceRegistry()

init {
    extensions(DatabaseTestExtension(registryProvider = { registry }))
}

@BeforeAll
fun setupSpec() {
    registry.registerDefault(dataSource)
    registry.register("secondary", secondaryDataSource)
}
```

### Class-Level Annotations

Apply annotations at the class level for all test methods:

```kotlin
@Preparation
@Expectation
class UserRepositorySpec : AnnotationSpec() {

    @Test
    fun `should create user`() {
        // Uses class-level annotations
    }
}
```

### Method-Level Annotations

Override class-level annotations at the method level:

```kotlin
@Test
@Preparation(dataSets = [DataSet(resourceLocation = "custom/path")])
fun `should create user with custom data`() {
    // Uses custom data location
}
```

### Scenario Filtering

Use the `[Scenario]` column to share CSV files across multiple tests:

```csv
[Scenario],ID,NAME,EMAIL
should create user,1,Alice,alice@example.com
should update user,1,Alice Updated,alice.updated@example.com
```

Test method names with backticks map directly to `[Scenario]` column values.

### Configuration Customization

```kotlin
init {
    val conventions = ConventionSettings.standard()
        .withScenarioMarker("[TestCase]")
        .withDataFormat(DataFormat.TSV)
    val config = Configuration.withConventions(conventions)

    extensions(DatabaseTestExtension(
        registryProvider = { registry },
        configuration = config
    ))
}
```

## Java Platform Module System (JPMS)

**Automatic-Module-Name**: `io.github.seijikohara.dbtester.kotest`

This module provides JPMS compatibility via the `Automatic-Module-Name` manifest attribute.

## Key Classes

| Class | Description |
|-------|-------------|
| [`DatabaseTestExtension`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/extension/DatabaseTestExtension.kt) | TestCaseExtension for test lifecycle integration |
| [`KotestPreparationExecutor`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/lifecycle/KotestPreparationExecutor.kt) | Executes data preparation phase |
| [`KotestExpectationVerifier`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/lifecycle/KotestExpectationVerifier.kt) | Verifies database state after test execution |
| [`KotestScenarioNameResolver`](src/main/kotlin/io/github/seijikohara/dbtester/kotest/spi/KotestScenarioNameResolver.kt) | Resolves scenario names from Kotest test methods |

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (loaded at runtime) |
| [`db-tester-kotest-spring-boot-starter`](../db-tester-kotest-spring-boot-starter/) | Spring Boot auto-configuration |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
