# DB Tester

[![Test](https://github.com/seijikohara/db-tester/actions/workflows/test.yml/badge.svg)](https://github.com/seijikohara/db-tester/actions/workflows/test.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-bom.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-bom)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Docs](https://img.shields.io/badge/Docs-VitePress-646cff.svg)](https://seijikohara.github.io/db-tester/)

<div align="center">
  <img src="docs/public/favicon.svg" width="200" alt="DB Tester Logo">
</div>

A database testing framework for JUnit 6, Spock 2, and Kotest 6. Load CSV test data before tests and verify database state after tests using `@Preparation` and `@Expectation` annotations.

**[Documentation](https://seijikohara.github.io/db-tester/)** · **[Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-bom)** · **[Examples](examples/)**

---

## Quick Start

### JUnit

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setUp(ExtensionContext context) {
        DataSource dataSource = createDataSource();
        DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);
    }

    @Test
    @Preparation  // Loads USERS.csv before test
    @Expectation  // Verifies expected/USERS.csv after test
    void shouldCreateUser() {
        userRepository.create(new User("john", "john@example.com"));
    }
}
```

### Spock

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification {

    @Shared
    DataSourceRegistry dbTesterRegistry

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(createDataSource())
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

### Kotest

```kotlin
class UserRepositorySpec : AnnotationSpec() {

    private val registry = DataSourceRegistry()

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    @BeforeAll
    fun setupSpec() {
        registry.registerDefault(createDataSource())
    }

    @Test
    @Preparation
    @Expectation
    fun `should create user`() {
        userRepository.create(User("john", "john@example.com"))
    }
}
```

### Dataset Files

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

---

## Features

| Feature | Description |
|---------|-------------|
| Annotation-driven | Declarative test data management with `@Preparation` and `@Expectation` |
| Convention-based | Automatic dataset discovery based on test class package and name |
| Scenario filtering | Share CSV files across tests using the `[Scenario]` column |
| Spring Boot integration | Automatic DataSource registration from ApplicationContext |
| Pure JDBC | No ORM or external testing framework dependencies |

---

## Requirements

| Component | Version |
|-----------|---------|
| Java | 21 or later |
| JUnit | 6 (for JUnit integration) |
| Spock | 2 with Groovy 5 (for Spock integration) |
| Kotest | 6 with Kotlin 2 (for Kotest integration) |
| Spring Boot | 4 (for Spring Boot integration) |

---

## Installation

Select a module based on your test framework:

| Use Case | Module |
|----------|--------|
| JUnit | `db-tester-junit` |
| JUnit with Spring Boot | `db-tester-junit-spring-boot-starter` |
| Spock | `db-tester-spock` |
| Spock with Spring Boot | `db-tester-spock-spring-boot-starter` |
| Kotest | `db-tester-kotest` |
| Kotest with Spring Boot | `db-tester-kotest-spring-boot-starter` |

<details>
<summary>All Modules</summary>

| Module | Description |
|--------|-------------|
| `db-tester-bom` | Bill of Materials for version management |
| `db-tester-api` | Public API (annotations, configuration, SPI) |
| `db-tester-core` | Internal implementation |
| `db-tester-junit` | JUnit extension |
| `db-tester-spock` | Spock extension |
| `db-tester-kotest` | Kotest extension |
| `db-tester-junit-spring-boot-starter` | Spring Boot auto-configuration for JUnit |
| `db-tester-spock-spring-boot-starter` | Spring Boot auto-configuration for Spock |
| `db-tester-kotest-spring-boot-starter` | Spring Boot auto-configuration for Kotest |

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

---

## Spring Boot Integration

Spring Boot starters automatically discover and register `DataSource` beans from the ApplicationContext. No manual registration is required.

### JUnit with Spring Boot

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

### Spock with Spring Boot

```groovy
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositorySpec extends Specification {

    @Autowired
    UserRepository userRepository

    @Preparation
    @Expectation
    def "should create user"() {
        when:
        userRepository.save(new User("john", "john@example.com"))

        then:
        noExceptionThrown()
    }
}
```

### Kotest with Spring Boot

```kotlin
@SpringBootTest
class UserRepositorySpec : AnnotationSpec() {

    @Autowired
    private lateinit var userRepository: UserRepository

    init {
        extensions(SpringBootDatabaseTestExtension())
    }

    @Test
    @Preparation
    @Expectation
    fun `should create user`() {
        userRepository.save(User("john", "john@example.com"))
    }
}
```

### Configuration Properties

Configure via `application.properties`:

```properties
db-tester.enabled=true
db-tester.auto-register-data-sources=true
db-tester.convention.data-format=CSV
db-tester.convention.expectation-suffix=/expected
db-tester.operation.preparation=CLEAN_INSERT
```

See the [Configuration](https://seijikohara.github.io/db-tester/specs/04-configuration) documentation for all options.

---

## Usage Examples

### Scenario Filtering

Share CSV files across multiple tests using the `[Scenario]` column:

```csv
[Scenario],ID,NAME,EMAIL
shouldCreateUser,1,existing,existing@example.com
shouldUpdateUser,1,target,target@example.com
shouldDeleteUser,1,delete_me,delete@example.com
```

Each test method loads only rows matching its name.

### Custom Paths

Specify explicit paths instead of convention-based discovery:

```java
@Preparation(paths = "custom/data/users.csv")
@Expectation(paths = "custom/expected/users.csv")
void testWithCustomPaths() { }
```

### Column Exclusion

Exclude columns from verification (timestamps, auto-generated IDs):

```java
@Expectation(excludeColumns = {"CREATED_AT", "UPDATED_AT"})
void testWithExcludedColumns() { }
```

---

## Configuration

### Operations

| Operation | Description |
|-----------|-------------|
| `CLEAN_INSERT` | Delete all rows, then insert (default) |
| `INSERT` | Insert rows |
| `UPDATE` | Update existing rows |
| `REFRESH` | Upsert (insert or update) |
| `DELETE` | Delete specified rows |
| `DELETE_ALL` | Delete all rows |
| `TRUNCATE_TABLE` | Truncate tables |
| `TRUNCATE_INSERT` | Truncate, then insert |

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

---

## Assertion Output

When verification fails, the framework reports differences in YAML format:

```yaml
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
| `summary.total_differences` | Total count of differences |
| `tables.<name>.differences` | List of differences for each table |
| `path` | Location: `table_count`, `row_count`, or `row[N].COLUMN` |
| `expected` / `actual` | Expected and actual values |
| `column` | JDBC metadata when available |

---

## Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| `DataSetLoadException: Could not find dataset directory` | CSV path does not match test class | Verify directory structure matches package path |
| `DataSourceNotFoundException` | DataSource not registered | Register in `@BeforeAll` or use Spring Boot starter |

---

## Documentation

| Resource | Description |
|----------|-------------|
| [Technical Specifications](https://seijikohara.github.io/db-tester/) | Architecture, API, and configuration |
| [Examples](examples/) | Working test examples for all frameworks |

---

## License

MIT License. See [LICENSE](LICENSE) for details.
