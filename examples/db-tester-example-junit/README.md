# DB Tester - JUnit Examples

This module contains example tests demonstrating the features of the DB Tester framework with JUnit.

## Overview

- **Convention-based testing** - CSV file resolution based on test class and method names
- **Scenario filtering** - CSV row filtering using `[Scenario]` column marker
- **Database operations** - All supported operations (CLEAN_INSERT, INSERT, UPDATE, UPSERT, DELETE)
- **Database integration** - Compatibility tests with Derby, HSQLDB, MySQL, PostgreSQL, Oracle, SQL Server,
  and Neo4j

## Prerequisites

- Java 21 or later
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Running Tests

Run all tests.

```bash
./gradlew :examples:db-tester-example-junit:test
```

Run feature tests only.

```bash
./gradlew :examples:db-tester-example-junit:test --tests "example.feature.*"
```

Run database integration tests only.

```bash
./gradlew :examples:db-tester-example-junit:test --tests "example.database.*"
```

## Test Classes

### Feature Tests

| Test Class | Description |
|------------|-------------|
| `MinimalExampleTest` | Convention-based testing with minimal configuration |
| `ScenarioFilteringTest` | CSV row filtering using `[Scenario]` column |
| `AnnotationConfigurationTest` | Advanced annotation configuration |
| `ColumnStrategyAnnotationTest` | Annotation-based column comparison strategies using `@ColumnStrategy` |
| `ConfigurationCustomizationTest` | Framework convention customization |
| `DataFormatTest` | AUTO detection, CSV, TSV, JSON, and YAML format support |
| `DataSetExportTest` | `DataSetExporter` API for exporting database state to CSV, JSON, and YAML files |
| `TableMergeStrategyTest` | FIRST, LAST, UNION, and UNION_ALL strategies |
| `TableOrderingStrategiesTest` | Table ordering strategies (ALPHABETICAL, MANUAL) |
| `ComparisonStrategyTest` | All comparison strategies (STRICT, NUMERIC, CASE_INSENSITIVE, IGNORE, TIMESTAMP_FLEXIBLE, DATE_FLEXIBLE, JSON_EQUIVALENT, NOT_NULL, REGEX) |
| `ComprehensiveDataTypesTest` | SQL data types support |
| `CustomExpectationPathsTest` | Custom resource path configuration |
| `CustomQueryValidationTest` | Custom SQL query validation |
| `InheritedAnnotationTest` | Annotation inheritance from base classes |
| `MultipleDataSourceTest` | Multiple DataSource support |
| `NestedConventionTest` | `@Nested` test classes with convention-based loading |
| `NullAndEmptyValuesTest` | NULL and empty value handling |
| `OperationVariationsTest` | All database operations (INSERT, UPDATE, DELETE) |
| `PartialColumnValidationTest` | Partial column comparison |
| `ProgrammaticAssertionApiTest` | Programmatic `DatabaseAssertion` API usage |
| `TemplateExpressionTest` | Template expression processing for dynamic test data (UUID, sequence, timestamp, Faker) |
| `ErrorHandlingTest` | Assertion failure scenarios and error messages |
| `ProgrammaticPreparationApiTest` | Programmatic `DatabasePreparation` API for dynamic test data setup |
| `ComposedAnnotationTest` | Composed meta-annotations combining `@DataSet` and `@ExpectedDataSet` |

### Database Integration Tests

| Test Class | Description |
|------------|-------------|
| `DerbyIntegrationTest` | Apache Derby integration |
| `HSQLDBIntegrationTest` | HSQLDB (HyperSQL) integration |
| `MSSQLServerIntegrationTest` | SQL Server integration (Testcontainers) |
| `MySQLIntegrationTest` | MySQL integration (Testcontainers) |
| `Neo4jIntegrationTest` | Neo4j Graph Database integration (Testcontainers) |
| `OracleIntegrationTest` | Oracle Database integration (Testcontainers) |
| `PostgreSQLIntegrationTest` | PostgreSQL integration (Testcontainers) |

## JUnit-Specific Features

JUnit method names map directly to `[Scenario]` column values.

```java
@Test
void testCreateActiveUser() { ... }  // Matches: testCreateActiveUser
@Test
void testCreateInactiveUser() { ... } // Matches: testCreateInactiveUser
```

Nested test classes with `@Nested` annotation inherit parent class conventions.

## Related Modules

- [db-tester-junit](../../db-tester-junit/) - JUnit extension

## Documentation

For framework documentation, see the [main README](../../README.md).
