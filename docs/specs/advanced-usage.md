---
title: "Advanced Usage - DB Tester"
description: "Implementation examples for complex test scenarios including foreign keys, scenario filtering, comparison strategies, and multiple data sources."
---

# Advanced Usage

This guide demonstrates complex test scenarios with implementation examples.

## 1. Foreign Key Constraint Handling

When tables have foreign key relationships, configure the table ordering strategy
to insert parent records before child records.

### Table Ordering Strategies

| Strategy | Description |
|----------|-------------|
| `AUTO` | Processes tables alphabetically (default) |
| `FOREIGN_KEY` | Resolves insertion order based on foreign key constraints |
| `TABLE_ORDERING_FILE` | Uses explicit ordering defined in `table-ordering.txt` |

### Example: Parent-Child Tables

```java
@Test
@DataSet(tableOrdering = TableOrderingStrategy.FOREIGN_KEY)
@ExpectedDataSet(tableOrdering = TableOrderingStrategy.FOREIGN_KEY)
void shouldInsertOrdersWithForeignKeys() throws SQLException {
    // Inserts USERS first, then ORDERS (respecting FK constraint)
    // Test logic here
}
```

CSV files:

`USERS.csv`:

```csv
ID,NAME,EMAIL
1,Alice,alice@example.com
2,Bob,bob@example.com
```

`ORDERS.csv`:

```csv
ID,USER_ID,AMOUNT,STATUS
1001,1,99.99,PENDING
1002,2,149.50,COMPLETED
```

### Using table-ordering.txt

Create `table-ordering.txt` in the dataset directory to specify insertion order:

```
USERS
ORDERS
ORDER_ITEMS
```

```java
@Test
@DataSet(tableOrdering = TableOrderingStrategy.TABLE_ORDERING_FILE)
void shouldFollowExplicitTableOrder() throws SQLException {
    // Tables processed in the order listed in table-ordering.txt
}
```

## 2. Scenario Filtering

Scenario filtering allows multiple test methods to share a single data file.
The `[Scenario]` marker column selects rows for each test.

### Basic Scenario Usage

`USERS.csv`:

```csv
[Scenario],ID,NAME,EMAIL
shouldFindActiveUsers,1,Alice,alice@example.com
shouldFindActiveUsers,2,Bob,bob@example.com
shouldFindInactiveUsers,3,Charlie,charlie@example.com
shouldFindInactiveUsers,4,Diana,diana@example.com
```

```java
@Test
@DataSet
void shouldFindActiveUsers() throws SQLException {
    // Loads only rows where [Scenario] = "shouldFindActiveUsers"
    // Database contains: Alice (ID=1) and Bob (ID=2)
}

@Test
@DataSet
void shouldFindInactiveUsers() throws SQLException {
    // Loads only rows where [Scenario] = "shouldFindInactiveUsers"
    // Database contains: Charlie (ID=3) and Diana (ID=4)
}
```

### Custom Scenario Names

Override the default method-name matching with explicit scenario names:

```java
@Test
@DataSet(sources = @DataSetSource(scenarioNames = {"basic", "premium"}))
void shouldHandleMultipleUserTypes() throws SQLException {
    // Loads rows matching either "basic" or "premium" scenario
}
```

`USERS.csv`:

```csv
[Scenario],ID,NAME,PLAN
basic,1,Alice,FREE
basic,2,Bob,FREE
premium,3,Charlie,GOLD
premium,4,Diana,PLATINUM
admin,5,Eve,ADMIN
```

This test loads users 1 through 4, excluding the "admin" row (Eve).

### Shared Rows Across Scenarios

Rows without a scenario value (blank or null) are included in all scenarios:

```csv
[Scenario],ID,NAME,ROLE
,1,System,SYSTEM
shouldTestAdmin,2,Admin,ADMIN
shouldTestUser,3,User,USER
```

Both `shouldTestAdmin` and `shouldTestUser` include the System row (ID=1).

## 3. Column Comparison Strategies

The `@ColumnStrategy` annotation controls how individual columns are compared
during expectation verification.

### Ignoring Auto-Generated Columns

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT", "VERSION"}
))
void shouldCreateUser() throws SQLException {
    // Verification ignores CREATED_AT, UPDATED_AT, and VERSION columns
}
```

### Combining Multiple Strategies

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "ID", strategy = Strategy.REGEX, pattern = "[a-f0-9-]{36}"),
        @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
        @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
        @ColumnStrategy(name = "BALANCE", strategy = Strategy.NUMERIC)
    }
))
void shouldProcessTransaction() throws SQLException {
    // ID: validated as UUID format
    // EMAIL: compared case-insensitively
    // CREATED_AT: skipped entirely
    // BALANCE: compared by numeric value (ignores type differences)
}
```

### Flexible Date and Timestamp Comparison

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "BIRTH_DATE", strategy = Strategy.DATE_FLEXIBLE),
        @ColumnStrategy(name = "LOGIN_AT", strategy = Strategy.TIMESTAMP_FLEXIBLE)
    }
))
void shouldHandleDateFormats() throws SQLException {
    // BIRTH_DATE: accepts "2024-01-15", "2024/01/15", "2024.01.15"
    // LOGIN_AT: normalizes to UTC, ignores sub-second precision
}
```

### JSON Structural Comparison

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "METADATA", strategy = Strategy.JSON_EQUIVALENT)
    }
))
void shouldStoreJsonMetadata() throws SQLException {
    // {"b":2,"a":1} and {"a":1,"b":2} are considered equal
    // Ignores key order and insignificant whitespace
}
```

### Range and Containment Verification

