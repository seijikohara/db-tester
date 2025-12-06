# DB Tester

[![Test](https://github.com/seijikohara/db-tester/actions/workflows/test.yml/badge.svg)](https://github.com/seijikohara/db-tester/actions/workflows/test.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-bom.svg)](https://search.maven.org/search?q=g:io.github.seijikohara%20AND%20a:db-tester-*)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

DB Tester is a database testing framework that automates test data setup and verification. Add `@Preparation` and `@Expectation` annotations to test methods, and the framework automatically loads test data from CSV files before tests and verifies database state after tests.

**Before (without DB Tester):**

```java
@Test
void shouldCreateUser() {
    // Manual setup: delete existing data, insert test data
    jdbcTemplate.execute("DELETE FROM USERS");
    jdbcTemplate.execute("INSERT INTO USERS (ID, NAME) VALUES (1, 'existing')");

    userRepository.create(new User("john", "john@example.com"));

    // Manual verification: query and assert
    List<User> users = jdbcTemplate.query("SELECT * FROM USERS", rowMapper);
    assertThat(users).hasSize(2);
    assertThat(users.get(1).getName()).isEqualTo("john");
}
```

**After (with DB Tester):**

```java
@Test
@Preparation  // Loads USERS.csv automatically
@Expectation  // Verifies expected/USERS.csv automatically
void shouldCreateUser() {
    userRepository.create(new User("john", "john@example.com"));
}
```

## Features

- **Annotation-Driven Testing** - Declaratively manage test data using `@Preparation` and `@Expectation` annotations
- **Convention over Configuration** - Automatic dataset discovery based on test class name and scenario filtering
- **Multiple Format Support** - CSV dataset format with scenario marker column for filtering
- **Framework Integration** - Native support for [JUnit](https://junit.org/), [Spock](https://spockframework.org/), and [Spring Boot](https://spring.io/projects/spring-boot)
- **DbUnit Powered** - Built on the reliable [DbUnit](https://www.dbunit.org/) library for database operations
- **Type-Safe** - Full Java generics support with [JSpecify](https://jspecify.dev/) null-safety annotations

## Requirements

- [Java 21](https://openjdk.org/projects/jdk/21/) or later
- [JUnit 6](https://junit.org/) (for JUnit integration)
- [Spock 2](https://spockframework.org/) with [Groovy 5](https://groovy-lang.org/) (for Spock integration)
- [Spring Boot 4](https://spring.io/projects/spring-boot) (for Spring Boot integration)

## Installation

### Which Module to Choose

| Use Case | Module |
|----------|--------|
| JUnit with manual DataSource setup | [`db-tester-junit`](db-tester-junit/) |
| JUnit with Spring Boot | [`db-tester-junit-spring-boot-starter`](db-tester-junit-spring-boot-starter/) |
| Spock Framework with manual DataSource setup | [`db-tester-spock`](db-tester-spock/) |
| Spock Framework with Spring Boot | [`db-tester-spock-spring-boot-starter`](db-tester-spock-spring-boot-starter/) |

### Maven

Add the BOM for dependency management:

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

Then add the appropriate module:

```xml
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
```

### Gradle

```kotlin
// Add BOM
testImplementation(platform("io.github.seijikohara:db-tester-bom:${dbTesterVersion}"))

// For JUnit
testImplementation("io.github.seijikohara:db-tester-junit")

// For Spock
testImplementation("io.github.seijikohara:db-tester-spock")

// For Spring Boot with JUnit
testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")

// For Spring Boot with Spock
testImplementation("io.github.seijikohara:db-tester-spock-spring-boot-starter")
```

## Quick Start

This example uses JUnit with H2 in-memory database.

### Step 1: Add Dependencies

```kotlin
// build.gradle.kts
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-junit")
    testImplementation("com.h2database:h2:2.2.224")
}
```

### Step 2: Create Test Class

```java
package com.example;

import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.Connection;
import java.sql.Statement;

@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    private static JdbcDataSource dataSource;

    @BeforeAll
    static void setUp(ExtensionContext context) throws Exception {
        // Create H2 in-memory database
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Create table
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE USERS (
                    ID BIGINT PRIMARY KEY,
                    NAME VARCHAR(100),
                    EMAIL VARCHAR(100)
                )
                """);
        }

        // Register DataSource with DB Tester
        DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Test
    @Preparation  // Loads: com/example/UserRepositoryTest/USERS.csv
    @Expectation  // Verifies: com/example/UserRepositoryTest/expected/USERS.csv
    void shouldCreateUser() throws Exception {
        // Insert a new user (your business logic here)
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO USERS (ID, NAME, EMAIL) VALUES (2, 'john', 'john@example.com')");
        }
    }
}
```

### Step 3: Create CSV Files

Directory structure follows the test class package and name:

```
src/test/resources/
└── com/
    └── example/
        └── UserRepositoryTest/
            ├── USERS.csv              # Preparation data (loaded before test)
            └── expected/
                └── USERS.csv          # Expected data (verified after test)
```

**`USERS.csv`** - Initial data loaded before the test:

```csv
ID,NAME,EMAIL
1,existing_user,existing@example.com
```

**`expected/USERS.csv`** - Expected database state after the test:

```csv
ID,NAME,EMAIL
1,existing_user,existing@example.com
2,john,john@example.com
```

### Step 4: Run Test

```bash
./gradlew test
```

The framework will:
1. Load `USERS.csv` into the database before the test
2. Execute the test method
3. Compare the actual database state with `expected/USERS.csv`
4. Fail the test if the states do not match

## Usage

### JUnit Example

```java
import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    private static DataSource dataSource;

    @BeforeAll
    static void setUp(ExtensionContext context) {
        // Create your DataSource (H2, PostgreSQL, MySQL, etc.)
        dataSource = createDataSource();

        // Register with DB Tester
        DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Test
    @Preparation  // Loads data from: com/example/UserRepositoryTest/USERS.csv
    @Expectation  // Verifies: com/example/UserRepositoryTest/expected/USERS.csv
    void shouldCreateUser() {
        userRepository.create(new User("john", "john@example.com"));
    }
}
```

### Spring Boot Example

With Spring Boot starter, DataSource is automatically registered:

```java
import io.github.seijikohara.dbtester.api.annotation.Expectation;
import io.github.seijikohara.dbtester.api.annotation.Preparation;
import io.github.seijikohara.dbtester.junit.jupiter.extension.SpringBootDatabaseTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Preparation  // No manual DataSource setup needed
    @Expectation
    void shouldCreateUser() {
        userRepository.save(new User("john", "john@example.com"));
    }
}
```

### Spock Example

```groovy
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import spock.lang.Shared
import spock.lang.Specification

class UserRepositorySpec extends Specification {

    @Shared
    static DataSource dataSource

    @Shared
    DataSourceRegistry dbTesterRegistry  // Field name must be 'dbTesterRegistry'

    def setupSpec() {
        // Create your DataSource
        dataSource = createDataSource()

        // Register with DB Tester
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Preparation
    @Expectation
    def "should create user"() {
        when:
        userRepository.create(new User("john", "john@example.com"))

        then:
        // Database state is automatically verified by @Expectation
        noExceptionThrown()
    }
}
```

### Dataset Files

Dataset files are placed in a directory structure that mirrors the test class package and name:

```
src/test/resources/
└── com/
    └── example/
        └── UserRepositoryTest/        # Package path + class name
            ├── USERS.csv              # Preparation data
            ├── ORDERS.csv             # Multiple tables supported
            └── expected/
                ├── USERS.csv          # Expected data after test
                └── ORDERS.csv
```

### Scenario Filtering

Use the `[Scenario]` column to share a single CSV file across multiple test methods. This eliminates the need to create separate CSV files for each test.

**Test class with multiple test methods:**

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setUp(ExtensionContext context) {
        // DataSource setup...
    }

    @Test
    @Preparation  // Loads only rows where [Scenario] = "shouldCreateUser"
    @Expectation
    void shouldCreateUser() {
        userRepository.create(new User("john", "john@example.com"));
    }

    @Test
    @Preparation  // Loads only rows where [Scenario] = "shouldUpdateUser"
    @Expectation
    void shouldUpdateUser() {
        userRepository.update(1, "updated_name", "updated@example.com");
    }

    @Test
    @Preparation  // Loads only rows where [Scenario] = "shouldDeleteUser"
    void shouldDeleteUser() {
        userRepository.delete(1);
    }
}
```

**USERS.csv** - Shared test data for all scenarios:

```csv
[Scenario],ID,NAME,EMAIL
shouldCreateUser,1,existing_user,existing@example.com
shouldUpdateUser,1,update_target,target@example.com
shouldDeleteUser,1,delete_target,delete@example.com
```

When `shouldCreateUser()` runs, only the first row is loaded. When `shouldUpdateUser()` runs, only the second row is loaded.

**expected/USERS.csv** - Expected states per scenario:

```csv
[Scenario],ID,NAME,EMAIL
shouldCreateUser,1,existing_user,existing@example.com
shouldCreateUser,2,john,john@example.com
shouldUpdateUser,1,updated_name,updated@example.com
```

When verifying `shouldCreateUser()`, only rows with `[Scenario] = "shouldCreateUser"` are compared against the actual database state.

## Configuration

### Annotations

#### [`@Preparation`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/annotation/Preparation.java)

Configures test data setup before each test method.

| Attribute   | Type        | Default        | Description                     |
| ----------- | ----------- | -------------- | ------------------------------- |
| `dataSets`  | [`DataSet[]`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/annotation/DataSet.java) | `{}`           | Explicit dataset configurations |
| `operation` | [`Operation`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/operation/Operation.java) | `CLEAN_INSERT` | Database operation to perform   |

#### [`@Expectation`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/annotation/Expectation.java)

Configures database state verification after each test method.

| Attribute  | Type        | Default | Description                     |
| ---------- | ----------- | ------- | ------------------------------- |
| `dataSets` | [`DataSet[]`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/annotation/DataSet.java) | `{}`    | Explicit dataset configurations |

#### [`@DataSet`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/annotation/DataSet.java)

Specifies dataset location and filtering options.

| Attribute          | Type       | Default | Description                                            |
| ------------------ | ---------- | ------- | ------------------------------------------------------ |
| `resourceLocation` | `String`   | `""`    | Dataset directory path (classpath or file system)      |
| `dataSourceName`   | `String`   | `""`    | Named data source to use (empty for default)           |
| `scenarioNames`    | `String[]` | `{}`    | Scenario names to filter (empty uses test method name) |

### Operations

| [`Operation`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/operation/Operation.java) | Description |
|-----------|-------------|
| `CLEAN_INSERT` | Delete all rows then insert (default) |
| `INSERT` | Insert new rows |
| `UPDATE` | Update existing rows |
| `REFRESH` | Upsert (insert or update) |
| `DELETE` | Delete specified rows by primary key |
| `DELETE_ALL` | Delete all rows from tables |
| `TRUNCATE_TABLE` | Truncate tables |
| `TRUNCATE_INSERT` | Truncate then insert |
| `NONE` | No operation |

### Dataset Format

#### CSV Conventions

| Element | Description | Example |
|---------|-------------|---------|
| Column names | Must match database column names exactly | `ID,NAME,EMAIL` |
| `[Scenario]` column | Optional first column for test filtering | `[Scenario],ID,NAME` |
| NULL values | Empty cells represent SQL NULL | `1,,john@example.com` |
| Empty strings | Quoted empty string | `1,"",john@example.com` |
| Dates | ISO format | `2024-01-15` or `2024-01-15 10:30:00` |
| Booleans | TRUE/FALSE or 1/0 | `TRUE` or `1` |
| Commas in values | Quote the entire value | `"Value, with comma"` |

### Custom Configuration

#### Custom DataSetLoader

```java
Configuration config = Configuration.withLoader(new CustomDataSetLoader());
```

#### Custom Convention Settings

```java
ConventionSettings conventions = new ConventionSettings(
    null,           // baseDirectory (null for classpath)
    "/expected",    // expectationSuffix
    "[Scenario]"    // scenarioMarker
);
Configuration config = Configuration.withConventions(conventions);
```

See [`Configuration`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/config/Configuration.java) and [`ConventionSettings`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/config/ConventionSettings.java) for details.

#### Multiple Data Sources

```java
DataSourceRegistry registry = new DataSourceRegistry();
registry.registerDefault(primaryDataSource);
registry.register("secondary", secondaryDataSource);
```

Use in annotations:

```java
@Preparation(dataSets = @DataSet(dataSourceName = "secondary"))
```

See [`DataSourceRegistry`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/config/DataSourceRegistry.java) for details.

## Troubleshooting

### CSV file not found

**Error**: `DataSetLoadException: Could not find dataset directory`

**Cause**: The CSV file path does not match the test class location.

**Solution**: Ensure the directory structure matches the test class package:
- Test class: `com.example.UserRepositoryTest`
- CSV location: `src/test/resources/com/example/UserRepositoryTest/USERS.csv`

### Column name mismatch

**Error**: `NoSuchColumnException: Column 'NAME' not found`

**Cause**: CSV column names do not match database column names.

**Solution**: Ensure CSV header names exactly match database column names (case-sensitive for some databases).

### DataSource not registered

**Error**: `DataSourceNotFoundException: No default DataSource registered`

**Cause**: DataSource was not registered before test execution.

**Solution**:
- For JUnit: Call `DatabaseTestExtension.getRegistry(context).registerDefault(dataSource)` in `@BeforeAll`
- For Spring Boot: Use `SpringBootDatabaseTestExtension` which auto-registers Spring-managed DataSources
- For Spock: Define a `@Shared DataSourceRegistry dbTesterRegistry` field and initialize it in `setupSpec()`

### Unexpected row count

**Error**: `AssertionError: Expected 2 rows but found 3`

**Cause**: The actual database state differs from the expected CSV.

**Solution**:
- Check if previous tests left data (use `CLEAN_INSERT` operation)
- Verify the `[Scenario]` column filters correctly
- Ensure the expected CSV contains all rows that should exist after the test

## Key Classes

| Module | Class | Description |
|--------|-------|-------------|
| [`db-tester-core`](db-tester-core/) | [`@Preparation`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/annotation/Preparation.java), [`@Expectation`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/annotation/Expectation.java) | Test data annotations |
| [`db-tester-core`](db-tester-core/) | [`DataSourceRegistry`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/config/DataSourceRegistry.java) | DataSource registration |
| [`db-tester-core`](db-tester-core/) | [`Configuration`](db-tester-core/src/main/java/io/github/seijikohara/dbtester/api/config/Configuration.java) | Framework configuration |
| [`db-tester-junit`](db-tester-junit/) | [`DatabaseTestExtension`](db-tester-junit/src/main/java/io/github/seijikohara/dbtester/junit/jupiter/extension/DatabaseTestExtension.java) | JUnit extension |
| [`db-tester-junit-spring-boot-starter`](db-tester-junit-spring-boot-starter/) | [`SpringBootDatabaseTestExtension`](db-tester-junit-spring-boot-starter/src/main/java/io/github/seijikohara/dbtester/junit/spring/boot/autoconfigure/SpringBootDatabaseTestExtension.java) | Spring Boot integration for JUnit |
| [`db-tester-spock`](db-tester-spock/) | [`DatabaseTestExtension`](db-tester-spock/src/main/groovy/io/github/seijikohara/dbtester/spock/extension/DatabaseTestExtension.groovy) | Spock global extension |
| [`db-tester-spock-spring-boot-starter`](db-tester-spock-spring-boot-starter/) | [`DbTesterSpockAutoConfiguration`](db-tester-spock-spring-boot-starter/src/main/groovy/io/github/seijikohara/dbtester/spock/spring/boot/autoconfigure/DbTesterSpockAutoConfiguration.groovy) | Spring Boot integration for Spock |

## Related Modules

| Module                              | Description                                         |
| ----------------------------------- | --------------------------------------------------- |
| [`db-tester-core`](db-tester-core/)                    | Core library with public API and DbUnit integration |
| [`db-tester-junit`](db-tester-junit/)                   | JUnit extension                                     |
| [`db-tester-spock`](db-tester-spock/)                   | Spock framework extension                           |
| [`db-tester-junit-spring-boot-starter`](db-tester-junit-spring-boot-starter/) | Spring Boot auto-configuration for JUnit          |
| [`db-tester-spock-spring-boot-starter`](db-tester-spock-spring-boot-starter/) | Spring Boot auto-configuration for Spock          |
| [`db-tester-bom`](db-tester-bom/)                     | Bill of Materials for dependency management         |

## Building from Source

```bash
./gradlew build
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Documentation

- [Examples](examples/) - Example tests demonstrating framework features
- [DbUnit](https://www.dbunit.org/) - Database testing framework
- [JUnit](https://junit.org/) - Testing framework for Java
- [Spock Framework](https://spockframework.org/) - Testing framework for Groovy
- [Spring Boot](https://spring.io/projects/spring-boot) - Java application framework
