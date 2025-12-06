# DB Tester - JUnit Spring Boot Starter

This module provides Spring Boot auto-configuration for the DB Tester framework with JUnit integration.

## Overview

The JUnit Spring Boot Starter includes:

- **Automatic DataSource Registration** - Spring-managed `DataSource` beans are automatically registered with DB Tester
- **Zero Configuration** - Works out of the box with Spring Boot's default DataSource
- **Multiple DataSources Support** - Handles multiple DataSource beans with `@Primary` annotation support
- **JUnit Integration** - Extends `DatabaseTestExtension` for full JUnit compatibility

## Requirements

- Java 21 or later
- Spring Boot 4 or later
- JUnit 6 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-junit-spring-boot-starter</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

Replace `VERSION` with the latest version from [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit-spring-boot-starter).

## Usage

### Basic Example

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

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

### Configuration

Configure in `application.properties` or `application.yml`:

```yaml
db-tester:
  enabled: true                      # Enable auto-configuration (default: true)
  auto-register-data-sources: true   # Auto-register DataSources (default: true)
```

## Key Classes

| Class | Description |
|-------|-------------|
| `SpringBootDatabaseTestExtension` | JUnit extension with Spring Boot integration |
| `DbTesterAutoConfiguration` | Spring Boot auto-configuration class |
| `DbTesterProperties` | Configuration properties for customization |

## Related Modules

- [db-tester-junit](../db-tester-junit/) - JUnit integration (base module)
- [db-tester-core](../db-tester-core/) - Core API and implementation

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
