# DB Tester - Examples

This directory contains example tests demonstrating the features of the DB Tester framework.

## Overview

- **Convention-Based Testing** - CSV file resolution based on test class and method names
- **Scenario Filtering** - CSV row filtering using `[Scenario]` column marker
- **Data Formats** - CSV, TSV, JSON, and YAML file format support
- **Table Merge Strategies** - FIRST, LAST, UNION, UNION_ALL merge behaviors
- **Comparison Strategies** - STRICT, IGNORE, NUMERIC, CASE_INSENSITIVE, TIMESTAMP_FLEXIBLE, DATE_FLEXIBLE, JSON_EQUIVALENT, NOT_NULL, REGEX comparisons
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

## Feature Coverage (JUnit Examples)

| Feature | Test Class |
|---------|-----------|
| Convention-based data loading | `MinimalExampleTest` |
| Nested class conventions | `NestedConventionTest` |
| Scenario filtering | `ScenarioFilteringTest` |
| CSV format | `DataFormatTest` (CsvFormatTest) |
| TSV format | `DataFormatTest` (TsvFormatTest) |
| JSON format | `DataFormatTest` (JsonFormatTest) |
| YAML format | `DataFormatTest` (YamlFormatTest) |
| All database operations | `OperationVariationsTest` |
| Batch size configuration | `OperationVariationsTest` (shouldUseBatchInsertOperation) |
| Column comparison strategies | `ColumnStrategyAnnotationTest` |
| Programmatic assertions | `ComparisonStrategyTest` |
| Partial column validation | `PartialColumnValidationTest` |
| Custom expectation paths | `CustomExpectationPathsTest` |
| Custom query validation | `CustomQueryValidationTest` |
| Table merge strategies | `TableMergeStrategyTest` |
| Table ordering strategies | `TableOrderingStrategiesTest` |
| Multiple DataSources | `MultipleDataSourceTest` |
| Annotation configuration | `AnnotationConfigurationTest` |
| Configuration customization | `ConfigurationCustomizationTest` |
| Annotation inheritance | `InheritedAnnotationTest` |
| Null and empty values | `NullAndEmptyValuesTest` |
| Comprehensive data types | `ComprehensiveDataTypesTest` |
| Error handling | `ErrorHandlingTest` |
| Template expressions (UUID) | `TemplateExpressionTest` (UuidExpressionTest) |
| Template expressions (Sequence) | `TemplateExpressionTest` (SequenceExpressionTest) |
| Template expressions (Timestamp) | `TemplateExpressionTest` (TimestampExpressionTest) |
| Template expressions (Datafaker) | `TemplateExpressionTest` (FakerExpressionTest) |
| Dataset export (CSV, JSON, YAML) | `DataSetExportTest` |
| Export configuration | `DataSetExportTest` (CustomConfigTests) |
| Programmatic assertion API | `ProgrammaticAssertionApiTest` |

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
