# DB Tester - BOM

This module provides a Bill of Materials (BOM) for managing consistent versions of DB Tester dependencies across projects.

## Overview

The BOM module includes:

- **Centralized Version Management** - Single location for all DB Tester artifact versions
- **Version Compatibility** - Ensures compatibility between modules
- **Simplified Dependencies** - Eliminates the need to specify versions for individual artifacts

## Requirements

- Java 21 or later
- Maven or Gradle build system

## Installation

### Maven

Add the BOM in the `<dependencyManagement>` section:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.seijikohara</groupId>
            <artifactId>db-tester-bom</artifactId>
            <version>${db-tester.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

Then add the required modules without specifying versions:

```xml
<dependencies>
    <!-- For JUnit -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-junit</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- For Spock -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-spock</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- For Spring Boot with JUnit -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-junit-spring-boot-starter</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- For Spring Boot with Spock -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-spock-spring-boot-starter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Gradle

Add the BOM using the `platform` dependency:

```kotlin
dependencies {
    // Import BOM
    testImplementation(platform("io.github.seijikohara:db-tester-bom:${dbTesterVersion}"))

    // Add modules without specifying versions
    testImplementation("io.github.seijikohara:db-tester-junit")
    testImplementation("io.github.seijikohara:db-tester-spock")
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")
    testImplementation("io.github.seijikohara:db-tester-spock-spring-boot-starter")
}
```

For the latest version, see [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-bom).

## Usage

### JUnit Integration

```kotlin
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:${dbTesterVersion}"))
    testImplementation("io.github.seijikohara:db-tester-junit")
}
```

### Spock Framework Integration

```kotlin
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:${dbTesterVersion}"))
    testImplementation("io.github.seijikohara:db-tester-spock")
}
```

### Spring Boot Integration (JUnit)

```kotlin
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:${dbTesterVersion}"))
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")
}
```

### Spring Boot Integration (Spock)

```kotlin
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:${dbTesterVersion}"))
    testImplementation("io.github.seijikohara:db-tester-spock-spring-boot-starter")
}
```

### Multiple Testing Frameworks

To use both JUnit and Spock Framework in the same project:

```kotlin
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:${dbTesterVersion}"))
    testImplementation("io.github.seijikohara:db-tester-junit")
    testImplementation("io.github.seijikohara:db-tester-spock")
}
```

## Managed Dependencies

The following artifacts are managed by this BOM:

| Artifact | Description |
|----------|-------------|
| [`db-tester-core`](../db-tester-core/) | Public API and implementation with DbUnit integration |
| [`db-tester-junit`](../db-tester-junit/) | JUnit extension |
| [`db-tester-spock`](../db-tester-spock/) | Spock Framework extension |
| [`db-tester-junit-spring-boot-starter`](../db-tester-junit-spring-boot-starter/) | Spring Boot auto-configuration for JUnit |
| [`db-tester-spock-spring-boot-starter`](../db-tester-spock-spring-boot-starter/) | Spring Boot auto-configuration for Spock |

## Related Modules

| Module | Documentation |
|--------|---------------|
| `db-tester-core` | [README](../db-tester-core/README.md) |
| `db-tester-junit` | [README](../db-tester-junit/README.md) |
| `db-tester-spock` | [README](../db-tester-spock/README.md) |
| `db-tester-junit-spring-boot-starter` | [README](../db-tester-junit-spring-boot-starter/README.md) |
| `db-tester-spock-spring-boot-starter` | [README](../db-tester-spock-spring-boot-starter/README.md) |
| Examples | [README](../examples/README.md) |

## Documentation

For detailed usage documentation and examples, refer to the [main README](../README.md).
