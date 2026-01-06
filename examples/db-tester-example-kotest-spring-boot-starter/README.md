# DB Tester - Kotest Spring Boot Starter Examples

This module contains example tests demonstrating the DB Tester framework with Spring Boot and Kotest.

## Overview

- **Auto-Registration** - Spring-managed `DataSource` beans are automatically registered with DB Tester
- **Convention-Based Loading** - CSV dataset resolution based on test class and method names
- **Property-Based Configuration** - Configure DB Tester via `application.properties`
- **Spring Data JPA Integration** - Testing Spring Data repositories

## Prerequisites

- Java 21 or later
- Kotlin 2 or later
- Spring Boot 4 or later
- H2 Database (included as test dependency)

## Usage

### Running Tests

```bash
./gradlew :examples:db-tester-example-kotest-spring-boot-starter:test
```

## Test Classes

| Test Class | Description |
|------------|-------------|
| `UserRepositorySpec` | Spring Data JPA integration with automatic DataSource registration |
| `MultipleDataSourcesSpec` | Multiple DataSource support with `@Primary` detection |
| `PropertiesConfigurationSpec` | Property-based configuration demonstration |

## Basic Example

```kotlin
@SpringBootTest
class UserRepositorySpec : AnnotationSpec() {

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        extensions(SpringBootDatabaseTestExtension())
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should save user`() {
        userRepository.save(User("Alice", "alice@example.com"))
    }
}
```

## Related Modules

- [db-tester-kotest-spring-boot-starter](../../db-tester-kotest-spring-boot-starter/) - Spring Boot auto-configuration for Kotest

## Documentation

For detailed framework documentation, refer to the [main README](../../README.md).
