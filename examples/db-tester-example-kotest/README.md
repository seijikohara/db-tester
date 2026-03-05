# DB Tester - Kotest Examples

This module contains example tests demonstrating the features of the DB Tester framework with Kotest.

## Overview

- **Convention-based testing** - CSV file resolution based on test class and method names
- **Scenario filtering** - CSV row filtering using `[Scenario]` column marker
- **Database operations** - All supported operations (CLEAN_INSERT, INSERT, UPDATE, UPSERT, DELETE)
- **Database integration** - Compatibility tests with Derby, HSQLDB, MySQL, PostgreSQL, Oracle, SQL Server,
  and Neo4j

## Prerequisites

- Java 21 or later
- Kotlin 2 or later
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Running Tests

Run all tests.

```bash
./gradlew :examples:db-tester-example-kotest:test
```

Run feature tests only.

```bash
./gradlew :examples:db-tester-example-kotest:test --tests "example.feature.*"
```

Run database integration tests only.

```bash
./gradlew :examples:db-tester-example-kotest:test --tests "example.database.*"
```

## Test Classes

### Feature Tests

| Test Class | Description |
|------------|-------------|
| `MinimalExampleSpec` | Convention-based testing with minimal configuration |
| `ScenarioFilteringSpec` | CSV row filtering using `[Scenario]` column |
| `AnnotationConfigurationSpec` | Advanced annotation configuration |
| `ColumnStrategyAnnotationSpec` | Annotation-based column comparison strategies using `@ColumnStrategy` |
| `ConfigurationCustomizationSpec` | Framework convention customization |
| `DatabaseTestAnnotationSpec` | Simplified `@DatabaseTest` annotation for automatic extension registration |
| `DataFormatSpec` | CSV, TSV, JSON, and YAML format support |
| `TableMergeStrategySpec` | FIRST, LAST, UNION, and UNION_ALL strategies |
| `TableOrderingStrategiesSpec` | Table ordering strategies (ALPHABETICAL, MANUAL) |
| `ComparisonStrategySpec` | Comparison strategies (STRICT, NUMERIC, CASE_INSENSITIVE, IGNORE, TIMESTAMP_FLEXIBLE, NOT_NULL, REGEX) |
| `ComprehensiveDataTypesSpec` | SQL data types support |
| `CustomExpectationPathsSpec` | Custom resource path configuration |
| `CustomQueryValidationSpec` | Custom SQL query validation |
| `InheritedAnnotationSpec` | Annotation inheritance from base classes |
| `MultipleDataSourceSpec` | Multiple DataSource support |
| `NullAndEmptyValuesSpec` | NULL and empty value handling |
| `OperationVariationsSpec` | All database operations (INSERT, UPDATE, DELETE) |
| `PartialColumnValidationSpec` | Partial column comparison |
| `ProgrammaticAssertionApiSpec` | Programmatic `DatabaseAssertion` API usage |
| `ErrorHandlingSpec` | Assertion failure scenarios and error messages |
| `ProgrammaticPreparationApiSpec` | Programmatic `DatabasePreparation` API for dynamic test data setup |
| `ComposedAnnotationSpec` | Composed meta-annotations combining `@DataSet` and `@ExpectedDataSet` |

### Database Integration Tests

| Test Class | Description |
|------------|-------------|
| `DerbyIntegrationSpec` | Apache Derby integration |
| `HSQLDBIntegrationSpec` | HSQLDB (HyperSQL) integration |
| `MSSQLServerIntegrationSpec` | SQL Server integration (Testcontainers) |
| `MySQLIntegrationSpec` | MySQL integration (Testcontainers) |
| `Neo4jIntegrationSpec` | Neo4j Graph Database integration (Testcontainers) |
| `OracleIntegrationSpec` | Oracle Database integration (Testcontainers) |
| `PostgreSQLIntegrationSpec` | PostgreSQL integration (Testcontainers) |

## Kotest-Specific Features

Test method names with backticks map directly to `[Scenario]` column values.

```kotlin
@Test
fun `should create active user`() { ... }  // Matches: should create active user
@Test
fun `should create inactive user`() { ... } // Matches: should create inactive user
```

## Related Modules

- [db-tester-kotest](../../db-tester-kotest/) - Kotest extension

## Documentation

For framework documentation, see the [main README](../../README.md).
