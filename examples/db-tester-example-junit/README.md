# DB Tester - JUnit Examples

This module contains example tests demonstrating the features of the DB Tester framework with JUnit.

## Overview

- **Convention-Based Testing** - CSV file resolution based on test class and method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Database Operations** - All supported operations (CLEAN_INSERT, INSERT, UPDATE, REFRESH, DELETE)
- **Database Integration** - Compatibility tests with Derby, HSQLDB, MySQL, PostgreSQL, Oracle, SQL Server

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
| `ComparisonStrategyTest` | STRICT, NUMERIC, CASE_INSENSITIVE, IGNORE comparisons |

### Database Integration Tests

| Test Class | Description |
|------------|-------------|
| `DerbyIntegrationTest` | Apache Derby integration |
| `HSQLDBIntegrationTest` | HSQLDB (HyperSQL) integration |
| `MySQLIntegrationTest` | MySQL integration (Testcontainers) |
| `PostgreSQLIntegrationTest` | PostgreSQL integration (Testcontainers) |
| `OracleIntegrationTest` | Oracle Database integration (Testcontainers) |
| `MSSQLServerIntegrationTest` | SQL Server integration (Testcontainers) |

## Related Modules

- [db-tester-junit](../../db-tester-junit/) - JUnit extension

## Documentation

For detailed framework documentation, refer to the [main README](../../README.md).
