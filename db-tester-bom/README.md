# DB Tester - BOM Module

This module provides a Bill of Materials (BOM) for managing consistent versions of DB Tester dependencies.

## Overview

- **Centralized version management** - Single location for all DB Tester artifact versions
- **Version compatibility** - Ensures compatibility between modules
- **Simplified dependency declaration** - Eliminates version specification for individual artifacts

## Architecture

```
db-tester-bom (manages versions)
    ├── db-tester-api
    ├── db-tester-core
    ├── db-tester-spring-support
    ├── db-tester-junit
    ├── db-tester-spock
    ├── db-tester-kotest
    ├── db-tester-junit-spring-boot-starter
    ├── db-tester-spock-spring-boot-starter
    └── db-tester-kotest-spring-boot-starter
```

The BOM contains no code. It provides dependency management only.

## Requirements

- Maven or Gradle build system

## Installation

### Gradle

```kotlin
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-junit")
}
```

### Maven

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.seijikohara</groupId>
            <artifactId>db-tester-bom</artifactId>
            <version>VERSION</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-junit</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-bom).

## Managed Dependencies

| Artifact | Description |
|----------|-------------|
| [`db-tester-api`](../db-tester-api/) | Public API (annotations, configuration, SPI interfaces) |
| [`db-tester-core`](../db-tester-core/) | Internal implementation (SPI providers) |
| [`db-tester-spring-support`](../db-tester-spring-support/) | Common Spring utilities for DataSource registration |
| [`db-tester-junit`](../db-tester-junit/) | JUnit extension |
| [`db-tester-spock`](../db-tester-spock/) | Spock extension |
| [`db-tester-kotest`](../db-tester-kotest/) | Kotest extension |
| [`db-tester-junit-spring-boot-starter`](../db-tester-junit-spring-boot-starter/) | Spring Boot auto-configuration for JUnit |
| [`db-tester-spock-spring-boot-starter`](../db-tester-spock-spring-boot-starter/) | Spring Boot auto-configuration for Spock |
| [`db-tester-kotest-spring-boot-starter`](../db-tester-kotest-spring-boot-starter/) | Spring Boot auto-configuration for Kotest |

## Documentation

For usage examples and configuration details, see the [main README](../README.md).
