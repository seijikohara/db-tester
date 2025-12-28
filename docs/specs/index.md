---
layout: home

hero:
  name: "DB Tester"
  text: "Database Testing Framework"
  tagline: Annotation-driven data preparation and state verification for JUnit, Spock, and Kotest
  image:
    src: /favicon.svg
    alt: DB Tester
  actions:
    - theme: brand
      text: Get Started
      link: /01-overview
    - theme: alt
      text: View on GitHub
      link: https://github.com/seijikohara/db-tester
    - theme: alt
      text: Maven Central
      link: https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit

features:
  - icon: 📝
    title: Declarative Testing
    details: Use @Preparation and @Expectation annotations to define test data setup and verification.
    link: /03-public-api
    linkText: View API Reference
  - icon: 📁
    title: Convention over Configuration
    details: Automatic dataset discovery based on test class and method names. Just follow the conventions.
    link: /04-configuration
    linkText: Learn Conventions
  - icon: 🔧
    title: Multiple Frameworks
    details: Full support for JUnit Jupiter, Spock, and Kotest with Spring Boot integration.
    link: /07-test-frameworks
    linkText: Framework Integration
  - icon: 📊
    title: Flexible Data Formats
    details: CSV and TSV support with scenario filtering for sharing datasets across multiple tests.
    link: /05-data-formats
    linkText: Data Format Guide
  - icon: 🗄️
    title: Database Operations
    details: Support for CLEAN_INSERT, INSERT, UPDATE, DELETE, TRUNCATE and more with customizable table ordering.
    link: /06-database-operations
    linkText: Operation Reference
  - icon: 🔌
    title: Extensible Architecture
    details: Service Provider Interface (SPI) for custom data loaders, comparators, and operation handlers.
    link: /08-spi
    linkText: Extension Points
---

## Quick Start

### Installation

::: code-group

```kotlin [Gradle (Kotlin DSL)]
dependencies {
    // Using BOM (recommended)
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))

    // JUnit
    testImplementation("io.github.seijikohara:db-tester-junit")

    // Or Spock
    testImplementation("io.github.seijikohara:db-tester-spock")

    // Or Kotest
    testImplementation("io.github.seijikohara:db-tester-kotest")
}
```

```groovy [Gradle (Groovy DSL)]
dependencies {
    // Using BOM (recommended)
    testImplementation platform('io.github.seijikohara:db-tester-bom:VERSION')

    // JUnit
    testImplementation 'io.github.seijikohara:db-tester-junit'

    // Or Spock
    testImplementation 'io.github.seijikohara:db-tester-spock'

    // Or Kotest
    testImplementation 'io.github.seijikohara:db-tester-kotest'
}
```

```xml [Maven]
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
    <!-- JUnit -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-junit</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Or Spock -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-spock</artifactId>
        <scope>test</scope>
    </dependency>

    <!-- Or Kotest -->
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-kotest</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

:::

### Basic Usage

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @Preparation  // Loads test data from CSV
    @Expectation  // Verifies database state
    @Test
    void shouldCreateUser() {
        // Your test logic here
        userRepository.create(new User("john", "john@example.com"));
    }
}
```

### Directory Structure

```
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── shouldCreateUser/
    │   └── users.csv           # Preparation data
    └── shouldCreateUser/
        └── expected/
            └── users.csv       # Expected state
```

### Validation Output

When expectation verification fails, DB Tester provides detailed YAML-formatted error messages:

```yaml
Assertion failed: 2 differences in USERS
summary:
  status: FAILED
  total_differences: 2
tables:
  USERS:
    differences:
      - path: row_count
        expected: 3
        actual: 2
      - path: "row[0].EMAIL"
        expected: john@example.com
        actual: john@test.com
        column:
          type: VARCHAR(255)
          nullable: false
```

::: tip
The output is valid YAML and can be parsed by standard YAML libraries for CI/CD integration.
:::

See [Error Handling](/09-error-handling) for more details.
