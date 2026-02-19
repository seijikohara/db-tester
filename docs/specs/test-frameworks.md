---
title: "Test Frameworks - DB Tester"
description: "Integrate DB Tester with JUnit, Spock, and Kotest using framework-specific extensions."
---

# Test Framework Integration

DB Tester integrates with three test frameworks through annotation-driven extensions.

## Supported Frameworks

| Framework | Module | Extension | Language |
|-----------|--------|-----------|----------|
| [JUnit](junit) | `db-tester-junit` | `DatabaseTestExtension` | Java |
| [Spock](spock) | `db-tester-spock` | `DatabaseTestExtension` | Groovy |
| [Kotest](kotest) | `db-tester-kotest` | `DatabaseTestExtension` | Kotlin |

Each framework also provides a [Spring Boot starter](spring-boot) for automatic DataSource discovery.

## Framework Pages

| Page | Description |
|------|-------------|
| [JUnit](junit) | Extension registration, DataSource setup, nested tests, annotation precedence |
| [Spock](spock) | DatabaseTestSupport trait, feature method naming, data-driven tests |
| [Kotest](kotest) | AnnotationSpec integration, DatabaseTestSupport interface, extension registration |
| [Spring Boot](spring-boot) | Auto-configuration starters for JUnit, Spock, and Kotest |
| [Lifecycle](lifecycle) | Lifecycle hooks, executor classes, and error handling |

## Related Specifications

- [Overview](overview) - Framework purpose and key concepts
- [Annotations](annotations) - Annotation details
- [Configuration](configuration) - Configuration options
- [SPI](spi) - Service Provider Interface extension points
- [Error Handling](error-handling) - Lifecycle error handling
