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
  - icon:
      src: /icons/declarative.svg
    title: Declarative Testing
    details: Use @Preparation and @Expectation annotations to define test data setup and verification.
  - icon:
      src: /icons/convention.svg
    title: Convention over Configuration
    details: Automatic dataset discovery based on test class and method names. Follow the conventions.
  - icon:
      src: /icons/frameworks.svg
    title: Multiple Frameworks
    details: Full support for JUnit Jupiter, Spock, and Kotest with Spring Boot integration.
  - icon:
      src: /icons/data-formats.svg
    title: Flexible Data Formats
    details: CSV and TSV support with scenario filtering for sharing datasets across multiple tests.
  - icon:
      src: /icons/database.svg
    title: Database Operations
    details: Support for CLEAN_INSERT, INSERT, UPDATE, DELETE, TRUNCATE and more with customizable table ordering.
  - icon:
      src: /icons/extensible.svg
    title: Extensible Architecture
    details: Service Provider Interface (SPI) for custom data loaders, comparators, and operation handlers.
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

::: code-group

```java [JUnit]
package com.example;

@ExtendWith(DatabaseTestExtension.class)
@Preparation  // Loads test data from CSV
@Expectation  // Verifies database state
class UserRepositoryTest {

    @Test
    void shouldCreateUser() {
        userRepository.create(new User("john", "john@example.com"));
    }

    @Test
    void shouldUpdateUser() {
        userRepository.update(1L, new User("john", "john.doe@example.com"));
    }
}
```

```groovy [Spock]
package com.example

@DatabaseTest
@Preparation  // Loads test data from CSV
@Expectation  // Verifies database state
class UserRepositorySpec extends Specification {

    def "should create user"() {
        when:
        userRepository.create(new User("john", "john@example.com"))

        then:
        noExceptionThrown()
    }

    def "should update user"() {
        when:
        userRepository.update(1L, new User("john", "john.doe@example.com"))

        then:
        noExceptionThrown()
    }
}
```

```kotlin [Kotest]
package com.example

@Preparation  // Loads test data from CSV
@Expectation  // Verifies database state
class UserRepositorySpec : AnnotationSpec() {

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    @Test
    fun shouldCreateUser() {
        userRepository.create(User("john", "john@example.com"))
    }

    @Test
    fun shouldUpdateUser() {
        userRepository.update(1L, User("john", "john.doe@example.com"))
    }
}
```

:::

### Directory Structure

::: code-group

```text [JUnit]
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── users.csv              # Preparation data with [Scenario] column
    └── expected/
        └── users.csv          # Expected state with [Scenario] column
```

```text [Spock]
src/test/resources/
└── com/example/UserRepositorySpec/
    ├── users.csv              # Preparation data with [Scenario] column
    └── expected/
        └── users.csv          # Expected state with [Scenario] column
```

```text [Kotest]
src/test/resources/
└── com/example/UserRepositorySpec/
    ├── users.csv              # Preparation data with [Scenario] column
    └── expected/
        └── users.csv          # Expected state with [Scenario] column
```

:::

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
