# DB Tester Specification - Test Framework Integration

This document describes the integration with JUnit, Spock, and Kotest test frameworks.

## JUnit Integration

### Module

`db-tester-junit`

### Extension Class

**Location**: `io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension`

**Implemented Interfaces**:
- `BeforeEachCallback` - DataSet phase execution
- `AfterEachCallback` - ExpectedDataSet phase verification
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
    @DataSet
    @ExpectedDataSet
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
        @DataSet
        @ExpectedDataSet
        void testCreateUser() { }  // Uses parent's registry
    }

    @Nested
    class UpdateTests {
        @Test
        @DataSet
        @ExpectedDataSet
        void testUpdateUser() { }  // Uses parent's registry
    }
}
```

### Annotation Precedence

Method-level annotations override class-level:

```java
@DataSet(operation = Operation.CLEAN_INSERT)  // Class default
class UserRepositoryTest {

    @Test
    @DataSet(operation = Operation.INSERT)  // Overrides class
    void testWithInsert() { }

    @Test
    @DataSet  // Uses class default
    void testWithDefault() { }
}
```

## Spock Integration

### Module

`db-tester-spock`

### Extension Class

**Location**: `io.github.seijikohara.dbtester.spock.extension.DatabaseTestExtension`

**Type**: Annotation-driven extension (`IAnnotationDrivenExtension<DatabaseTest>`)

### Registration

The extension is activated by adding `@DatabaseTest` to the specification class:

```groovy
@DatabaseTest
class UserRepositorySpec extends Specification {

    @Shared
    DataSourceRegistry dbTesterRegistry

    def setupSpec() {
        dbTesterRegistry = new DataSourceRegistry()
        dbTesterRegistry.registerDefault(dataSource)
    }

    @DataSet
    @ExpectedDataSet
    def 'should create user'() {
        // Test implementation
    }
}
```

### Configuration Customization

Use a `@Shared` field named `dbTesterConfiguration`:

```groovy
@DatabaseTest
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

    @DataSet
    @ExpectedDataSet
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
@DataSet
def 'should create user with email'() {
    // Scenario name: "should create user with email"
}
```

### Data-Driven Tests

For parameterized tests with `where:` blocks, the iteration name is used:

```groovy
@DataSet
def 'should process #status order'() {
    expect:
    // Test implementation

    where:
    status << ['PENDING', 'COMPLETED']
}
```

Scenario names: `"should process PENDING order"`, `"should process COMPLETED order"`

## Kotest Integration

### Module

`db-tester-kotest`

### Extension Class

**Location**: `io.github.seijikohara.dbtester.kotest.extension.DatabaseTestExtension`

**Type**: `TestCaseExtension` - Intercepts test case execution for data set and expected data set phases.

### Registration

Register the extension in the `init` block. In Kotest 6, the `extensions()` method is final and cannot be overridden:

```kotlin
class UserRepositorySpec : AnnotationSpec() {

    private val registry = DataSourceRegistry()

    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    @BeforeAll
    fun setupSpec() {
        registry.registerDefault(dataSource)
    }

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // Test implementation
    }
}
```

### DataSource Registration

The extension accepts a `registryProvider` lambda for late-bound DataSource registration:

```kotlin
class UserRepositorySpec : AnnotationSpec() {

    companion object {
        private var sharedRegistry: DataSourceRegistry? = null
        private var sharedDataSource: DataSource? = null

        private fun initializeSharedResources() {
            sharedDataSource = createDataSource()
            sharedRegistry = DataSourceRegistry().apply {
                registerDefault(sharedDataSource!!)
            }
        }

        fun getDbTesterRegistry(): DataSourceRegistry {
            if (sharedRegistry == null) {
                initializeSharedResources()
            }
            return sharedRegistry!!
        }
    }

    init {
        extensions(DatabaseTestExtension(registryProvider = { getDbTesterRegistry() }))
    }

    @BeforeAll
    fun setupSpec() {
        if (sharedDataSource == null) {
            initializeSharedResources()
        }
    }
}
```

### Configuration Customization

Pass a custom `Configuration` to the extension:

```kotlin
class UserRepositorySpec : AnnotationSpec() {

    init {
        val conventions = ConventionSettings.standard()
            .withDataFormat(DataFormat.TSV)
        val config = Configuration.withConventions(conventions)

        extensions(DatabaseTestExtension(
            registryProvider = { registry },
            configuration = config
        ))
    }
}
```

### Test Method Naming

Use backtick method names for descriptive test names:

```kotlin
@Test
@DataSet
fun `should create user with email`() {
    // Scenario name: "should create user with email"
}
```

### AnnotationSpec Requirement

DB Tester requires `AnnotationSpec` style for Kotest integration because:
1. Annotations (`@DataSet`, `@ExpectedDataSet`) can be applied to test methods
2. Method resolution via reflection is reliable
3. Familiar JUnit-like structure for Java developers

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
    @DataSet
    @ExpectedDataSet
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
    @DataSet(sources = {
        @DataSetSource(dataSourceName = ""),          // Primary (default)
        @DataSetSource(dataSourceName = "secondary")  // Secondary
    })
    void testMultipleDatabases() { }
}
```

