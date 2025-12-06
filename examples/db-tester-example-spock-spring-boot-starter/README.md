# DB Tester - Spock Spring Boot Starter Examples

This module contains example tests demonstrating the DB Tester framework with Spring Boot and Spock Framework.

## Overview

The example project demonstrates:

- **Automatic DataSource Registration** - Spring-managed `DataSource` beans are automatically registered via Spring auto-configuration
- **Convention-Based Loading** - CSV dataset resolution based on specification class and feature method names
- **Annotation-Driven Testing** - `@Preparation` and `@Expectation` annotations for declarative test data management
- **Spring Data JPA Integration** - Testing Spring Data repositories with DB Tester
- **Spock Specification Syntax** - Expressive feature method definitions

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

### Specification Classes

| Specification | Description |
|---------------|-------------|
| `UserRepositorySpec` | Basic Spring Data JPA integration with automatic DataSource registration |

### Basic Example

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

## Project Structure

```
src/
├── main/java/
│   └── io/github/seijikohara/dbtester/example/springboot/
│       ├── ExampleApplication.java
│       ├── User.java
│       └── UserRepository.java
└── test/
    ├── groovy/example/
    │   └── UserRepositorySpec.groovy
    └── resources/
        ├── application.properties
        ├── schema.sql
        └── example/
            └── UserRepositorySpec/
                ├── USERS.csv
                └── expected/
                    └── USERS.csv
```

## Related Modules

- [db-tester-spock-spring-boot-starter](../../db-tester-spock-spring-boot-starter/) - Spring Boot auto-configuration for Spock

## Documentation

For detailed framework documentation, refer to the [main README](../../README.md).
