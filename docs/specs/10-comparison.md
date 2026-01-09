# Framework Comparison

This page provides an in-depth comparison of DB Tester with other database testing frameworks in the Java/JVM ecosystem.

## Executive Summary

| Framework | Best For | Trade-offs |
|-----------|----------|------------|
| **DB Tester** | Convention-based testing with JUnit 6/Spock 2/Kotest 6 | Limited data formats, newer project |
| **DBUnit** | Extensive format support and customization | Verbose configuration, no JUnit 5 |
| **Database Rider** | Comprehensive annotation-driven testing | Complex dependency tree |
| **Spring Test DBUnit** | Spring-centric projects | Spring-only, aging project |
| **DbSetup** | Code-only setup without external files | No assertion capabilities |
| **JDBDT** | Incremental change verification | Smaller community |

::: info Testcontainers
[Testcontainers](https://testcontainers.com/) is complementary to these frameworks. It provides database infrastructure (Docker containers), while the frameworks above manage test data. They can be used together.
:::

## Detailed Feature Comparison

### Test Framework Integration

| Feature | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|---------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| JUnit 6 | Yes | - | - | - | - | - |
| JUnit 5 | - | - | Yes | - | Yes | Yes |
| JUnit 4 | - | Yes | Yes | Yes | Yes | Yes |
| Spock 2 | Yes | - | DSL* | - | Yes | - |
| Kotest | Yes | - | - | - | - | - |
| TestNG | - | Yes | - | - | Yes | Yes |
| Spring Boot | Yes | - | Yes | Yes | - | - |
| CDI/Jakarta EE | - | - | Yes | - | - | - |
| Cucumber/BDD | - | - | Yes | - | - | - |

*DSL: Annotations (`@DataSet`, `@ExpectedDataSet`) are supported for Database Rider. DB Tester uses these same annotation names. For Spock with Database Rider, use [RiderDSL](https://database-rider.github.io/database-rider/latest/documentation.html#_rider_dsl) programmatic API.

**Analysis:**
- DB Tester is the only framework with native JUnit 6 and Kotest support
- Database Rider has the widest test framework coverage for JUnit 5, but Spock requires programmatic API
- DBUnit lacks JUnit 5/6 support

### Data Format Support

| Format | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|--------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| CSV | Yes | Yes | Yes | Yes | - | Yes |
| TSV | Yes | - | - | - | - | - |
| Flat XML | - | Yes | Yes | Yes | - | - |
| Full XML | - | Yes | Yes | Yes | - | - |
| YAML | - | - | Yes | - | - | - |
| JSON | - | - | Yes | - | - | - |
| Excel (XLS/XLSX) | - | Yes | Yes | Yes | - | - |
| Java DSL | - | - | Yes | - | Yes | Yes |
| Kotlin DSL | - | - | - | - | Yes | - |
| SQL Scripts | - | - | Yes | - | Yes | - |

**Analysis:**
- Database Rider supports the most formats (YAML, JSON, XML, CSV, Excel)
- DB Tester focuses on CSV/TSV for straightforward data management
- DbSetup and JDBDT prefer programmatic data definition

### Configuration Approach

| Approach | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|----------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| Annotations | Yes | - | Yes | Yes | - | - |
| Convention-based | Yes | - | - | - | - | - |
| Programmatic API | Yes | Yes | Yes | Yes | Yes | Yes |
| External Config (YAML/XML) | - | Yes | Yes | Yes | - | - |
| Global Defaults | Yes | - | Yes | Yes | - | - |

**Convention-based Discovery (DB Tester unique):**
```
src/test/resources/
└── com/example/UserRepositoryTest/    ← Matches test class
    ├── users.csv                       ← Table name
    └── expected/
        └── users.csv                   ← Expected state
```

### Database Operations

| Operation | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|-----------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| NONE | Yes | Yes | Yes | Yes | - | - |
| INSERT | Yes | Yes | Yes | Yes | Yes | Yes |
| UPDATE | Yes | Yes | Yes | Yes | - | - |
| UPSERT | Yes | Yes | Yes | Yes | - | - |
| DELETE | Yes | Yes | Yes | Yes | Yes | Yes |
| DELETE_ALL | Yes | Yes | Yes | Yes | Yes | - |
| TRUNCATE_TABLE | Yes | Yes | Yes | Yes | Yes | - |
| CLEAN_INSERT | Yes | Yes | Yes | Yes | Yes | Yes |
| TRUNCATE_INSERT | Yes | Yes | Yes | Yes | - | - |

### Assertion Capabilities

| Feature | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|---------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| Full State Verification | Yes | Yes | Yes | Yes | - | Yes |
| Delta Assertions | - | - | - | - | - | Yes |
| Column Exclusion | Yes | Yes | Yes | Yes | - | Yes |
| Row Ordering Control | Yes | Yes | Yes | Yes | - | Yes |
| Regex Matching | Yes | - | Yes | - | - | - |
| Scriptable Expected Values | - | - | Yes | - | - | - |
| Scenario Filtering | Yes | - | - | - | - | - |
| Structured Error Output | Yes (YAML) | - | - | - | - | - |

**Delta Assertions (JDBDT unique):**
```java
// Verify only inserted rows, ignore unchanged data
assertInserted(expected);

// Verify query had no side effects
assertUnchanged(dataSource);
```

**Scenario Filtering (DB Tester unique):**
```csv
[Scenario],id,name,email
shouldCreateUser,1,john,john@example.com
shouldUpdateUser,1,john,john.updated@example.com
```

### Advanced Features

| Feature | DB Tester | DBUnit | Database Rider | Spring Test DBUnit | DbSetup | JDBDT |
|---------|:---------:|:------:|:--------------:|:------------------:|:-------:|:-----:|
| Multiple DataSources | Yes | Yes | Yes | Yes | Yes | Yes |
| Transaction Support | Yes | Yes | Yes | Yes | Yes | - |
| FK Constraint Handling | Yes | Yes | Yes | Yes | Yes | - |
| Sequence/Identity Reset | - | Yes | Yes | Yes | - | - |
| Dataset Export | - | Yes | Yes | - | - | Yes |
| Replacement/Placeholder | - | Yes | Yes | - | - | - |
| Scriptable Datasets | - | - | Yes | - | - | - |
| Connection Leak Detection | - | - | Yes | - | - | - |
| SPI Extensibility | Yes | - | - | - | - | - |
| Logging/Diagnostics | Yes | Yes | Yes | Yes | - | Yes |

**Scriptable Datasets (Database Rider):**
```yaml
USER:
  - ID: 1
    NAME: "js:(new Date()).toString()"
    CREATED_AT: "groovy:new Date()"
```

**SPI Extensibility (DB Tester):**
- Custom `TableSetLoaderProvider` implementations
- Custom `OperationProvider` implementations
- Custom `ExpectedDataSetProvider` implementations

---

## DB Tester Limitations

### Data Format Limitations

| Limitation | Impact | Workaround |
|------------|--------|------------|
| **No YAML/JSON support** | Cannot use human-friendly formats for complex nested data | Use CSV with clear column naming |
| **No XML support** | Cannot migrate from existing DBUnit XML datasets | Convert XML to CSV manually or via script |
| **No Excel support** | Business users cannot maintain test data in spreadsheets | Export Excel to CSV |
| **No programmatic dataset builder** | Cannot generate dynamic test data in code | Use SPI to implement custom DataLoader |

### Feature Limitations

| Limitation | Impact | Alternative |
|------------|--------|-------------|
| **No delta assertions** | Cannot verify only the changes made by test | Verify full expected state |

| **No scriptable datasets** | Cannot embed dynamic values in CSV | Prepare data programmatically before test |
| **No dataset export** | Cannot capture current DB state for debugging | Use database client tools |
| **No replacement/placeholder** | Cannot use variables in datasets | Define explicit values per scenario |
| **No sequence reset** | Cannot reset auto-increment counters | Handle via SQL in @BeforeEach |
| **No connection leak detection** | Memory leaks may go unnoticed | Use external monitoring tools |

### Ecosystem Limitations

| Limitation | Impact | Consideration |
|------------|--------|---------------|
| **No JUnit 4/5 support** | Cannot use with legacy test suites | Migrate to JUnit 6 or use Database Rider |
| **No TestNG support** | Limited options for TestNG users | Use DbSetup or JDBDT |
| **No CDI integration** | Cannot auto-inject in Jakarta EE | Manual DataSource registration required |
| **No Cucumber support** | Cannot use in BDD scenarios | Use Database Rider for BDD |
| **New project** | Smaller community, less battle-tested | Evaluate thoroughly before production use |
| **Limited documentation** | Fewer examples and tutorials | Refer to test cases in source code |

### When NOT to Choose DB Tester

Consider alternatives if you need:

1. **Multiple data formats** → Choose Database Rider
2. **Existing XML datasets** → Choose DBUnit or Database Rider
3. **BDD/Cucumber integration** → Choose Database Rider
4. **JUnit 4/5 or TestNG** → Choose DBUnit, Database Rider, or DbSetup
5. **Delta assertions** → Choose JDBDT
6. **Code-only approach** → Choose DbSetup
7. **Mature, battle-tested solution** → Choose DBUnit

---

## Framework Deep Dive

### DB Tester

**Philosophy:** Convention over configuration with minimal boilerplate.

**Unique Strengths:**
- Zero-configuration dataset discovery based on test class/method names
- Scenario filtering allows sharing datasets across multiple test methods
- YAML-formatted assertion errors for readable debugging output
- Native Kotest support (only framework with this)
- SPI for custom extensions

**Architecture:**
```
db-tester-api     → Public annotations and interfaces
db-tester-core    → JDBC implementation (internal)
db-tester-junit   → JUnit 6 extension
db-tester-spock   → Spock 2 extension
db-tester-kotest  → Kotest 6 extension
```

**Example:**
```java
@ExtendWith(DatabaseTestExtension.class)
@DataSet  // Loads com/example/UserTest/users.csv
@ExpectedDataSet  // Verifies against com/example/UserTest/expected/users.csv
class UserTest {
    @Test
    void shouldCreateUser() {
        // [Scenario] column filters rows for this method
        repository.create(new User("john", "john@example.com"));
    }
}
```

### DBUnit

**Philosophy:** Comprehensive database state management with extensive customization options.

**Unique Strengths:**
- Most mature and battle-tested (since 2002)
- Extensive XML dataset support with schema validation
- ReplacementDataSet for dynamic placeholders
- Database export for capturing production-like data
- Wide IDE and tool integration

**Core Components:**
- `IDatabaseConnection` - Database connection abstraction
- `IDataSet` - Collection of tables (FlatXml, Xml, Xls, Query, and others)
- `DatabaseOperation` - CRUD operations on datasets

**Example:**
```java
@Before
public void setUp() throws Exception {
    IDatabaseConnection connection = new DatabaseConnection(dataSource.getConnection());
    IDataSet dataSet = new FlatXmlDataSetBuilder().build(new File("dataset.xml"));
    DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
}
```

### Database Rider

**Philosophy:** Comprehensive DBUnit wrapper with annotation-driven API.

**Unique Strengths:**
- Widest data format support (YAML, JSON, XML, CSV, Excel)
- Scriptable datasets with Groovy/JavaScript
- Regex matching in expected datasets
- CDI and Cucumber integration
- Connection leak detection
- Active development and community

**Configuration Options:**
```java
@DataSet(
    value = "users.yml",
    strategy = SeedStrategy.CLEAN_INSERT,
    cleanBefore = true,
    cleanAfter = true,
    disableConstraints = true,
    transactional = true,
    executeStatementsBefore = "SET FOREIGN_KEY_CHECKS=0",
    executeStatementsAfter = "SET FOREIGN_KEY_CHECKS=1"
)
```

**Example:**
```java
@ExtendWith(DBUnitExtension.class)
class UserTest {
    @Test
    @DataSet("users.yml")
    @ExpectedDataSet(value = "expected.yml", ignoreCols = {"id", "created_at"})
    void shouldUpdateUser() {
        repository.update(1L, new User("john.doe"));
    }
}
```

### Spring Test DBUnit

**Philosophy:** Seamless Spring integration for DBUnit.

**Unique Strengths:**
- Deep Spring TestContext integration
- Transaction management with Spring
- Familiar annotation style for Spring developers
- TestExecutionListener approach

**Limitations:**
- No active maintenance (last release 2016)
- No JUnit 5 support
- Spring-only

**Example:**
```java
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    TransactionDbUnitTestExecutionListener.class
})
@DatabaseSetup("/initial-data.xml")
@ExpectedDatabase("/expected-data.xml")
public class UserRepositoryTest {
    @Test
    public void shouldUpdateUser() { ... }
}
```

### DbSetup

**Philosophy:** Pure code, no external files, fast execution.

**Unique Strengths:**
- Zero external dependencies
- Type-safe Java/Kotlin DSL
- DbSetupTracker for test optimization
- Value generators for sequences
- Fast execution

**Limitations:**
- Setup only, no assertion capabilities
- No annotation support
- Requires more boilerplate code

**Example:**
```java
private static final Operation DELETE_ALL = deleteAllFrom("users", "orders");
private static final Operation INSERT_REFERENCE_DATA = sequenceOf(
    insertInto("users")
        .columns("id", "name", "email")
        .values(1L, "john", "john@example.com")
        .values(2L, "jane", "jane@example.com")
        .build()
);

@BeforeEach
void prepare() {
    new DbSetup(destination, sequenceOf(DELETE_ALL, INSERT_REFERENCE_DATA)).launch();
}
```

**Kotlin DSL:**
```kotlin
val operation = dbSetup(to = destination) {
    deleteAllFrom("users")
    insertInto("users") {
        columns("id", "name", "email")
        values(1L, "john", "john@example.com")
    }
}
operation.launch()
```

### JDBDT

**Philosophy:** Lightweight delta testing without external dependencies.

**Unique Strengths:**
- Delta assertions (verify only changes)
- Self-contained (Java 8 SE only)
- Programmatic dataset builders
- CSV import/export
- Lightweight (~100KB)

**Delta Assertion Concept:**
```
Initial State (Snapshot) → Test Execution → Final State
                                ↓
                          δ = Final - Initial
                                ↓
                    Assert: δ matches expected changes
```

**Example:**
```java
@Before
public void setup() {
    // Take snapshot of initial state
    snapshot = takeSnapshot(userTable);
}

@Test
public void testInsertUser() {
    // Execute code under test
    repository.insert(new User("john"));

    // Assert only the delta (inserted rows)
    assertInserted(
        data(userTable)
            .row("john", "john@example.com")
    );
}

@Test
public void testQueryDoesNotModify() {
    repository.findAll();

    // Assert no changes were made
    assertUnchanged(userTable);
}
```

---

## Decision Matrix

### By Use Case

| Use Case | Recommended | Alternatives |
|----------|-------------|--------------|
| New JUnit 6 project | DB Tester | - |
| JUnit 5 project | Database Rider | DbSetup, JDBDT |
| Spock/Groovy project | DB Tester | DbSetup |
| Kotest/Kotlin project | DB Tester | DbSetup (Kotlin DSL) |
| Legacy JUnit 4/5 project | Database Rider | DBUnit |
| Spring Boot application | DB Tester, Database Rider | Spring Test DBUnit |
| Jakarta EE / CDI | Database Rider | - |
| BDD / Cucumber | Database Rider | - |
| Minimal dependencies | DbSetup, JDBDT | DB Tester |
| Change verification only | JDBDT | - |
| Extensive format flexibility | Database Rider | DBUnit |

### By Team Preference

| Preference | Recommended |
|------------|-------------|
| Convention over configuration | DB Tester |
| Annotation-driven | DB Tester, Database Rider |
| Code-only (no external files) | DbSetup, JDBDT |
| YAML/JSON datasets | Database Rider |
| Battle-tested solution | DBUnit, Database Rider |
| Lightweight | DbSetup, JDBDT, DB Tester |

---

## Migration Guides

### From Database Rider to DB Tester

| Database Rider | DB Tester | Notes |
|----------------|-----------|-------|
| `@DataSet("users.yml")` | `@DataSet` | Convert YAML to CSV |
| `@ExpectedDataSet("expected.yml")` | `@ExpectedDataSet` | Convert YAML to CSV |
| `strategy = SeedStrategy.CLEAN_INSERT` | `operation = Operation.CLEAN_INSERT` | Same semantics |
| `ignoreCols = {"id"}` | `excludeColumns = {"id"}` | Same functionality |
| `cleanBefore = true` | Default behavior | CLEAN_INSERT is default |
| `dbunit.yml` config | `@DatabaseTestConfiguration` | Annotation-based |

### From Spring Test DBUnit to DB Tester

| Spring Test DBUnit | DB Tester | Notes |
|--------------------|-----------|-------|
| `@DatabaseSetup("/data.xml")` | `@DataSet` | Convert XML to CSV |
| `@ExpectedDatabase("/expected.xml")` | `@ExpectedDataSet` | Convert XML to CSV |
| `DbUnitTestExecutionListener` | `DatabaseTestExtension` | JUnit 6 extension |
| `@DbUnitConfiguration` | `@DatabaseTestConfiguration` | Similar options |

### From DbSetup to DB Tester

| DbSetup | DB Tester | Notes |
|---------|-----------|-------|
| `insertInto("users").columns(...).values(...)` | `users.csv` file | Externalize to file |
| `deleteAllFrom("users")` | Implicit in CLEAN_INSERT | Default behavior |
| `DbSetupTracker` | Not needed | Each test has own data |
| No assertions | `@ExpectedDataSet` | Add verification |

---

## References

### Official Documentation
- [DBUnit](https://www.dbunit.org/)
- [Database Rider](https://database-rider.github.io/database-rider/)
- [Spring Test DBUnit](https://springtestdbunit.github.io/spring-test-dbunit/)
- [DbSetup](https://dbsetup.ninja-squad.com/)
- [JDBDT](https://jdbdt.github.io/)

### Related Tools
- [Testcontainers](https://testcontainers.com/) - Database containers for integration testing
- [Flyway](https://flywaydb.org/) - Database migration tool
- [Liquibase](https://www.liquibase.org/) - Database change management