```java
@Test
@DataSet
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "SCORE", strategy = Strategy.RANGE, options = "min=0,max=100"),
        @ColumnStrategy(name = "DESCRIPTION", strategy = Strategy.CONTAINS)
    }
))
void shouldValidateScoreAndDescription() throws SQLException {
    // SCORE: actual value must be between 0 and 100 (inclusive)
    // DESCRIPTION: actual value must contain the expected value as substring
}
```

### Strategy Precedence

The framework applies strategies in this order:

1. `excludeColumns` - Columns listed here are excluded from comparison
2. `columnStrategies` - Per-column strategies override the default
3. `STRICT` - Default comparison for columns without explicit strategy

## 4. Multiple DataSource Usage

Register multiple data sources to test across databases.

### Registering Multiple DataSources

```java
@BeforeAll
static void setUp(ExtensionContext context) throws SQLException {
    var primaryDs = createDataSource("jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1");
    var secondaryDs = createDataSource("jdbc:h2:mem:secondary;DB_CLOSE_DELAY=-1");

    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(primaryDs);
    registry.register("secondary", secondaryDs);

    createTables(primaryDs, "CREATE TABLE USERS (ID INT PRIMARY KEY, NAME VARCHAR(100))");
    createTables(secondaryDs, "CREATE TABLE AUDIT_LOG (ID INT PRIMARY KEY, ACTION VARCHAR(255))");
}
```

### Using Named DataSources in Annotations

```java
@Test
@DataSet(sources = {
    @DataSetSource(resourceLocation = "classpath:data/primary/"),
    @DataSetSource(dataSourceName = "secondary", resourceLocation = "classpath:data/secondary/")
})
@ExpectedDataSet(sources = {
    @DataSetSource(resourceLocation = "classpath:data/primary/expected/"),
    @DataSetSource(dataSourceName = "secondary", resourceLocation = "classpath:data/secondary/expected/")
})
void shouldWriteToMultipleDatabases() throws SQLException {
    // Primary DB: loads from classpath:data/primary/
    // Secondary DB: loads from classpath:data/secondary/
    // Both databases verified after test execution
}
```

## 5. Retry for Eventual Consistency

Configure retries for tests involving asynchronous operations.

### Per-Method Retry

```java
@Test
@DataSet
@ExpectedDataSet(retryCount = 5, retryDelayMillis = 500)
void shouldProcessAsyncEvent() throws SQLException {
    // Trigger async operation
    eventPublisher.publish(new UserCreatedEvent(1, "Alice"));

    // @ExpectedDataSet retries up to 5 times with 500ms delay
    // Total maximum wait: 2500ms
}
```

### Global Retry Configuration

```java
var operations = OperationDefaults.builder()
    .retryCount(3)
    .retryDelay(Duration.ofMillis(200))
    .build();

var config = Configuration.builder()
    .operations(operations)
    .build();
```

Individual methods override global settings when `retryCount` or
`retryDelayMillis` is explicitly set (not `-1`).

## 6. Unordered Row Comparison

When database queries return rows in unpredictable order, use unordered comparison.

### Per-Method Configuration

```java
@Test
@DataSet
@ExpectedDataSet(rowOrdering = RowOrdering.UNORDERED)
void shouldReturnUsersInAnyOrder() throws SQLException {
    // Rows are compared as sets, ignoring positional order
    // {Alice, Bob} matches {Bob, Alice}
}
```

### Global Configuration

```java
var conventions = ConventionSettings.builder()
    .rowOrdering(RowOrdering.UNORDERED)
    .build();

var config = Configuration.builder()
    .conventions(conventions)
    .build();
```

## 7. Spring Boot Integration

The Spring Boot starters automatically register Spring-managed DataSource beans.

### Dependencies

::: code-group

```kotlin [JUnit]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")
}
```

```groovy [Spock]
dependencies {
    testImplementation platform("io.github.seijikohara:db-tester-bom:VERSION")
    testImplementation "io.github.seijikohara:db-tester-spock-spring-boot-starter"
}
```

```kotlin [Kotest]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-kotest-spring-boot-starter")
}
```

:::

### Test Class

```java
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @DataSet
    @ExpectedDataSet
    void shouldCreateUser() {
        // Spring Boot auto-configures DataSource registration
        // No manual @BeforeAll setup required

        userService.create(new User("Alice", "alice@example.com"));

        // @ExpectedDataSet verifies the database state
    }
}
```

### application.yml Configuration

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
    username: sa
    password:
    driver-class-name: org.h2.Driver
```

### Multiple DataSources with Spring Boot

```java
@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    DataSource primaryDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1")
            .build();
    }

    @Bean("secondary")
    DataSource secondaryDataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:h2:mem:secondary;DB_CLOSE_DELAY=-1")
            .build();
    }
}
```

The starter registers all `DataSource` beans using their bean name.
The `@Primary` bean becomes the default data source.

## 8. Convention-Based vs Explicit Paths

### Convention-Based (Default)

```java
@DataSet           // src/test/resources/com/example/MyTest/
@ExpectedDataSet   // src/test/resources/com/example/MyTest/expected/
```

Directory structure:

```
src/test/resources/com/example/MyTest/
├── USERS.csv
├── ORDERS.csv
└── expected/
    ├── USERS.csv
    └── ORDERS.csv
```

### Explicit Resource Location

```java
@DataSet(sources = @DataSetSource(resourceLocation = "classpath:shared/common-data/"))
void testWithSharedData() { }
```

Explicit paths are useful for sharing datasets across test classes.

## Related Documentation

- [Getting Started](getting-started) - First test setup
- [Public API](public-api) - Annotation and configuration reference
- [Configuration](configuration) - Framework configuration details
- [Data Formats](data-formats) - CSV and TSV format specification
- [Comparison](comparison) - Comparison strategy details
- [Test Frameworks](test-frameworks) - JUnit, Spock, and Kotest integration
