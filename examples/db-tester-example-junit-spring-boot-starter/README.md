# DB Tester - JUnit Spring Boot Starter Examples

This module contains example tests demonstrating the DB Tester framework with Spring Boot and JUnit.

## Overview

- **Auto-registration** - Spring-managed `DataSource` beans are registered with DB Tester automatically
- **Convention-based loading** - CSV dataset resolution based on test class and method names
- **Property-based configuration** - Configure DB Tester via `application.properties`
- **Spring Data JPA integration** - Testing Spring Data repositories

## Prerequisites

- Java 21 or later
- Spring Boot 4 or later
- H2 Database (included as test dependency)

## Running Tests

```bash
./gradlew :examples:db-tester-example-junit-spring-boot-starter:test
```

## Test Classes

| Test Class | Description |
|------------|-------------|
| `UserRepositoryTest` | Spring Data JPA integration with automatic DataSource registration |
| `MultipleDataSourcesTest` | Multiple DataSource support with `@Primary` detection |
| `PropertiesConfigurationTest` | Property-based configuration demonstration |

## Basic Example

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DataSet
    @ExpectedDataSet
    void shouldSaveUser() {
        userRepository.save(new User("Alice", "alice@example.com"));
    }
}
```

## Related Modules

- [db-tester-junit-spring-boot-starter](../../db-tester-junit-spring-boot-starter/) - Spring Boot auto-configuration
  for JUnit

## Documentation

For framework documentation, see the [main README](../../README.md).
