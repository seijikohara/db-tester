# DB Tester - Spock Framework Examples

This module contains example tests demonstrating the features of the DB Tester framework with Spock Framework. Each specification class illustrates specific functionality through executable code and associated test data in CSV format.

## Overview

The example project demonstrates:

- **Convention-Based Testing** - Automatic CSV file resolution based on specification class and feature method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Database Operations** - All supported operations (CLEAN_INSERT, INSERT, UPDATE, REFRESH, DELETE, etc.)
- **Multiple DataSources** - Testing with multiple database connections
- **Annotation Inheritance** - Inheriting test configuration from base classes
- **Testcontainers Integration** - Using `@Testcontainers` with `@Shared` containers

## Prerequisites

- Java 21 or later
- Groovy 5 or later
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Usage

### Running Tests

Run all Spock examples:

```bash
./gradlew :examples:db-tester-example-spock:test
```

Run feature tests only:

```bash
./gradlew :examples:db-tester-example-spock:test --tests "example.feature.*"
```

Run database integration tests (embedded databases only):

```bash
./gradlew :examples:db-tester-example-spock:test --tests "example.database.derby.*" --tests "example.database.hsqldb.*"
```

Run Testcontainers integration tests (requires Docker):

```bash
./gradlew :examples:db-tester-example-spock:test --tests "example.database.pgsql.*" --tests "example.database.mysql.*"
```

Run a specific specification class:

```bash
./gradlew :examples:db-tester-example-spock:test --tests example.feature.MinimalExampleSpec
```

Run with detailed logging:

```bash
./gradlew :examples:db-tester-example-spock:test --info
```

## Test Structure

Tests are organized into two main categories:

- **Feature Tests** (`example/feature/`) - Demonstrate framework features and capabilities
- **Database Integration Tests** (`example/database/`) - Validate compatibility with specific databases

### Feature Tests

| Specification | Description |
|---------------|-------------|
| `MinimalExampleSpec` | Convention-based testing with minimal configuration |
| `ScenarioFilteringSpec` | CSV row filtering using `[Scenario]` column marker |
| `AnnotationConfigurationSpec` | Advanced annotation configuration with custom paths and scenarios |
| `ConfigurationCustomizationSpec` | Framework convention customization via Configuration API |
| `ComprehensiveDataTypesSpec` | Coverage of all CSV-representable data types |
| `NullAndEmptyValuesSpec` | NULL value and empty string handling |
| `OperationVariationsSpec` | All database operations (CLEAN_INSERT, INSERT, UPDATE, etc.) |
| `CustomExpectationPathsSpec` | Custom expectation paths for flexible test data organization |
| `ProgrammaticAssertionApiSpec` | Annotation-based and programmatic validation approaches |
| `PartialColumnValidationSpec` | Partial column validation techniques |
| `CustomQueryValidationSpec` | Custom query validation scenarios |
| `TableOrderingStrategiesSpec` | Table ordering for foreign key constraints |
| `MultipleDataSourceSpec` | Multiple named data sources in a single specification |
| `InheritedAnnotationSpec` | Annotation inheritance from base specification class |

### Database Integration Tests

| Specification | Description |
|---------------|-------------|
| `DerbyIntegrationSpec` | Apache Derby integration |
| `HSQLDBIntegrationSpec` | HSQLDB (HyperSQL) integration |
| `MySQLIntegrationSpec` | MySQL integration using Testcontainers |
| `PostgreSQLIntegrationSpec` | PostgreSQL integration using Testcontainers |
| `OracleIntegrationSpec` | Oracle Database integration using Testcontainers |
| `MSSQLServerIntegrationSpec` | Microsoft SQL Server integration using Testcontainers |

## Configuration

### CSV File Format

CSV files use standard format with optional scenario filtering:

```csv
[Scenario],ID,COLUMN1,COLUMN2
should create active user,1,Value1,100
should create inactive user,2,Value2,200
```

### CSV Conventions

| Element | Description |
|---------|-------------|
| `[Scenario]` column | Optional first column for scenario filtering by feature method name |
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
  example/feature/MinimalExampleSpec/
    TABLE1.csv              # Preparation data
    expected/
      TABLE1.csv            # Expected data

  example/feature/ScenarioFilteringSpec/
    TABLE1.csv              # Preparation data (shared with scenarios)
    expected/
      TABLE1.csv            # Expected data

  example/feature/AnnotationConfigurationSpec/custom-location/
    TABLE1.csv              # Custom location example
    TABLE2.csv
    expected/
      TABLE1.csv
      TABLE2.csv
```

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
