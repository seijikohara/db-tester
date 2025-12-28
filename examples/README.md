# DB Tester - Examples

This directory contains example tests demonstrating the features of the DB Tester framework.

## Overview

- **Convention-Based Testing** - CSV file resolution based on test class and method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Data Formats** - CSV and TSV file format support
- **Table Merge Strategies** - FIRST, LAST, UNION, UNION_ALL merge behaviors
- **Comparison Strategies** - STRICT, NUMERIC, CASE_INSENSITIVE, IGNORE, NOT_NULL, REGEX comparisons
- **Property-Based Configuration** - Spring Boot property binding for DB Tester settings

## Prerequisites

- Java 21 or later
- Groovy 5 or later (for Spock examples)
- Kotlin 2 or later (for Kotest examples)
- H2 Database (included as test dependency)
- Docker (optional, for Testcontainers-based integration tests)

## Example Modules

| Module | Description |
|--------|-------------|
| [db-tester-example-junit](db-tester-example-junit/) | JUnit examples with feature tests and database integration tests |
| [db-tester-example-spock](db-tester-example-spock/) | Spock examples with feature tests and database integration tests |
| [db-tester-example-kotest](db-tester-example-kotest/) | Kotest examples with feature tests and database integration tests |
| [db-tester-example-junit-spring-boot-starter](db-tester-example-junit-spring-boot-starter/) | Spring Boot examples with JUnit |
| [db-tester-example-spock-spring-boot-starter](db-tester-example-spock-spring-boot-starter/) | Spring Boot examples with Spock |
| [db-tester-example-kotest-spring-boot-starter](db-tester-example-kotest-spring-boot-starter/) | Spring Boot examples with Kotest |

## Usage

### Running All Examples

```bash
./gradlew :examples:db-tester-example-junit:test
./gradlew :examples:db-tester-example-spock:test
./gradlew :examples:db-tester-example-kotest:test
./gradlew :examples:db-tester-example-junit-spring-boot-starter:test
./gradlew :examples:db-tester-example-spock-spring-boot-starter:test
./gradlew :examples:db-tester-example-kotest-spring-boot-starter:test
```

### Running with Verbose Output

```bash
./gradlew :examples:db-tester-example-junit:test --info
```

## Related Modules

| Module | Description |
|--------|-------------|
| [db-tester-junit](../db-tester-junit/) | JUnit extension |
| [db-tester-spock](../db-tester-spock/) | Spock extension |
| [db-tester-kotest](../db-tester-kotest/) | Kotest extension |
| [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) | Spring Boot auto-configuration for JUnit |
| [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) | Spring Boot auto-configuration for Spock |
| [db-tester-kotest-spring-boot-starter](../db-tester-kotest-spring-boot-starter/) | Spring Boot auto-configuration for Kotest |

## Documentation

For detailed framework documentation, refer to the [main README](../README.md).
