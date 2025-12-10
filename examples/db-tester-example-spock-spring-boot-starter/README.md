# DB Tester - Spock Spring Boot Starter Examples

This module contains example tests demonstrating the DB Tester framework with Spring Boot and Spock Framework.

## Overview

- **Auto-Registration** - Spring-managed `DataSource` beans are registered via auto-configuration
- **Convention-Based Loading** - CSV dataset resolution based on specification class and feature method names
- **Property-Based Configuration** - Configure DB Tester via `application.properties`
- **Spring Data JPA Integration** - Testing Spring Data repositories

## Prerequisites

- Java 21 or later
- Groovy 5 or later
- Spring Boot 4 or later
- H2 Database (included as test dependency)

## Usage

### Running Tests

```bash
./gradlew :examples:db-tester-example-spock-spring-boot-starter:test
```

## Specification Classes

| Specification | Description |
|---------------|-------------|
| `UserRepositorySpec` | Spring Data JPA integration with DataSource registration |
| `MultipleDataSourcesSpec` | Multiple DataSource support with `@Primary` detection |
| `PropertiesConfigurationSpec` | Property-based configuration demonstration |

## Basic Example

```groovy
@SpringBootTest(classes = ExampleApplication)
class UserRepositorySpec extends Specification {

    @Autowired
    DataSourceRegistry dbTesterRegistry

    DataSourceRegistry getDbTesterRegistry() {
        dbTesterRegistry
    }

    @Preparation
    @Expectation
    def "should save new user"() {
        when:
        userRepository.save(new User(3L, "Charlie", "charlie@example.com"))

        then:
        noExceptionThrown()
    }
}
```

## Related Modules

- [db-tester-spock-spring-boot-starter](../../db-tester-spock-spring-boot-starter/) - Spring Boot auto-configuration for Spock

## Documentation

For detailed framework documentation, refer to the [main README](../../README.md).
