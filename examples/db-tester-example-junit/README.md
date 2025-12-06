# DB Tester - JUnit Examples

This module contains example tests demonstrating the features of the DB Tester framework with JUnit. Each test class illustrates specific functionality through executable code and associated test data in CSV format.

## Overview

The example project demonstrates:

- **Convention-Based Testing** - Automatic CSV file resolution based on test class and method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Database Operations** - All supported operations (CLEAN_INSERT, INSERT, UPDATE, REFRESH, DELETE, etc.)
- **Multiple DataSources** - Testing with multiple database connections
- **Annotation Inheritance** - Inheriting test configuration from base classes
- **Nested Test Classes** - Convention-based loading in `@Nested` test classes

## Prerequisites

- Java 21 or later
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Usage

### Running Tests

Run all example tests:

```bash
./gradlew :examples:db-tester-example-junit:test
```

Run feature tests only:

```bash
./gradlew :examples:db-tester-example-junit:test --tests "example.feature.*"
```

Run database integration tests only:

```bash
./gradlew :examples:db-tester-example-junit:test --tests "example.database.*"
```

Run a specific test class:

```bash
./gradlew :examples:db-tester-example-junit:test --tests example.feature.MinimalExampleTest
```

Run with detailed logging:

```bash
./gradlew :examples:db-tester-example-junit:test --info
```

## Test Structure

Tests are organized into two main categories:

- **Feature Tests** (`example/feature/`) - Demonstrate framework features and capabilities
- **Database Integration Tests** (`example/database/`) - Validate compatibility with specific databases

### Feature Tests

| Test Class | Description |
|------------|-------------|
| `MinimalExampleTest` | Convention-based testing with minimal configuration |
| `ScenarioFilteringTest` | CSV row filtering using `[Scenario]` column marker |
| `AnnotationConfigurationTest` | Advanced annotation configuration with custom paths and scenarios |
| `ConfigurationCustomizationTest` | Framework convention customization via Configuration API |
| `ComprehensiveDataTypesTest` | Coverage of all CSV-representable data types |
| `NullAndEmptyValuesTest` | NULL value and empty string handling |
| `OperationVariationsTest` | All database operations (CLEAN_INSERT, INSERT, UPDATE, etc.) |
| `CustomExpectationPathsTest` | Custom expectation paths for flexible test data organization |
| `ProgrammaticAssertionApiTest` | Annotation-based and programmatic validation approaches |
| `PartialColumnValidationTest` | Partial column validation techniques |
| `CustomQueryValidationTest` | Custom query validation scenarios |
| `TableOrderingStrategiesTest` | Table ordering for foreign key constraints |
| `MultipleDataSourceTest` | Multiple named data sources in a single test |
| `InheritedAnnotationTest` | Annotation inheritance from base test class |
| `NestedConventionTest` | `@Nested` test classes with convention-based data loading |

### Database Integration Tests

| Test Class | Description |
|------------|-------------|
| `DerbyIntegrationTest` | Apache Derby integration |
| `HSQLDBIntegrationTest` | HSQLDB (HyperSQL) integration |
| `MySQLIntegrationTest` | MySQL integration using Testcontainers |
| `PostgreSQLIntegrationTest` | PostgreSQL integration using Testcontainers |
| `OracleIntegrationTest` | Oracle Database integration using Testcontainers |
| `MSSQLServerIntegrationTest` | Microsoft SQL Server integration using Testcontainers |

## Configuration

### CSV File Format

CSV files use standard format with optional scenario filtering:

```csv
[Scenario],ID,COLUMN1,COLUMN2
testScenario,1,Value1,100
testScenario,2,Value2,200
```

### CSV Conventions

| Element | Description |
|---------|-------------|
| `[Scenario]` column | Optional first column for scenario filtering |
| Column names | Must match database column names exactly (case-sensitive) |
| NULL values | Empty cells represent SQL NULL |
| Empty strings | Quoted empty string (`""`) represents empty string |
| Dates | ISO format: `2024-01-15` or `2024-01-15 10:30:00` |
| Booleans | `TRUE`/`FALSE` or `1`/`0` (case-insensitive) |
| Commas in values | Quote the entire value (`"Value, with comma"`) |
| Binary data | Base64 encoded with `[BASE64]` prefix |

### File Location

Test data files follow convention-based paths:

```
src/test/resources/
  example/feature/MinimalExampleTest/
    TABLE1.csv              # Preparation data
    expected/
      TABLE1.csv            # Expected data

  example/feature/ScenarioFilteringTest/
    TABLE1.csv              # Preparation data (shared with scenarios)
    expected/
      TABLE1.csv            # Expected data

  example/feature/AnnotationConfigurationTest/custom-location/
    TABLE1.csv              # Custom location example
    TABLE2.csv
    expected/
      TABLE1.csv
      TABLE2.csv
```

## Related Modules

- [db-tester-junit](../../db-tester-junit/) - JUnit extension

## Documentation

For detailed framework documentation, refer to the [main README](../../README.md).
