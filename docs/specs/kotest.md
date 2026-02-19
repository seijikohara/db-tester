---
title: "Kotest Integration - DB Tester"
description: "Integrate DB Tester with Kotest using AnnotationSpec and the DatabaseTestSupport interface."
---

# Kotest Integration

## Module

`db-tester-kotest`

## Extension Class

**Location**: `io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension`

**Type**: `TestCaseExtension` - Intercepts test case execution for data set and expected data set phases.

## Registration

**Simplified approach with `@DatabaseTest` (recommended)**:

The `@DatabaseTest` annotation registers `DatabaseTestExtension` automatically. The specification class must implement the `DatabaseTestSupport` interface:

```kotlin
@DatabaseTest
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    @BeforeAll
    fun setupSpec() {
        dataSource = createDataSource()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // Test implementation
    }
}
```

**Explicit extension registration**:

Register the extension in the `init` block. In Kotest 6, the `extensions()` method is final:

```kotlin
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()

    init {
        extensions(DatabaseTestExtension())
    }

    @BeforeAll
    fun setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // Test implementation
    }
}
```

## DatabaseTestSupport Interface

The `DatabaseTestSupport` interface provides the contract for database testing:

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `dbTesterRegistry` | `DataSourceRegistry` | Yes | Data source registration |
| `dbTesterConfiguration` | `Configuration` | No | Custom configuration (defaults to `Configuration.defaults()`) |

## DataSource Registration

Implement the `DatabaseTestSupport` interface and override `dbTesterRegistry`:

```kotlin
@DatabaseTest
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()

    @BeforeAll
    fun setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
        dbTesterRegistry.register("secondary", secondaryDataSource)
    }
}
```

## Configuration Customization

Override `dbTesterConfiguration` in the interface implementation:

```kotlin
@DatabaseTest
class UserRepositorySpec : AnnotationSpec(), DatabaseTestSupport {

    override val dbTesterRegistry = DataSourceRegistry()

    override val dbTesterConfiguration = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build()
}
```

## Test Method Naming

Use backtick method names for descriptive test names:

```kotlin
@Test
@DataSet
fun `should create user with email`() {
    // Scenario name: "should create user with email"
}
```

## AnnotationSpec Requirement

DB Tester requires `AnnotationSpec` style for Kotest integration because:
1. Annotations (`@DataSet`, `@ExpectedDataSet`) can be applied to test methods
2. Method resolution via reflection is reliable
3. Familiar JUnit-like structure for Java developers

## Related Specifications

- [Test Frameworks Overview](test-frameworks) - Supported frameworks summary
- [JUnit](junit) - JUnit integration
- [Spock](spock) - Spock integration
- [Spring Boot](spring-boot) - Spring Boot auto-configuration
- [Lifecycle](lifecycle) - Lifecycle hooks and executor classes
- [Annotations](annotations) - Annotation details
- [Configuration](configuration) - Configuration options
