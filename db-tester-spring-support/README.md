# DB Tester - Spring Support Module

This module provides common Spring utilities for the DB Tester framework. It contains shared logic used by the Spring Boot starter modules to eliminate code duplication.

## Overview

- **DataSource Registration Support** - Common logic for registering Spring-managed DataSource beans
- **Primary Bean Resolution** - Utility for determining if a bean is marked with `@Primary`

## Architecture

```
db-tester-api (public API)
        ↑
db-tester-spring-support (Spring utilities)
        ↑
db-tester-junit-spring-boot-starter
db-tester-spock-spring-boot-starter
db-tester-kotest-spring-boot-starter
```

- **Depends on**: `db-tester-api`, Spring Context
- **Is used by**: Spring Boot starter modules

## Requirements

- Java 21 or later
- Spring Framework 7 or later

## Installation

This module is a transitive dependency of the Spring Boot starters. Direct dependency is not required for typical usage.

Use the Spring Boot starter modules instead:

- [db-tester-junit-spring-boot-starter](../db-tester-junit-spring-boot-starter/) for Spring Boot with JUnit
- [db-tester-spock-spring-boot-starter](../db-tester-spock-spring-boot-starter/) for Spring Boot with Spock
- [db-tester-kotest-spring-boot-starter](../db-tester-kotest-spring-boot-starter/) for Spring Boot with Kotest

### Gradle

```kotlin
dependencies {
    implementation("io.github.seijikohara:db-tester-spring-support:VERSION")
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-spring-support</artifactId>
    <version>VERSION</version>
</dependency>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-spring-support).

## Key Classes

| Class | Description |
|-------|-------------|
| `DataSourceRegistrarSupport` | Common logic for DataSource registration |
| `PrimaryBeanResolver` | Utility for resolving primary bean status |

## Package Structure

| Package | Description |
|---------|-------------|
| `spring.support` | Spring support utilities |

## Usage

The support classes are typically used internally by the Spring Boot starters:

```java
// In a Spring Boot starter's DataSourceRegistrar
public void registerAll(DataSourceRegistry registry) {
    var dataSources = context.getBeansOfType(DataSource.class);
    
    DataSourceRegistrarSupport.registerDataSources(
        registry,
        dataSources,
        name -> PrimaryBeanResolver.isPrimaryBean(context, name, logger),
        logger
    );
}
```

## DataSource Resolution Priority

When multiple DataSources are present, the default is resolved in this order:

1. **Single DataSource** - Automatically becomes the default
2. **Primary DataSource** - Bean marked with `@Primary`
3. **Named "dataSource"** - Bean with the standard name

## Related Modules

| Module | Description |
|--------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-junit-spring-boot-starter`](../db-tester-junit-spring-boot-starter/) | Spring Boot with JUnit |
| [`db-tester-spock-spring-boot-starter`](../db-tester-spock-spring-boot-starter/) | Spring Boot with Spock |
| [`db-tester-kotest-spring-boot-starter`](../db-tester-kotest-spring-boot-starter/) | Spring Boot with Kotest |

## Documentation

For usage examples and configuration details, refer to the [main README](../README.md).
