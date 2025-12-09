# DB Tester Specification - Test Framework Integration

This document describes the integration with JUnit and Spock test frameworks.

---

## Table of Contents

1. [JUnit Integration](#junit-integration)
2. [Spock Integration](#spock-integration)
3. [Spring Boot Integration](#spring-boot-integration)
4. [Lifecycle Hooks](#lifecycle-hooks)

---

## JUnit Integration

### Module

`db-tester-junit`

### Extension Class

**Location**: `io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension`

**Implemented Interfaces**:
- `BeforeEachCallback` - Preparation phase execution
- `AfterEachCallback` - Expectation phase verification
- `ParameterResolver` - `ExtensionContext` injection

### Registration

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {
    // ...
}
```

### DataSource Registration

Register data sources in `@BeforeAll`:

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setup(ExtensionContext context) {
        var registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Test
    @Preparation
    @Expectation
    void testCreateUser() {
        // Test implementation
    }
}
```

### Configuration Customization

```java
@BeforeAll
static void setup(ExtensionContext context) {
    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(dataSource);

    var conventions = ConventionSettings.standard()
        .withDataFormat(DataFormat.TSV);
    var config = Configuration.withConventions(conventions);
    DatabaseTestExtension.setConfiguration(context, config);
}
```

### Static Methods

| Method | Description |
|--------|-------------|
| `getRegistry(ExtensionContext)` | Returns or creates DataSourceRegistry |
| `setConfiguration(ExtensionContext, Configuration)` | Sets custom configuration |

### Nested Test Classes

The extension shares state across nested test classes:

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {

    @BeforeAll
    static void setup(ExtensionContext context) {
        var registry = DatabaseTestExtension.getRegistry(context);
        registry.registerDefault(dataSource);
    }

    @Nested
    class CreateTests {
        @Test
        @Preparation
        @Expectation
        void testCreateUser() { }  // Uses parent's registry
    }

    @Nested
    class UpdateTests {
        @Test
        @Preparation
        @Expectation
        void testUpdateUser() { }  // Uses parent's registry
    }
}
```

### Annotation Precedence

Method-level annotations override class-level:

```java
@Preparation(operation = Operation.CLEAN_INSERT)  // Class default
class UserRepositoryTest {

    @Test
    @Preparation(operation = Operation.INSERT)  // Overrides class
    void testWithInsert() { }

    @Test
    @Preparation  // Uses class default
    void testWithDefault() { }
}
```

---

## Spock Integration

### Module

`db-tester-spock`

### Extension Class

**Location**: `io.github.seijikohara.dbtester.spock.extension.DatabaseTestExtension`

**Type**: Global extension (`IGlobalExtension`)

### Automatic Registration

The extension is automatically registered via `META-INF/services`:

```
# META-INF/services/org.spockframework.runtime.extension.IGlobalExtension
io.github.seijikohara.dbtester.spock.extension.DatabaseTestExtension
```

No explicit extension registration required in test classes.

### DataSource Registration

Use a `@Shared` field named `dbTesterRegistry`:

```groovy
class UserRepositorySpec extends Specification {

    @Shared
    DataSourceRegistry dbTesterRegistry

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @Preparation
    @Expectation
    def 'should create user'() {
        // Test implementation
    }
}
```

### Configuration Customization

Use a `@Shared` field named `dbTesterConfiguration`:

```groovy
class UserRepositorySpec extends Specification {

    @Shared
    DataSourceRegistry dbTesterRegistry

    @Shared
    Configuration dbTesterConfiguration

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)

        def conventions = ConventionSettings.standard()
            .withDataFormat(DataFormat.TSV)
        dbTesterConfiguration = Configuration.withConventions(conventions)
    }

    @Preparation
    @Expectation
    def 'should create user'() { }
}
```

### Reserved Field Names

| Field Name | Type | Purpose |
|------------|------|---------|
| `dbTesterRegistry` | `DataSourceRegistry` | Data source registration |
| `dbTesterConfiguration` | `Configuration` | Custom configuration |

### Feature Method Naming

The scenario name is derived from the feature method:

```groovy
@Preparation
def 'should create user with email'() {
    // Scenario name: "should create user with email"
}
```

### Data-Driven Tests

For parameterized tests with `where:` blocks, the iteration name is used:

```groovy
@Preparation
def 'should process #status order'() {
    expect:
    // Test implementation

    where:
    status << ['PENDING', 'COMPLETED']
}
```

Scenario names: `"should process PENDING order"`, `"should process COMPLETED order"`

---

## Spring Boot Integration

### JUnit Spring Boot Starter

**Module**: `db-tester-junit-spring-boot-starter`

**Extension**: `SpringBootDatabaseTestExtension`

### Automatic DataSource Discovery

The Spring Boot extension automatically:
1. Detects Spring `ApplicationContext`
2. Finds `DataSource` beans
3. Registers them with `DataSourceRegistry`

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class UserRepositoryTest {

    @Test
    @Preparation
    @Expectation
    void testCreateUser() {
        // DataSource automatically registered from Spring context
    }
}
```

### Multiple DataSources

For multiple data sources, use `@Qualifier`:

```java
@Configuration
class DataSourceConfig {

    @Bean
    @Primary
    DataSource primaryDataSource() { }

    @Bean
    @Qualifier("secondary")
    DataSource secondaryDataSource() { }
}
```

```java
@SpringBootTest
@ExtendWith(SpringBootDatabaseTestExtension.class)
class MultiDatabaseTest {

    @Test
    @Preparation(dataSets = {
        @DataSet(dataSourceName = ""),          // Primary (default)
        @DataSet(dataSourceName = "secondary")  // Secondary
    })
    void testMultipleDatabases() { }
}
```

### Configuration Properties

Configure via `application.properties` or `application.yml`:

```properties
# Data format (CSV or TSV)
db-tester.conventions.data-format=CSV

# Expectation directory suffix
db-tester.conventions.expectation-suffix=/expected

# Scenario marker column name
db-tester.conventions.scenario-marker=[Scenario]

# Table merge strategy (FIRST, LAST, UNION, UNION_ALL)
db-tester.conventions.table-merge-strategy=UNION_ALL

# Default preparation operation
db-tester.operations.preparation-operation=CLEAN_INSERT
```

### Spock Spring Boot Starter

**Module**: `db-tester-spock-spring-boot-starter`

**Extension**: `SpringBootDatabaseTestExtension` (Groovy)

```groovy
@SpringBootTest
class UserRepositorySpec extends Specification {

    @Test
    @Preparation
    @Expectation
    def 'should create user'() {
        // DataSource automatically registered from Spring context
    }
}
```

### Auto-Configuration

Auto-configuration classes:

| Module | Auto-Configuration Class |
|--------|-------------------------|
| JUnit Starter | `DbTesterJUnitAutoConfiguration` |
| Spock Starter | `DbTesterSpockAutoConfiguration` |

---

## Lifecycle Hooks

### JUnit Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│                    Test Execution                        │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  @BeforeAll                                              │
│    └─► Register DataSource                               │
│    └─► Set Configuration (optional)                      │
│                                                          │
│  For each @Test method:                                  │
│    │                                                     │
│    ├─► beforeEach() [DatabaseTestExtension]              │
│    │     └─► Find @Preparation                           │
│    │     └─► Load datasets                               │
│    │     └─► Execute operation                           │
│    │                                                     │
│    ├─► Test method execution                             │
│    │                                                     │
│    └─► afterEach() [DatabaseTestExtension]               │
│          └─► Find @Expectation                           │
│          └─► Load expected datasets                      │
│          └─► Compare with database                       │
│          └─► Report mismatches                           │
│                                                          │
│  @AfterAll                                               │
│    └─► Cleanup (optional)                                │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Spock Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│                  Specification Execution                 │
├─────────────────────────────────────────────────────────┤
│                                                          │
│  setupSpec()                                             │
│    └─► Initialize dbTesterRegistry                       │
│    └─► Register DataSource                               │
│    └─► Set dbTesterConfiguration (optional)              │
│                                                          │
│  For each feature method:                                │
│    │                                                     │
│    ├─► Interceptor.interceptFeatureExecution()           │
│    │     └─► Before: Execute @Preparation                │
│    │                                                     │
│    ├─► Feature method execution                          │
│    │                                                     │
│    └─► Interceptor.interceptFeatureExecution()           │
│          └─► After: Execute @Expectation                 │
│                                                          │
│  cleanupSpec()                                           │
│    └─► Cleanup (optional)                                │
│                                                          │
└─────────────────────────────────────────────────────────┘
```

### Lifecycle Executor Classes

| Framework | Preparation | Expectation |
|-----------|-------------|-------------|
| JUnit | `PreparationExecutor` | `ExpectationVerifier` |
| Spock | `SpockPreparationExecutor` | `SpockExpectationVerifier` |

### Error Handling

| Phase | Error Type | Behavior |
|-------|------------|----------|
| Preparation | `DatabaseOperationException` | Test fails before execution |
| Test | Any exception | Expectation still runs |
| Expectation | `ValidationException` | Test fails with comparison details |

---

## Related Specifications

- [Overview](01-OVERVIEW.md) - Framework introduction
- [Public API](03-PUBLIC-API.md) - Annotation details
- [Configuration](04-CONFIGURATION.md) - Configuration options
