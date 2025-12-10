# DB Tester

[![Test](https://github.com/seijikohara/db-tester/actions/workflows/test.yml/badge.svg)](https://github.com/seijikohara/db-tester/actions/workflows/test.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-bom.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-bom)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

A database testing framework for JUnit and Spock. Add `@Preparation` and `@Expectation` annotations to test methods. The framework loads CSV data before tests and verifies database state after tests.

```java
@Test
@Preparation  // Loads USERS.csv before test
@Expectation  // Verifies expected/USERS.csv after test
void shouldCreateUser() {
    userRepository.create(new User("john", "john@example.com"));
}
```

## Features

- **Annotation-Driven** - `@Preparation` and `@Expectation` annotations for declarative test data management
- **Convention-Based** - Dataset discovery based on test class package and name
- **Multiple Formats** - CSV and TSV with scenario filtering via `[Scenario]` column
- **Framework Support** - JUnit, Spock, and Spring Boot integration
- **Pure JDBC** - No external testing framework dependencies

## Requirements

- Java 21 or later
- JUnit 6 (for JUnit integration)
- Spock 2 with Groovy 5 (for Spock integration)
- Spring Boot 4 (for Spring Boot integration)

## Installation

Select a module based on the test framework:

| Use Case | Module |
|----------|--------|
| JUnit | [`db-tester-junit`](db-tester-junit/) |
| JUnit with Spring Boot | [`db-tester-junit-spring-boot-starter`](db-tester-junit-spring-boot-starter/) |
| Spock | [`db-tester-spock`](db-tester-spock/) |
| Spock with Spring Boot | [`db-tester-spock-spring-boot-starter`](db-tester-spock-spring-boot-starter/) |

<details>
<summary>All Modules</summary>

| Module | Description | Maven Central |
|--------|-------------|---------------|
| [`db-tester-bom`](db-tester-bom/) | Bill of Materials for version management | [![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-bom.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-bom) |
| [`db-tester-api`](db-tester-api/) | Public API (annotations, configuration, SPI) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-api.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-api) |
| [`db-tester-core`](db-tester-core/) | Internal implementation (runtime dependency) | [![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-core.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-core) |
| [`db-tester-junit`](db-tester-junit/) | JUnit extension | [![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-junit.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-junit) |
| [`db-tester-junit-spring-boot-starter`](db-tester-junit-spring-boot-starter/) | Spring Boot auto-configuration for JUnit | [![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-junit-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-junit-spring-boot-starter) |
| [`db-tester-spock`](db-tester-spock/) | Spock extension | [![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-spock.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-spock) |
| [`db-tester-spock-spring-boot-starter`](db-tester-spock-spring-boot-starter/) | Spring Boot auto-configuration for Spock | [![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-spock-spring-boot-starter.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-spock-spring-boot-starter) |

</details>

### Gradle

```kotlin
testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
testImplementation("io.github.seijikohara:db-tester-junit")
```

### Maven

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

<dependency>
    <groupId>io.github.seijikohara</groupId>
    <artifactId>db-tester-junit</artifactId>
    <scope>test</scope>
</dependency>
```

## Quick Start

### 1. Create Test Class

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setUp(ExtensionContext context) {
        DataSource dataSource = createDataSource();
        DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);
    }

    @Test
    @Preparation
    @Expectation
    void shouldCreateUser() {
        userRepository.create(new User("john", "john@example.com"));
    }
}
```

### 2. Create Dataset Files

```
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── USERS.csv              # Loaded before test
    └── expected/
        └── USERS.csv          # Verified after test
```

**USERS.csv** (preparation):

```csv
ID,NAME,EMAIL
1,existing,existing@example.com
```

**expected/USERS.csv** (expectation):

```csv
ID,NAME,EMAIL
1,existing,existing@example.com
2,john,john@example.com
```

### 3. Run Test

```bash
# Gradle
./gradlew test

# Maven
./mvnw test
```

## Usage Examples

### JUnit with Spring Boot

Requires `@ExtendWith(SpringBootDatabaseTestExtension.class)` to enable the extension. The DataSource is automatically discovered from the Spring ApplicationContext.

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Preparation
    @Expectation
    void shouldCreateUser() {
        userRepository.save(new User("john", "john@example.com"));
    }
}
```

### Spock

Add `@DatabaseTest` annotation to enable the extension. Provide a `getDbTesterRegistry()` property accessor for DataSource registration.

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification {

    @Shared
    DataSource dataSource

    @Shared
    DataSourceRegistry registry

    // Property accessor required by the framework
    DataSourceRegistry getDbTesterRegistry() {
        return registry
    }

    def setupSpec() {
        dataSource = createDataSource()
        registry = new DataSourceRegistry()
        registry.registerDefault(dataSource)
    }

    @Preparation
    @Expectation
    def "should create user"() {
        when:
        userRepository.create(new User("john", "john@example.com"))

        then:
        noExceptionThrown()
    }
}
```

### Scenario Filtering

Share CSV files across multiple tests using the `[Scenario]` column:

```csv
[Scenario],ID,NAME,EMAIL
shouldCreateUser,1,existing,existing@example.com
shouldUpdateUser,1,target,target@example.com
shouldDeleteUser,1,delete_me,delete@example.com
```

Each test method loads only rows matching its name.

## Configuration

### Operations

| Operation | Description |
|-----------|-------------|
| `CLEAN_INSERT` | Delete all then insert (default) |
| `INSERT` | Insert rows |
| `UPDATE` | Update existing rows |
| `REFRESH` | Upsert (insert or update) |
| `DELETE` | Delete specified rows |
| `DELETE_ALL` | Delete all rows |
| `TRUNCATE_TABLE` | Truncate tables |
| `TRUNCATE_INSERT` | Truncate then insert |

```java
@Preparation(operation = Operation.INSERT)
```

### Data Formats

| Format | Extension |
|--------|-----------|
| CSV | `.csv` (default) |
| TSV | `.tsv` |

```java
ConventionSettings conventions = ConventionSettings.standard()
    .withDataFormat(DataFormat.TSV);
```

### Multiple DataSources

```java
DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);
registry.registerDefault(primaryDataSource);
registry.register("secondary", secondaryDataSource);
```

```java
@Preparation(dataSets = @DataSet(dataSourceName = "secondary"))
```

## Assertion Messages

When expectation verification fails, the framework collects **all differences** and reports them with a human-readable summary followed by YAML details:

```
Assertion failed: 3 differences in USERS, ORDERS
summary:
  status: FAILED
  total_differences: 3
tables:
  USERS:
    differences:
      - path: row_count
        expected: 3
        actual: 2
  ORDERS:
    differences:
      - path: "row[0].STATUS"
        expected: COMPLETED
        actual: PENDING
        column:
          type: VARCHAR(50)
          nullable: true
      - path: "row[1].AMOUNT"
        expected: 100.00
        actual: 99.99
        column:
          type: "DECIMAL(10,2)"
```

### Output Structure

| Field | Description |
|-------|-------------|
| `summary.status` | `FAILED` when differences exist |
| `summary.total_differences` | Total count of all differences |
| `tables.<name>.differences` | List of differences for each table |
| `path` | Location of mismatch: `table_count`, `row_count`, or `row[N].COLUMN` |
| `expected` / `actual` | The expected and actual values |
| `column` | JDBC metadata (type, nullable, primary_key) when available |

The output is **valid YAML** and can be parsed by standard YAML libraries for CI/CD integration.

## Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| `DataSetLoadException: Could not find dataset directory` | CSV path does not match test class | Verify directory structure matches package path |
| `DataSourceNotFoundException` | DataSource not registered | Register in `@BeforeAll` or use Spring Boot starter |

## Documentation

| Document | Description |
|----------|-------------|
| [Technical Specifications](docs/specs/) | Architecture, API, configuration details |
| [Examples](examples/) | Working test examples |

## License

MIT License - see [LICENSE](LICENSE) for details.
