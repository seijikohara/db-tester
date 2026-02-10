# DB Tester

[![Test](https://github.com/seijikohara/db-tester/actions/workflows/test.yml/badge.svg)](https://github.com/seijikohara/db-tester/actions/workflows/test.yml)
[![codecov](https://codecov.io/gh/seijikohara/db-tester/graph/badge.svg)](https://codecov.io/gh/seijikohara/db-tester)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.seijikohara/db-tester-bom.svg)](https://search.maven.org/artifact/io.github.seijikohara/db-tester-bom)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![Docs](https://img.shields.io/badge/Docs-VitePress-646cff.svg)](https://seijikohara.github.io/db-tester/)

<div align="center">
  <img src="docs/public/favicon.svg" width="200" alt="DB Tester Logo">
</div>

A database testing framework for JUnit 6, Spock 2, and Kotest 6. The framework prepares database state from CSV, TSV, JSON, and YAML test data before tests and verifies it after tests using `@DataSet` and `@ExpectedDataSet` annotations.

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
    @DataSet  // Loads USERS.csv before test
    @ExpectedDataSet  // Verifies expected/USERS.csv after test
    void shouldCreateUser() {
        userRepository.create(new User("john", "john@example.com"));
    }
}
```

### Spock

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification implements DatabaseTestSupport {

    @Shared
    DataSourceRegistry dbTesterRegistry

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(createDataSource())
    }

    @DataSet
    @ExpectedDataSet
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
    @DataSet
    @ExpectedDataSet
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
| Annotation-driven | Declarative test data management with `@DataSet` and `@ExpectedDataSet` |
| Convention-based | Automatic dataset discovery based on test class package and name |
| Multi-format data | CSV, TSV, JSON, and YAML dataset support |
| Template expressions | Dynamic values using `${uuid}`, `${sequence}`, `${now}`, `${faker.*}` |
| 11 comparison strategies | Column-level verification: STRICT, REGEX, DATE_FLEXIBLE, JSON_EQUIVALENT, and more |
| Dataset export | Export database tables to CSV, TSV, JSON, or YAML via `DataSetExporter` |
| Scenario filtering | Share dataset files across tests using the `[Scenario]` column |
| Batch insert | Configurable batch size for large dataset insertion |
| Retry mechanism | Configurable retry with delay for async operation verification |
| Programmatic assertion API | `DatabaseAssertion` facade for code-based verification |
| Spring Boot integration | Automatic DataSource registration from ApplicationContext |
| Pure JDBC | No ORM or external testing framework dependencies |
| SPI extensibility | Custom providers for data loading, operations, and export via ServiceLoader |

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
| `db-tester-spring-support` | Common Spring utilities for DataSource registration |
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

Spring Boot starters automatically discover and register `DataSource` beans from the ApplicationContext. Manual registration is not required.

**Default DataSource resolution priority** (for multiple DataSource beans):

1. Single `DataSource` bean (automatic default)
2. `@Primary`-annotated `DataSource`
3. `DataSource` bean named `"dataSource"`

### JUnit with Spring Boot

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DataSet
    @ExpectedDataSet
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

    @DataSet
    @ExpectedDataSet
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
    @DataSet
    @ExpectedDataSet
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
db-tester.convention.data-format=AUTO
db-tester.convention.expectation-suffix=/expected
db-tester.operation.preparation=CLEAN_INSERT
```

See the [Configuration](https://seijikohara.github.io/db-tester/configuration) documentation for all options.

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

**Behavior details**:

- CSV files without a `[Scenario]` column load all rows for every test
- By default, the test method name is used as the scenario name for filtering
- Override with `@DataSetSource(scenarioNames = {"scenario1", "scenario2"})` to load rows matching any of the specified scenarios (OR filter)

### Custom Resource Location

Specify explicit resource locations instead of convention-based discovery:

```java
@DataSet(sources = @DataSetSource(resourceLocation = "custom/data"))
@ExpectedDataSet(sources = @DataSetSource(resourceLocation = "custom/expected"))
void testWithCustomLocation() { }
```

### Column Exclusion

Exclude columns (such as timestamps or auto-generated IDs) from verification:

**Per-dataset exclusion** via `@DataSetSource.excludeColumns`:

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT", "VERSION"}
))
void testWithExcludedColumns() {
    userRepository.create(new User("john", "john@example.com"));
}
```

**Global exclusion** via `VerificationSettings.globalExcludeColumns`:

```java
@BeforeAll
static void setUp(ExtensionContext context) {
    var config = Configuration.builder()
        .verification(VerificationSettings.builder()
            .globalExcludeColumns(Set.of("CREATED_AT", "UPDATED_AT"))
            .build())
        .build();
    DatabaseTestExtension.setConfiguration(context, config);
    DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);
}
```

**Spring Boot configuration**:

```properties
db-tester.verification.global-exclude-columns=CREATED_AT,UPDATED_AT,VERSION
```

Column names are case-insensitive. Per-dataset exclusions are combined with global exclusions.

### Column Comparison Strategies

Override the default strict comparison for specific columns using `@ColumnStrategy`:

| Strategy | Description |
|----------|-------------|
| `STRICT` | Exact match using `equals()` (default) |
| `IGNORE` | Skip comparison entirely |
| `NUMERIC` | Type-aware numeric comparison |
| `CASE_INSENSITIVE` | Case-insensitive string comparison |
| `TIMESTAMP_FLEXIBLE` | Converts to UTC and ignores sub-second precision |
| `DATE_FLEXIBLE` | Multi-format date comparison (ISO-8601, slashed, dot) |
| `JSON_EQUIVALENT` | JSON structural comparison (ignores key order and whitespace) |
| `NOT_NULL` | Verifies value is not null |
| `REGEX` | Pattern matching (requires `pattern` attribute) |

**Per-dataset strategy** via `@DataSetSource.columnStrategies`:

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.TIMESTAMP_FLEXIBLE),
        @ColumnStrategy(name = "EMAIL", strategy = Strategy.REGEX, pattern = ".*@example\\.com")
    }
))
void testWithColumnStrategies() {
    userRepository.create(new User("john", "john@example.com"));
}
```

**Spring Boot configuration**:

```properties
db-tester.verification.column-strategies[0].column-name=CREATED_AT
db-tester.verification.column-strategies[0].strategy=TIMESTAMP_FLEXIBLE
```

Column names in strategies are case-insensitive. Annotation-level strategies override global strategies. Excluded columns take precedence over strategies.

### Template Expressions

Dataset values support template expressions that generate dynamic values at load time:

| Expression | Description | Example Output |
|------------|-------------|----------------|
| `${uuid}` | Random UUID | `550e8400-e29b-41d4-a716-446655440000` |
| `${sequence:N}` | Initialize sequence counter to N | `1` |
| `${sequence}` | Increment and return next value | `2`, `3`, `4`, ... |
| `${now}` | Current timestamp (ISO-8601) | `2024-01-15T10:30:00` |
| `${now+Xd}` | Relative future date (d=days, h=hours, m=minutes, s=seconds) | `2024-01-22T10:30:00` |
| `${now-Xd}` | Relative past date | `2024-01-08T10:30:00` |
| `${faker.xxx.yyy}` | Datafaker expression (optional dependency) | Varies |

```csv
ID,NAME,EMAIL,CREATED_AT
${sequence:1},${faker.name.fullName},user_${sequence}@example.com,${now}
```

The `${faker.*}` expressions require [Datafaker](https://www.datafaker.net/) as an optional runtime dependency:

```kotlin
testRuntimeOnly("net.datafaker:datafaker:VERSION")
```

If Datafaker is not on the classpath, `${faker....}` expressions are left unprocessed.

See the [Advanced Usage - Template Expressions](https://seijikohara.github.io/db-tester/advanced-usage#_9-template-expressions) documentation for details.

### Dataset Export

Export database tables to files for debugging or creating expected datasets:

```java
// Export tables to CSV files
DataSetExporter.csv(dataSource, List.of("USERS", "ORDERS"), Paths.get("export"));

// Export with custom configuration
var config = ExportConfiguration.builder()
    .lobHandling(LobHandling.OMIT)
    .writeLoadOrderFile(true)
    .build();
DataSetExporter.export(dataSource, List.of("USERS"), Paths.get("export"), DataFormat.JSON, config);

// Export SQL query results
DataSetExporter.exportQuery(dataSource, "SELECT * FROM USERS WHERE active = true",
    "ACTIVE_USERS", Paths.get("export"), DataFormat.CSV);
```

See the [Public API - Export API](https://seijikohara.github.io/db-tester/public-api#export-api) documentation for full reference.

---

## Configuration

### Operations

| Operation | Description |
|-----------|-------------|
| `NONE` | No database operation |
| `INSERT` | Insert rows |
| `UPDATE` | Update existing rows |
| `UPSERT` | Upsert (insert or update) |
| `DELETE` | Delete specified rows |
| `DELETE_ALL` | Delete all rows |
| `TRUNCATE_TABLE` | Truncate tables |
| `CLEAN_INSERT` | Delete all rows, then insert (default) |
| `TRUNCATE_INSERT` | Truncate, then insert |

```java
@DataSet(operation = Operation.INSERT)
```

### Data Formats

| Format | Extension |
|--------|-----------|
| AUTO | All supported (default) |
| CSV | `.csv` |
| TSV | `.tsv` |
| JSON | `.json` |
| YAML | `.yaml` |

The default `AUTO` mode detects all supported file formats in the dataset directory. If the same table name exists in multiple formats, an error is reported.

```java
// Explicitly specify a single format
ConventionSettings conventions = ConventionSettings.builder()
    .dataFormat(DataFormat.TSV)
    .build();
```

### Multiple DataSources

```java
DataSourceRegistry registry = DatabaseTestExtension.getRegistry(context);
registry.registerDefault(primaryDataSource);
registry.register("secondary", secondaryDataSource);
```

```java
@DataSet(sources = @DataSetSource(dataSourceName = "secondary"))
```

---

## Assertion Output

When verification fails, the framework outputs differences in YAML format:

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
