# DB Tester - JUnit Spring Boot Starter Examples

This module contains example tests demonstrating the DB Tester framework with Spring Boot and JUnit.

## Overview

The example project demonstrates:

- **Automatic DataSource Registration** - Spring-managed `DataSource` beans are automatically registered via `SpringBootDatabaseTestExtension`
- **Convention-Based Loading** - CSV dataset resolution based on test class and method names
- **Annotation-Driven Testing** - `@Preparation` and `@Expectation` annotations for declarative test data management
- **Spring Data JPA Integration** - Testing Spring Data repositories with DB Tester
- **Multiple DataSource Support** - Handling multiple DataSource beans

## Prerequisites

- Java 21 or later
- Spring Boot 4 or later
- H2 Database (included as test dependency)

## Usage

### Running Tests

```bash
./gradlew :examples:db-tester-example-junit-spring-boot-starter:test
```

### Test Classes

| Test Class | Description |
|------------|-------------|
| `UserRepositoryTest` | Basic Spring Data JPA integration with automatic DataSource registration |
| `MultipleDataSourcesTest` | Multiple DataSource support with `@Primary` detection |

### Basic Example

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {
    // No @BeforeAll setup needed - DataSource is auto-registered

    @Autowired
    private UserRepository userRepository;

    @Test
    @Preparation
    @Expectation
    void shouldSaveUser() {
        userRepository.save(new User("Alice", "alice@example.com"));
    }
}
```

### Multiple DataSources Example

```java
@SpringBootTest(classes = {ExampleApplication.class, MultiDataSourceConfig.class})
@ExtendWith(SpringBootDatabaseTestExtension.class)
class MultipleDataSourcesTest {
    // Both mainDb and archiveDb are automatically registered
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
    ├── java/example/
    │   ├── UserRepositoryTest.java
    │   └── MultipleDataSourcesTest.java
    └── resources/
        ├── application.properties
        ├── schema.sql
        └── example/
            ├── UserRepositoryTest/
            │   ├── USERS.csv
            │   └── expected/
            │       └── USERS.csv
            └── MultipleDataSourcesTest/
                └── USERS.csv
```

## Related Modules

- [db-tester-junit-spring-boot-starter](../../db-tester-junit-spring-boot-starter/) - Spring Boot auto-configuration for JUnit

## Documentation

For detailed framework documentation, refer to the [main README](../../README.md).
