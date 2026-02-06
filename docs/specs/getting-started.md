---
title: "Getting Started - DB Tester"
description: "Step-by-step guide to set up and run your first database test with DB Tester."
---

# Getting Started

This guide walks you through creating and running your first database test
with DB Tester. By the end, you will have a working test that prepares data
in an H2 in-memory database and verifies the expected state.

## Prerequisites

- Java 21 or later
- Gradle 8.0+ or Maven 3.9+

## Step 1: Add Dependencies

Add the following dependencies to your build configuration.

::: code-group

```kotlin [Gradle (Kotlin DSL)]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-junit")
    testImplementation("com.h2database:h2:2.3.232")
}
```

```groovy [Gradle (Groovy DSL)]
dependencies {
    testImplementation platform("io.github.seijikohara:db-tester-bom:VERSION")
    testImplementation "io.github.seijikohara:db-tester-junit"
    testImplementation "com.h2database:h2:2.3.232"
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
    <dependency>
        <groupId>io.github.seijikohara</groupId>
        <artifactId>db-tester-junit</artifactId>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.3.232</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

:::

Replace `VERSION` with the latest version from
[Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit).

::: tip H2 Version
The H2 version shown (`2.3.232`) is an example. Check
[Maven Central](https://central.sonatype.com/artifact/com.h2database/h2)
for the latest version.
:::

## Step 2: Create Your First Test

Create a test class with the following structure:

```java
package com.example;

import io.github.seijikohara.dbtester.api.annotation.DataSet;
import io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    private static JdbcDataSource dataSource;

    @BeforeAll
    static void setUp(ExtensionContext context) throws SQLException {
        // Create H2 in-memory DataSource
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        // Register DataSource with DB Tester
        DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);

        // Create schema
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE USERS (
                    ID INT PRIMARY KEY,
                    NAME VARCHAR(100),
                    EMAIL VARCHAR(255)
                )
                """);
        }
    }

    @Test
    @DataSet
    void shouldLoadTestData() throws SQLException {
        // The @DataSet annotation loads data from:
        // src/test/resources/com/example/UserRepositoryTest/USERS.csv

        // Your test logic here
        // For example, query the database and verify the data exists
        assertTrue(true, "Test data loaded successfully");
    }
}
```

Key points:

- `@ExtendWith(DatabaseTestExtension.class)` enables the DB Tester extension
- `@BeforeAll` with `ExtensionContext` parameter registers the DataSource
- `DB_CLOSE_DELAY=-1` keeps the H2 database open between tests
- `@DataSet` loads test data before the test method runs

## Step 3: Create Test Data Files

Create the test data directory and CSV file matching your test class location:

```
src/test/resources/
└── com/example/UserRepositoryTest/
    └── USERS.csv
```

The directory path follows the convention: `{package}/{TestClassName}/`

Create `USERS.csv` with the following content:

```csv
ID,NAME,EMAIL
1,Alice,alice@example.com
2,Bob,bob@example.com
```

::: tip CSV File Naming
The CSV filename must match the table name. H2 converts unquoted identifiers
to uppercase, so use `USERS.csv` to match the `USERS` table.
:::

::: info Scenario Column
The `[Scenario]` column is optional. When present, DB Tester filters rows
by the current test method name. When omitted, all rows are loaded.
See [Data Formats](data-formats) for details.
:::

## Step 4: Run the Test

Execute the test using your build tool:

::: code-group

```bash [Gradle]
./gradlew test --tests "com.example.UserRepositoryTest"
```

```bash [Maven]
mvn test -Dtest=com.example.UserRepositoryTest
```

:::

If successful, you will see output indicating one test passed with no failures.

## Step 5: Verify Database State (Optional)

Add `@ExpectedDataSet` to verify the database state after your test logic.

::: info Import Statement
Add the following import to use `@ExpectedDataSet`:
```java
import io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet;
```
:::

```java
@Test
@DataSet
@ExpectedDataSet
void shouldUpdateUser() throws SQLException {
    // Load initial data from USERS.csv

    // Perform update operation
    try (var conn = dataSource.getConnection();
         var stmt = conn.createStatement()) {
        stmt.execute(
            "UPDATE USERS SET EMAIL = 'alice.updated@example.com' WHERE ID = 1");
    }

    // @ExpectedDataSet verifies against:
    // src/test/resources/com/example/UserRepositoryTest/expected/USERS.csv
}
```

Create the expected data file:

```
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── USERS.csv
    └── expected/
        └── USERS.csv
```

The `expected/USERS.csv` file contains the expected state:

```csv
ID,NAME,EMAIL
1,Alice,alice.updated@example.com
2,Bob,bob@example.com
```

## Common Errors and Solutions

### No default data source registered

```
No default data source registered
```

**Cause**: The `@BeforeAll` method did not register a DataSource, or the method signature is incorrect.

**Solution**: Ensure your `@BeforeAll` method includes the `ExtensionContext`
parameter and calls `DatabaseTestExtension.getRegistry(context).registerDefault(dataSource)`.

### Dataset directory not found

```
Dataset directory not found on classpath: 'com/example/UserRepositoryTest'
Expected location: src/test/resources/com/example/UserRepositoryTest
Hint: Create the directory and add dataset files...
```

**Cause**: The test data directory does not exist or is in the wrong location.

**Solution**: Create the directory at `src/test/resources/{package}/{TestClassName}/`
where `{package}` uses forward slashes (for example, `com/example`).

### File is empty

```
File is empty: /path/to/USERS.csv
```

**Cause**: The CSV file exists but contains no data.

**Solution**: Ensure the CSV file contains at least a header row and one data row.

## Next Steps

- [Test Frameworks](test-frameworks) - Learn about Spock and Kotest integration
- [Configuration](configuration) - Customize framework behavior
- [Data Formats](data-formats) - Learn about CSV structure and scenario filtering
- [Database Operations](database-operations) - Understand available operations (INSERT, CLEAN_INSERT, etc.)
- [Error Handling](error-handling) - Complete error reference

For Spring Boot applications, see the Spring Boot Starter modules:

- `db-tester-junit-spring-boot-starter` - Auto-registers Spring-managed DataSource beans
- `db-tester-spock-spring-boot-starter` - Spock integration with Spring Boot
- `db-tester-kotest-spring-boot-starter` - Kotest integration with Spring Boot