### Configuration Properties

Configure via `application.properties` or `application.yml`:

```properties
# Enable or disable DB Tester (default: true)
db-tester.enabled=true

# Auto-register DataSource beans (default: true)
db-tester.auto-register-data-sources=true

# Data format (CSV or TSV)
db-tester.convention.data-format=CSV

# ExpectedDataSet directory suffix
db-tester.convention.expected-data-set-suffix=/expected

# Scenario marker column name
db-tester.convention.scenario-marker=[Scenario]

# Table merge strategy (FIRST, LAST, UNION, UNION_ALL)
db-tester.convention.table-merge-strategy=UNION_ALL

# Default data set operation
db-tester.operation.data-set=CLEAN_INSERT

# Default expected data set operation (typically NONE for verification only)
db-tester.operation.expected-data-set=NONE
```

Property names use singular form (`convention`, `operation`) not plural.

### Spock Spring Boot Starter

**Module**: `db-tester-spock-spring-boot-starter`

**Extension**: `SpringBootDatabaseTestExtension` (Groovy)

**Type**: Annotation-driven extension (`IAnnotationDrivenExtension<SpringBootDatabaseTest>`)

```groovy
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositorySpec extends Specification {

    @DataSet
    @ExpectedDataSet
    def 'should create user'() {
        // DataSource automatically registered from Spring context
    }
}
```

### Kotest Spring Boot Starter

**Module**: `db-tester-kotest-spring-boot-starter`

**Extension**: `SpringBootDatabaseTestExtension` (Kotlin)

**Type**: `TestCaseExtension` with automatic Spring ApplicationContext integration.

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
| Kotest Starter | `DbTesterKotestAutoConfiguration` |

## Lifecycle Hooks

### JUnit Lifecycle

```mermaid
flowchart TD
    subgraph Test Execution
        BA["@BeforeAll"]
        BA --> BA1[Register DataSource]
        BA1 --> BA2[Set Configuration]

        subgraph each["For each Test method"]
            BE["beforeEach()"]
            BE --> BE1["Find DataSet"]
            BE1 --> BE2[Load datasets]
            BE2 --> BE3[Execute operation]
            BE3 --> TM[Test method execution]
            TM --> AE["afterEach()"]
            AE --> AE1["Find ExpectedDataSet"]
            AE1 --> AE2[Load expected datasets]
            AE2 --> AE3[Compare with database]
            AE3 --> AE4[Report mismatches]
        end

        BA2 --> each
        each --> AA["@AfterAll"]
        AA --> AA1[Cleanup]
    end
```

### Spock Lifecycle

```mermaid
flowchart TD
    subgraph Specification Execution
        SS["setupSpec()"]
        SS --> SS1[Initialize dbTesterRegistry]
        SS1 --> SS2[Register DataSource]
        SS2 --> SS3[Set dbTesterConfiguration]

        subgraph each["For each feature method"]
            INT1["Interceptor (Before)"]
            INT1 --> INT1A["Execute DataSet"]
            INT1A --> FM[Feature method execution]
            FM --> INT2["Interceptor (After)"]
            INT2 --> INT2A["Execute ExpectedDataSet"]
        end

        SS3 --> each
        each --> CS["cleanupSpec()"]
        CS --> CS1[Cleanup]
    end
```

### Kotest Lifecycle

```mermaid
flowchart TD
    subgraph Specification Execution
        INIT["init block"]
        INIT --> INIT1[Register Extension]

        BA["@BeforeAll"]
        BA --> BA1[Initialize Registry]
        BA1 --> BA2[Register DataSource]

        subgraph each["For each @Test method"]
            INT["intercept()"]
            INT --> INT1["Find DataSet"]
            INT1 --> INT2[Load datasets]
            INT2 --> INT3[Execute operation]
            INT3 --> TM[Test method execution]
            TM --> INT4["Find ExpectedDataSet"]
            INT4 --> INT5[Load expected datasets]
            INT5 --> INT6[Compare with database]
            INT6 --> INT7[Report mismatches]
        end

        INIT1 --> BA
        BA2 --> each
        each --> AA["@AfterAll"]
        AA --> AA1[Cleanup]
    end
```

### Lifecycle Executor Classes

| Framework | DataSet | ExpectedDataSet |
|-----------|-------------|-------------|
| JUnit | `DataSetExecutor` | `ExpectedDataSetVerifier` |
| Spock | `SpockDataSetExecutor` | `SpockExpectedDataSetVerifier` |
| Kotest | `KotestDataSetExecutor` | `KotestExpectedDataSetVerifier` |

### Error Handling

| Phase | Error Type | Behavior |
|-------|------------|----------|
| DataSet | `DatabaseOperationException` | Test fails before execution |
| Test | Any exception | ExpectedDataSet still runs |
| ExpectedDataSet | `ValidationException` | Test fails with comparison details |

## Related Specifications

- [Overview](01-overview) - Framework purpose and key concepts
- [Public API](03-public-api) - Annotation details
- [Configuration](04-configuration) - Configuration options
- [SPI](08-spi) - Service Provider Interface extension points
- [Error Handling](09-error-handling) - Lifecycle error handling
