---
paths: "**/*Spec.groovy"
---

# Groovy Testing Code Style Rules (Spock Framework)

**All rules in this document are mandatory unless explicitly marked as Optional.**

**Reference**: [Spock Framework Documentation](https://spockframework.org/spock/docs/2.3/all_in_one.html)

---

## Table of Contents

- [Testing Framework](#testing-framework)
- [Specification Structure](#specification-structure)
- [Feature Method Naming](#feature-method-naming)
- [Block Structure](#block-structure)
- [Conditions and Assertions](#conditions-and-assertions)
- [Data-Driven Testing](#data-driven-testing)
- [Mocking and Stubbing](#mocking-and-stubbing)
- [Lifecycle Methods](#lifecycle-methods)
- [Field Management](#field-management)
- [Extensions and Annotations](#extensions-and-annotations)
- [Documentation](#documentation)
- [Testcontainers Integration](#testcontainers-integration)

---

## Testing Framework

### Required Dependencies

- **Spock Framework 2.x**: Primary testing framework for Groovy
- **Groovy 5**: Language runtime
- **Testcontainers** (Optional): For database integration tests

### Framework Imports

```groovy
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
```

---

## Specification Structure

### Class Requirements

1. **Extend `Specification`**: All test classes must extend `spock.lang.Specification`
2. **Javadoc**: Class-level Javadoc describing the specification purpose
3. **Naming**: Use `Spec` suffix (e.g., `MinimalExampleSpec`, `DatabaseTestInterceptorSpec`)

### Example Structure

```groovy
/**
 * Demonstrates the minimal convention-based database testing approach with Spock.
 *
 * <p>This specification illustrates:
 * <ul>
 *   <li>CSV file resolution based on specification class and feature method names
 *   <li>Method-level {@code @Preparation} and {@code @Expectation} annotations
 * </ul>
 */
class MinimalExampleSpec extends Specification {

    /** Shared DataSource for all feature methods. */
    @Shared
    DataSource dataSource

    /** Groovy SQL helper for database operations. */
    @Shared
    Sql sql

    def setupSpec() {
        // One-time setup
    }

    def cleanupSpec() {
        sql?.close()
    }

    def 'should load and verify product data'() {
        // Feature method
    }

    // Helper methods
    private void executeScript(String scriptPath) {
        // implementation
    }
}
```

---

## Feature Method Naming

### String Literal Names

Use descriptive string literals for feature method names instead of camelCase.

```groovy
// Correct - descriptive natural language
def 'should load and verify product data'() { }
def 'should create active user'() { }
def 'should throw exception when directory does not exist'() { }

// Incorrect - camelCase
def shouldLoadAndVerifyProductData() { }
```

### Naming Guidelines

| Pattern | Example |
|---------|---------|
| Normal behavior | `'should return value when valid input provided'` |
| Edge case | `'should handle empty list'` |
| Error scenario | `'should throw exception when input is null'` |
| State transition | `'should update status from pending to active'` |

**Conventions**:
- Start with `'should'` for behavior descriptions
- Use natural language that reads as a sentence
- Avoid technical jargon; favor business language
- Feature names map directly to scenario names in CSV files

---

## Block Structure

### Available Blocks

| Block | Purpose | Requirements |
|-------|---------|--------------|
| `given:` | Setup/fixture | Optional; use for explicit setup |
| `setup:` | Alias for `given:` | Prefer `given:` for readability |
| `when:` | Stimulus/action | Must pair with `then:` |
| `then:` | Response/verification | Conditions and interactions only |
| `expect:` | Combined stimulus+response | For purely functional methods |
| `and:` | Continuation of previous block | Improves readability |
| `cleanup:` | Resource cleanup | May only precede `where:` |
| `where:` | Data iteration | Always last block |

### Block Labels with Descriptions

Add descriptive text to blocks for documentation.

```groovy
def 'should load and verify product data'() {
    given: 'a database with initial products'
    sql.execute '''
        INSERT INTO products (id, name) VALUES (1, 'Mouse')
    '''

    when: 'inserting a new product'
    sql.execute '''
        INSERT INTO products (id, name) VALUES (2, 'Keyboard')
    '''

    then: 'database contains both products'
    sql.rows('SELECT * FROM products').size() == 2
}
```

### Given-When-Then Pattern

Use for behavior verification with side effects.

```groovy
def 'should execute basic database operations'() {
    given: 'initial data is loaded'
    // setup code

    when: 'performing the operation'
    def result = target.process(input)

    then: 'expected state is achieved'
    result.size() == expectedSize
}
```

### Expect Pattern

Use for purely functional methods without side effects.

```groovy
def 'should calculate maximum of two numbers'() {
    expect: 'maximum is returned'
    Math.max(a, b) == c

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
}
```

---

## Conditions and Assertions

### Implicit Assertions

Conditions in `then:` and `expect:` blocks are automatically assertions.

```groovy
then: 'result is correct'
result.size() == 3
result.first().name == 'Test'
result.every { it.active }
```

### Grouped Assertions with `verifyAll`

Use for multiple related assertions.

```groovy
then: 'all properties are correct'
verifyAll(result) {
    size() == 3
    first().name == 'Test'
    last().status == 'active'
}
```

### Object Assertions with `with`

Use for assertions on a single object.

```groovy
then: 'user has correct properties'
with(user) {
    name == 'John'
    email == 'john@example.com'
    active == true
}
```

### Exception Testing

```groovy
def 'should throw exception when input is invalid'() {
    when: 'processing invalid input'
    target.process(null)

    then: 'exception is thrown'
    def e = thrown(IllegalArgumentException)
    e.message.contains('must not be null')
}
```

### No Exception Verification

```groovy
def 'should complete without error'() {
    when: 'performing safe operation'
    target.safeOperation()

    then: 'no exception is thrown'
    noExceptionThrown()
}
```

### Condition Limit

Limit to 1-5 conditions per feature method. Split larger tests into multiple features.

---

## Data-Driven Testing

### Data Tables

Use for fixed sets of test data.

```groovy
def 'should calculate maximum of two numbers'() {
    expect: 'maximum is returned'
    Math.max(a, b) == c

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
    0 | 0 || 0
}
```

**Conventions**:
- Use `||` to separate inputs from expected outputs
- Align columns for readability
- Header row defines variable names

### Data Pipes

Use for dynamic or external data sources.

```groovy
where:
a << [1, 7, 0]
b << [3, 4, 0]
c << [3, 7, 0]
```

### Multi-Variable Data Pipes

```groovy
where:
[a, b, c] << sql.rows('SELECT a, b, c FROM test_data')
[name, _, age] << loadTestData()  // ignore column with _
```

### Unrolling

Iterations unroll by default in Spock 2.x.

```groovy
// Feature name with placeholders
def 'maximum of #a and #b is #c'() {
    expect:
    Math.max(a, b) == c

    where:
    a | b || c
    1 | 3 || 3
    7 | 4 || 7
}
// Reports as: "maximum of 1 and 3 is 3", "maximum of 7 and 4 is 7"
```

---

## Mocking and Stubbing

### Mock Creation

```groovy
// Type-inferred (preferred)
DataSource mockDataSource = Mock()

// Explicit type
def mockDataSource = Mock(DataSource)
```

### Stubbing Return Values

```groovy
given: 'mock returns expected value'
mockService.getData() >> 'result'
mockService.compute(_) >> { args -> args[0] * 2 }
mockService.getValues() >>> ['first', 'second', 'third']
```

### Interaction Verification

```groovy
then: 'service was called correctly'
1 * mockService.process('input')     // exactly once
0 * mockService.forbidden(_)         // never called
(1..3) * mockService.retry(_)        // 1-3 times
_ * mockAuditing._                   // any calls allowed
```

### Combined Stubbing and Verification

```groovy
then: 'service called and returned value'
1 * mockService.process('input') >> 'result'
```

### Strict Mocking

```groovy
then: 'only expected calls occur'
1 * mockService.expectedCall()
0 * _  // no other interactions
```

---

## Lifecycle Methods

### Available Methods

```groovy
def setupSpec() { }    // Once before all features
def setup() { }        // Before each feature
def cleanup() { }      // After each feature
def cleanupSpec() { }  // Once after all features
```

### Execution Order with Inheritance

1. Parent `setupSpec()`
2. Child `setupSpec()`
3. Parent `setup()`
4. Child `setup()`
5. Feature execution
6. Child `cleanup()`
7. Parent `cleanup()`
8. Child `cleanupSpec()`
9. Parent `cleanupSpec()`

### Example

```groovy
def setupSpec() {
    dataSource = createDataSource()
    sql = new Sql(dataSource)
    executeScript('ddl/schema.sql')
}

def setup() {
    sql.execute('DELETE FROM test_table')
}

def cleanup() {
    // Optional per-test cleanup
}

def cleanupSpec() {
    sql?.close()
}
```

---

## Field Management

### Instance Fields

Initialize at declaration for isolation between features.

```groovy
class MySpec extends Specification {
    List items = []  // Fresh list for each feature
}
```

### @Shared Fields

Use for expensive resources shared across features.

```groovy
@Shared
DataSource dataSource

@Shared
Sql sql
```

**Restrictions**: `setupSpec()` and `cleanupSpec()` may only access `@Shared` or `static` fields.

### Static Fields

Use only for constants or shared state that requires static initialization.

```groovy
static DataSourceRegistry sharedRegistry
static DataSource sharedDataSource
```

### Property Accessors for Framework Integration

Use Groovy property accessors to provide configuration to frameworks.

```groovy
/** Static registry shared across all tests. */
static DataSourceRegistry sharedRegistry

/**
 * Gets the DataSourceRegistry (Groovy property accessor).
 * @return the registry
 */
DataSourceRegistry getDbTesterRegistry() {
    if (sharedRegistry == null) {
        initializeSharedResources()
    }
    return sharedRegistry
}
```

---

## Extensions and Annotations

### Built-In Annotations

| Annotation | Purpose |
|------------|---------|
| `@Shared` | Share field across features |
| `@Unroll` | Expand iterations in reporting (default in 2.x) |
| `@Rollup` | Aggregate iterations in reporting |
| `@Timeout(value)` | Fail if feature exceeds duration |
| `@Ignore` | Skip feature or specification |
| `@IgnoreRest` | Run only this feature |
| `@IgnoreIf({ condition })` | Conditional skip |
| `@Requires({ condition })` | Conditional execution |
| `@PendingFeature` | Document incomplete features |
| `@Stepwise` | Enforce sequential execution |
| `@Retry(count = n)` | Automatic retry on failure |

### Metadata Annotations

```groovy
@Title('Database Integration Tests')
@Narrative('Tests database operations with real connections')
@See('https://example.com/docs')
@Issue('PROJ-123')
```

---

## Documentation

### Javadoc Requirements

**All test code requires Javadoc** (enforced by DocLint):

1. **Specification class**: Purpose and overview
2. **Feature methods**: What is being tested
3. **Helper methods**: `@param`, `@return`, `@throws` as needed
4. **Fields**: Single-line Javadoc for non-obvious fields

### Specification Javadoc

```groovy
/**
 * Demonstrates scenario-based testing with CSV row filtering.
 *
 * <p>This specification illustrates:
 * <ul>
 *   <li>Sharing a single CSV file across multiple feature methods
 *   <li>Each test loading only rows matching its feature method name
 * </ul>
 */
class ScenarioFilteringSpec extends Specification {
```

### Feature Method Javadoc

```groovy
/**
 * Demonstrates the minimal convention-based test.
 *
 * <p>Test flow:
 * <ul>
 *   <li>Preparation: Loads initial data from CSV
 *   <li>Execution: Inserts new record
 *   <li>Expectation: Verifies final state
 * </ul>
 */
def 'should load and verify product data'() {
```

### Helper Method Javadoc

```groovy
/**
 * Executes a SQL script from classpath.
 *
 * @param scriptPath the classpath resource path
 * @throws IllegalStateException if script not found
 */
private void executeScript(String scriptPath) {
```

---

## Testcontainers Integration

### Setup Pattern

Use `@Testcontainers` annotation with `@Shared` for container lifecycle management.

```groovy
import org.testcontainers.spock.Testcontainers

@Testcontainers
class PostgreSQLIntegrationSpec extends Specification {

    // @Shared prevents container restart between features
    @Shared
    PostgreSQLContainer postgres = new PostgreSQLContainer('postgres:latest')
            .withDatabaseName('testdb')
            .withUsername('testuser')
            .withPassword('testpass')

    static DataSourceRegistry sharedRegistry
    static DataSource sharedDataSource

    @Shared
    Sql sql

    DataSourceRegistry getDbTesterRegistry() {
        if (sharedRegistry == null) {
            initializeRegistry()
        }
        return sharedRegistry
    }

    private void initializeRegistry() {
        sharedDataSource = createDataSource(postgres)
        sharedRegistry = new DataSourceRegistry()
        sharedRegistry.registerDefault(sharedDataSource)
    }

    def setupSpec() {
        if (sharedDataSource == null) {
            initializeRegistry()
        }
        sql = new Sql(sharedDataSource)
        executeScript('ddl/schema.sql')
    }

    def cleanupSpec() {
        sql?.close()
    }
}
```

### Container Configuration

```groovy
// PostgreSQL
PostgreSQLContainer postgres = new PostgreSQLContainer('postgres:latest')
        .withDatabaseName('testdb')
        .withUsername('testuser')
        .withPassword('testpass')

// MySQL
MySQLContainer mysql = new MySQLContainer('mysql:latest')
        .withDatabaseName('testdb')
        .withUsername('testuser')
        .withPassword('testpass')

// SQL Server
MSSQLServerContainer mssql = new MSSQLServerContainer('mcr.microsoft.com/mssql/server:latest')
        .acceptLicense()
        .withPassword('StrongPassword123!')

// Oracle
OracleContainer oracle = new OracleContainer('gvenzl/oracle-free:latest')
        .withDatabaseName('testdb')
        .withUsername('testuser')
        .withPassword('testpass')
```

### Important Notes

- Do **NOT** use `static` keyword with `@Shared` containers (Spock manages lifecycle)
- Use `@Shared` to prevent container restart between feature methods
- Initialize DataSource lazily in the property accessor to ensure container is started

---

## Complete Example

```groovy
package example.feature

import groovy.sql.Sql
import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import javax.sql.DataSource
import org.h2.jdbcx.JdbcDataSource
import spock.lang.Shared
import spock.lang.Specification

/**
 * Demonstrates the minimal convention-based database testing approach with Spock.
 *
 * <p>This specification illustrates:
 * <ul>
 *   <li>CSV file resolution based on specification class and feature method names
 *   <li>Method-level {@code @Preparation} and {@code @Expectation} annotations
 * </ul>
 */
class MinimalExampleSpec extends Specification {

    /** Shared DataSource for all feature methods. */
    @Shared
    DataSource dataSource

    /** Groovy SQL helper for database operations. */
    @Shared
    Sql sql

    /** Static registry shared across all tests. */
    static DataSourceRegistry sharedRegistry
    static DataSource sharedDataSource

    /**
     * Gets the DataSourceRegistry (Groovy property accessor).
     * @return the registry
     */
    DataSourceRegistry getDbTesterRegistry() {
        if (sharedRegistry == null) {
            initializeSharedResources()
        }
        return sharedRegistry
    }

    /**
     * Initializes shared resources.
     */
    private static void initializeSharedResources() {
        sharedDataSource = new JdbcDataSource().tap {
            setURL('jdbc:h2:mem:test;DB_CLOSE_DELAY=-1')
            setUser('sa')
            setPassword('')
        }
        sharedRegistry = new DataSourceRegistry()
        sharedRegistry.registerDefault(sharedDataSource)
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    def setupSpec() {
        if (sharedDataSource == null) {
            initializeSharedResources()
        }
        dataSource = sharedDataSource
        sql = new Sql(dataSource)
        executeScript('ddl/schema.sql')
    }

    /**
     * Cleans up database resources.
     */
    def cleanupSpec() {
        sql?.close()
    }

    /**
     * Demonstrates the minimal convention-based test.
     */
    @Preparation
    @Expectation
    def 'should load and verify product data'() {
        when: 'inserting a new product'
        sql.execute '''
            INSERT INTO products (id, name, price)
            VALUES (3, 'Keyboard', 79.99)
        '''

        then: 'expectation phase verifies database state'
        noExceptionThrown()
    }

    /**
     * Executes a SQL script from classpath.
     *
     * @param scriptPath the classpath resource path
     */
    private void executeScript(String scriptPath) {
        def resource = getClass().classLoader.getResource(scriptPath)
        if (resource == null) {
            throw new IllegalStateException("Script not found: $scriptPath")
        }

        resource.text
                .split(';')
                .collect { it.trim() }
                .findAll { !it.empty }
                .each { sql.execute(it) }
    }
}
```
