# DB Tester - Spock Spring Boot Starter

This module provides Spring Boot auto-configuration for the DB Tester framework with Spock Framework integration.

## Overview

The Spock Spring Boot Starter includes:

- **Automatic DataSource Registration** - Spring-managed `DataSource` beans are automatically registered with DB Tester
- **Zero Configuration** - Works out of the box with Spring Boot's default DataSource
- **Multiple DataSources Support** - Handles multiple DataSource beans with `@Primary` annotation support
- **Spock Integration** - Works with Spock Framework's specification syntax

## Requirements

- Java 21 or later
- Groovy 5 or later
- Spock Framework 2 or later
- Spring Boot 4 or later

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-spock-spring-boot-starter:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-spock-spring-boot-starter</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

Replace `VERSION` with the latest version from [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-spock-spring-boot-starter).

## Usage

### Basic Example

```groovy
@SpringBootTest
class UserRepositorySpec extends Specification {

    @Autowired
    UserRepository userRepository

    @Preparation
    @Expectation
    def "should save user"() {
        when:
        userRepository.save(new User("Alice", "alice@example.com"))

        then:
        noExceptionThrown()
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
| `SpringBootDbTesterAutoConfiguration` | Spring Boot auto-configuration class |
| `DbTesterProperties` | Configuration properties for customization |

## Related Modules

- [db-tester-spock](../db-tester-spock/) - Spock Framework integration (base module)
- [db-tester-core](../db-tester-core/) - Core API and implementation

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
