# DB Tester - Examples

This directory contains example tests demonstrating the features of the DB Tester framework. Each example module illustrates specific functionality through executable code and associated test data in CSV format.

## Overview

The example modules demonstrate:

- **Convention-Based Testing** - Automatic CSV file resolution based on test class and method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Framework Integration** - JUnit, Spock Framework, and Spring Boot integration
- **Database Compatibility** - Support for multiple database systems via Testcontainers

## Prerequisites

- Java 21 or later
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Usage

### Running All Examples

```bash
./gradlew :examples:db-tester-example-junit:test :examples:db-tester-example-spock:test :examples:db-tester-example-junit-spring-boot-starter:test :examples:db-tester-example-spock-spring-boot-starter:test
```

Or from the examples directory:

```bash
./gradlew test
```

### Running with Verbose Output

```bash
./gradlew :examples:db-tester-example-junit:test --info
./gradlew :examples:db-tester-example-junit:test --console=verbose
```

## Example Modules

| Module | Description |
|--------|-------------|
| [db-tester-example-junit](db-tester-example-junit/) | JUnit examples with feature tests and database integration tests |
| [db-tester-example-spock](db-tester-example-spock/) | Spock Framework examples |
| [db-tester-example-junit-spring-boot-starter](db-tester-example-junit-spring-boot-starter/) | Spring Boot examples with JUnit |
| [db-tester-example-spock-spring-boot-starter](db-tester-example-spock-spring-boot-starter/) | Spring Boot examples with Spock |

## Configuration

### CSV File Format

CSV files use standard format with optional scenario filtering:

```csv
[Scenario],ID,NAME,EMAIL
testCreateUser,1,Alice,alice@example.com
testUpdateUser,1,Alice Updated,alice.updated@example.com
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

Test data files are placed in the resources directory matching the test class structure:

```
src/test/resources/
  example/
    feature/
      MinimalExampleTest/
        TABLE1.csv              # Preparation data
        expected/
          TABLE1.csv            # Expected data
      ScenarioFilteringTest/
        TABLE1.csv              # Shared preparation data with scenarios
        expected/
          TABLE1.csv            # Shared expectation data with scenarios
```

## Related Modules

- [db-tester-junit](../db-tester-junit/) - JUnit extension
- [db-tester-spock](../db-tester-spock/) - Spock Framework extension
- [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) - Spring Boot auto-configuration for JUnit
- [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) - Spring Boot auto-configuration for Spock

## Documentation

For detailed framework documentation, refer to the [main README](../README.md).
