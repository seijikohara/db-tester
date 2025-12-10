# DB Tester - Spock Examples

This module contains example tests demonstrating the features of the DB Tester framework with Spock Framework.

## Overview

- **Convention-Based Testing** - CSV file resolution based on specification class and feature method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Database Operations** - All supported operations (CLEAN_INSERT, INSERT, UPDATE, REFRESH, DELETE)
- **Database Integration** - Compatibility tests with Derby, HSQLDB, MySQL, PostgreSQL, Oracle, SQL Server

## Prerequisites

- Java 21 or later
- Groovy 5 or later
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Usage

### Running Tests

```bash
./gradlew :examples:db-tester-example-spock:test
```

### Running Feature Tests Only

```bash
./gradlew :examples:db-tester-example-spock:test --tests "example.feature.*"
```

### Running Database Integration Tests Only

```bash
./gradlew :examples:db-tester-example-spock:test --tests "example.database.*"
```

## Specification Classes

### Feature Tests

| Specification | Description |
|---------------|-------------|
| `MinimalExampleSpec` | Convention-based testing with minimal configuration |
| `ScenarioFilteringSpec` | CSV row filtering using `[Scenario]` column |
| `AnnotationConfigurationSpec` | Advanced annotation configuration |
| `ConfigurationCustomizationSpec` | Framework convention customization |
| `DataFormatSpec` | CSV and TSV format support |
| `TableMergeStrategySpec` | FIRST, LAST, UNION, UNION_ALL strategies |
| `TableOrderingStrategiesSpec` | Table ordering strategies (ALPHABETICAL, MANUAL, etc.) |
| `ComparisonStrategySpec` | STRICT, NUMERIC, CASE_INSENSITIVE, IGNORE comparisons |
| `ComprehensiveDataTypesSpec` | Various SQL data types support |
| `CustomExpectationPathsSpec` | Custom resource path configuration |
| `CustomQueryValidationSpec` | Custom SQL query validation |
| `InheritedAnnotationSpec` | Annotation inheritance from base classes |
| `MultipleDataSourceSpec` | Multiple DataSource support |
| `NullAndEmptyValuesSpec` | NULL and empty value handling |
| `OperationVariationsSpec` | All database operations (INSERT, UPDATE, DELETE, etc.) |
| `PartialColumnValidationSpec` | Partial column comparison |
| `ProgrammaticAssertionApiSpec` | Programmatic `DatabaseAssertion` API usage |
| `ErrorHandlingSpec` | Assertion failure scenarios and error messages |

### Database Integration Tests

| Specification | Description |
|---------------|-------------|
| `DerbyIntegrationSpec` | Apache Derby integration |
| `HSQLDBIntegrationSpec` | HSQLDB (HyperSQL) integration |
| `MySQLIntegrationSpec` | MySQL integration (Testcontainers) |
| `PostgreSQLIntegrationSpec` | PostgreSQL integration (Testcontainers) |
| `OracleIntegrationSpec` | Oracle Database integration (Testcontainers) |
| `MSSQLServerIntegrationSpec` | SQL Server integration (Testcontainers) |

### Spock-Specific Features

Feature method names with spaces map directly to `[Scenario]` column values:

```groovy
def 'should create active user'() { ... }  // Matches: should create active user
def 'should create inactive user'() { ... } // Matches: should create inactive user
```

## Related Modules

- [db-tester-spock](../../db-tester-spock/) - Spock Framework extension

## Documentation

For detailed framework documentation, refer to the [main README](../../README.md).
