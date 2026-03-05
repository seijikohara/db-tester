---
title: "Spock Integration - DB Tester"
description: "Integrate DB Tester with Spock using annotation-driven extensions and the DatabaseTestSupport trait."
---

# Spock Integration

## Module

`db-tester-spock`

## Extension Class

**Location**: `io.github.seijikohara.dbtester.spock.extension.DatabaseTestExtension`

**Type**: Annotation-driven extension (`IAnnotationDrivenExtension<DatabaseTest>`)

## Registration

Add `@DatabaseTest` to the specification class and implement the `DatabaseTestSupport` trait:

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification implements DatabaseTestSupport {

    DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()

    def setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
    }

    @DataSet
    @ExpectedDataSet
    def 'should create user'() {
        // Test implementation
    }
}
```

## DatabaseTestSupport Trait

The `DatabaseTestSupport` trait provides the contract for database testing:

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `dbTesterRegistry` | `DataSourceRegistry` | Yes | Data source registration |
| `dbTesterConfiguration` | `Configuration` | No | Custom configuration (defaults to `Configuration.defaults()`) |

## Configuration Customization

Override `getDbTesterConfiguration()` in the specification:

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification implements DatabaseTestSupport {

    DataSourceRegistry dbTesterRegistry = new DataSourceRegistry()

    Configuration dbTesterConfiguration = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build()

    def setupSpec() {
        dbTesterRegistry.registerDefault(dataSource)
    }

    @DataSet
    @ExpectedDataSet
    def 'should create user'() { }
}
```

## Feature Method Naming

The scenario name derives from the feature method:

```groovy
@DataSet
def 'should create user with email'() {
    // Scenario name: "should create user with email"
}
```

## Data-Driven Tests

For parameterized tests with `where:` blocks, Spock uses the iteration name:

```groovy
@DataSet
def 'should process #status order'() {
    expect:
    // Test implementation

    where:
    status << ['PENDING', 'COMPLETED']
}
```

Scenario names: `"should process PENDING order"`, `"should process COMPLETED order"`

## Related Specifications

- [Test Frameworks Overview](test-frameworks) - Supported frameworks summary
- [JUnit](junit) - JUnit integration
- [Kotest](kotest) - Kotest integration
- [Spring Boot](spring-boot) - Spring Boot auto-configuration
- [Lifecycle](lifecycle) - Lifecycle hooks and executor classes
- [Annotations](annotations) - Annotation details
- [Configuration](configuration) - Configuration options
