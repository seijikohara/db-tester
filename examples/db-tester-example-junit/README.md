# DB Tester - JUnit Examples

This module contains example tests demonstrating the features of the DB Tester framework with JUnit.

## Overview

- **Convention-Based Testing** - CSV file resolution based on test class and method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Database Operations** - All supported operations (CLEAN_INSERT, INSERT, UPDATE, REFRESH, DELETE)
- **Database Integration** - Compatibility tests with Derby, HSQLDB, MySQL, PostgreSQL, Oracle, SQL Server, Neo4j

## Prerequisites

- Java 21 or later
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Usage

### Running Tests

```bash
./gradlew :examples:db-tester-example-junit:test
```

### Running Feature Tests Only

```bash
./gradlew :examples:db-tester-example-junit:test --tests "example.feature.*"
```

### Running Database Integration Tests Only

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
| `ConfigurationCustomizationTest` | Framework convention customization |
| `DataFormatTest` | CSV and TSV format support |
| `TableMergeStrategyTest` | FIRST, LAST, UNION, UNION_ALL strategies |
| `TableOrderingStrategiesTest` | Table ordering strategies (ALPHABETICAL, MANUAL, etc.) |
| `ComparisonStrategyTest` | STRICT, NUMERIC, CASE_INSENSITIVE, IGNORE comparisons |
| `ComprehensiveDataTypesTest` | Various SQL data types support |
| `CustomExpectationPathsTest` | Custom resource path configuration |
| `CustomQueryValidationTest` | Custom SQL query validation |
| `InheritedAnnotationTest` | Annotation inheritance from base classes |
| `MultipleDataSourceTest` | Multiple DataSource support |
| `NestedConventionTest` | `@Nested` test classes with convention-based loading |
| `NullAndEmptyValuesTest` | NULL and empty value handling |
| `OperationVariationsTest` | All database operations (INSERT, UPDATE, DELETE, etc.) |
| `PartialColumnValidationTest` | Partial column comparison |
| `ProgrammaticAssertionApiTest` | Programmatic `DatabaseAssertion` API usage |
| `ErrorHandlingTest` | Assertion failure scenarios and error messages |

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

JUnit method names are used directly as `[Scenario]` column values:

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

For detailed framework documentation, refer to the [main README](../../README.md).
