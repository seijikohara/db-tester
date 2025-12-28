---
paths: "**/*Test.kt,**/*Spec.kt"
---

# Kotlin Testing Code Style Rules (Kotest Framework)

**All rules in this document are mandatory unless explicitly marked as Optional.**

**Reference**: [Kotest Documentation](https://kotest.io/docs/framework/framework.html)

---

## Testing Framework

### Required Dependencies

- **Kotest 6.x**: Primary testing framework for Kotlin
- **Kotest Extensions**: Additional matchers and utilities
- **MockK** (Optional): Kotlin-first mocking library
- **Testcontainers** (Optional): For database integration tests

### Framework Imports

```kotlin
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.core.extensions.Extension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldContainExactly
```

---

## Specification Structure

### Class Requirements

1. **Extend AnnotationSpec**: For db-tester integration, use `AnnotationSpec` (required for annotation-based testing)
2. **KDoc**: Class-level KDoc describing the specification purpose
3. **Naming**: Use `Test` suffix (e.g., `MinimalExampleTest`, `DatabaseTestExtensionTest`)

### Example Structure

```kotlin
/**
 * Demonstrates the minimal convention-based database testing approach with Kotest.
 *
 * This specification illustrates:
 * - CSV file resolution based on test class and method names
 * - Method-level [Preparation] and [Expectation] annotations
 */
class MinimalExampleSpec : AnnotationSpec() {

    private val registry = DataSourceRegistry()
    private lateinit var dataSource: DataSource

    // Kotest 6: Register extensions in init block (extensions() is final)
    init {
        extensions(DatabaseTestExtension(registryProvider = { registry }))
    }

    @BeforeAll
    fun setupSpec() {
        dataSource = createDataSource()
        registry.registerDefault(dataSource)
    }

    @Test
    @Preparation
    @Expectation
    fun `should load and verify product data`() {
        // Test implementation
    }

    companion object {
        private fun createDataSource(): DataSource = JdbcDataSource().apply {
            setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
            user = "sa"
            password = ""
        }
    }
}
```

> **Note**: In Kotest 6, the `extensions()` method is final and cannot be overridden.
> Use the `init { extensions(...) }` pattern instead.

---

## AnnotationSpec Style

### Why AnnotationSpec

For db-tester integration, `AnnotationSpec` is required because:
1. Annotations (`@Preparation`, `@Expectation`) can be applied to test methods
2. Method resolution via reflection is reliable
3. Familiar JUnit-like structure for Java developers

### Available Annotations

| Annotation | Purpose |
|------------|---------|
| `@Test` | Mark a function as a test |
| `@BeforeAll` | Execute once before all tests |
| `@AfterAll` | Execute once after all tests |
| `@BeforeEach` | Execute before each test |
| `@AfterEach` | Execute after each test |
| `@Ignore` | Skip this test |

### Test Method Structure

```kotlin
@Test
@Preparation
@Expectation
fun `should insert and verify user data`() {
    // given
    val userId = 1

    // when
    sql.execute("INSERT INTO users (id, name) VALUES (?, ?)", userId, "John")

    // then - verified by @Expectation
}
```

---

## Test Method Naming

### Backtick Method Names

Use backticks for descriptive test method names.

```kotlin
// Correct - descriptive natural language
@Test
fun `should load and verify product data`() { }

@Test
fun `should create active user when valid input provided`() { }

@Test
fun `should throw exception when directory does not exist`() { }

// Incorrect - camelCase
@Test
fun shouldLoadAndVerifyProductData() { }
```

### Naming Guidelines

| Pattern | Example |
|---------|---------|
| Normal behavior | `should return value when valid input provided` |
| Edge case | `should handle empty list` |
| Error scenario | `should throw exception when input is null` |
| State transition | `should update status from pending to active` |

**Conventions**:
- Start with `should` for behavior descriptions
- Use natural language that reads as a sentence
- Avoid technical jargon; favor business language
- Method names map directly to scenario names in CSV files

---

## Assertions

### Kotest Matchers

Use Kotest matchers for assertions.

```kotlin
// Equality
result shouldBe expected
result shouldNotBe unexpected

// Nullability
value shouldNotBe null
nullableValue.shouldBeNull()

// Collections
list shouldHaveSize 3
list shouldContain element
list shouldContainExactly listOf("a", "b", "c")
list.shouldBeEmpty()

// Strings
string shouldStartWith "prefix"
string shouldContain "substring"
string.shouldBeBlank()

// Numbers
number shouldBeGreaterThan 0
number.shouldBeInRange(1..10)

// Boolean
condition.shouldBeTrue()
condition.shouldBeFalse()
```

### Exception Testing

```kotlin
@Test
fun `should throw exception when input is invalid`() {
    shouldThrow<IllegalArgumentException> {
        processInput(null)
    }.message shouldContain "must not be null"
}

@Test
fun `should not throw any exception`() {
    shouldNotThrowAny {
        safeOperation()
    }
}
```

### Soft Assertions

Use `assertSoftly` for multiple related assertions.

```kotlin
@Test
fun `should return user with correct properties`() {
    val user = createUser("John", "john@example.com")

    assertSoftly(user) {
        name shouldBe "John"
        email shouldBe "john@example.com"
        isActive.shouldBeTrue()
    }
}
```

---

## Data-Driven Testing

### Property-Based Testing

Use Kotest's property testing for parameterized tests.

```kotlin
@Test
fun `should calculate maximum correctly`() {
    forAll(
        row(1, 3, 3),
        row(7, 4, 7),
        row(0, 0, 0)
    ) { a, b, expected ->
        maxOf(a, b) shouldBe expected
    }
}
```

### With Data Class

```kotlin
data class TestCase(val input: String, val expected: Int)

@Test
fun `should parse numbers correctly`() {
    listOf(
        TestCase("1", 1),
        TestCase("42", 42),
        TestCase("-5", -5)
    ).forEach { (input, expected) ->
        input.toInt() shouldBe expected
    }
}
```

---

## Lifecycle Callbacks

### Available Callbacks

```kotlin
@BeforeAll
fun setupSpec() { }    // Once before all tests

@BeforeEach
fun setup() { }        // Before each test

@AfterEach
fun cleanup() { }      // After each test

@AfterAll
fun cleanupSpec() { }  // Once after all tests
```

### Example

```kotlin
class DatabaseTest : AnnotationSpec() {

    private lateinit var sql: Sql

    companion object {
        private var dataSource: DataSource? = null
    }

    @BeforeAll
    fun setupSpec() {
        dataSource = createDataSource()
        sql = Sql(dataSource!!)
        executeScript("ddl/schema.sql")
    }

    @BeforeEach
    fun setup() {
        sql.execute("DELETE FROM test_table")
    }

    @AfterAll
    fun cleanupSpec() {
        sql.close()
    }
}
```

---

## Extensions

### TestCaseExtension

Implement `TestCaseExtension` for test interception.

```kotlin
class DatabaseTestExtension(
    private val registryProvider: () -> DataSourceRegistry
) : TestCaseExtension {

    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult
    ): TestResult {
        val preparation = findPreparation(testCase)
        val expectation = findExpectation(testCase)

        // Preparation phase
        preparation?.let { prepareDatabase(it, testCase) }

        // Execute test
        val result = execute(testCase)

        // Expectation phase
        if (result is TestResult.Success) {
            expectation?.let { verifyDatabase(it, testCase) }
        }

        return result
    }
}
```

### Registering Extensions

```kotlin
class MyTest : AnnotationSpec() {

    private val registry = DataSourceRegistry()

    // Kotest 6: Register extensions in init block
    init {
        extensions(
            DatabaseTestExtension(registryProvider = { registry }),
            LoggingExtension()
        )
    }
}
```

### Method Resolution in AnnotationSpec

To resolve the test method from a `TestCase`:

```kotlin
private fun resolveMethod(testCase: TestCase): Method? {
    val specClass = testCase.spec::class.java
    val methodName = testCase.name.testName

    return specClass.declaredMethods.find { method ->
        method.name == methodName || method.name == sanitizeMethodName(methodName)
    }
}

private fun sanitizeMethodName(name: String): String {
    // Handle backtick method names
    return name.replace("`", "").replace(" ", "_")
}
```

---

## Mocking

### MockK Integration

Use MockK for Kotlin-first mocking.

```kotlin
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

@Test
fun `should call service with correct parameters`() {
    // given
    val service = mockk<DataService>()
    every { service.getData(any()) } returns listOf("data")

    val processor = DataProcessor(service)

    // when
    val result = processor.process("input")

    // then
    result shouldHaveSize 1
    verify { service.getData("input") }
}
```

### Relaxed Mocks

```kotlin
val service = mockk<DataService>(relaxed = true)
```

### Capturing Arguments

```kotlin
val slot = slot<String>()
every { service.process(capture(slot)) } returns "result"

processor.execute("test")

slot.captured shouldBe "test"
```

---

## Documentation

### KDoc Requirements

**All test code requires KDoc**:

1. **Test class**: Purpose and overview
2. **Test methods**: What is being tested
3. **Helper methods**: `@param`, `@return`, `@throws` as needed

### Test Class KDoc

```kotlin
/**
 * Demonstrates scenario-based testing with CSV row filtering.
 *
 * This specification illustrates:
 * - Sharing a single CSV file across multiple test methods
 * - Each test loading only rows matching its method name
 */
class ScenarioFilteringTest : AnnotationSpec() {
```

### Test Method KDoc

```kotlin
/**
 * Demonstrates the minimal convention-based test.
 *
 * Test flow:
 * - Preparation: Loads initial data from CSV
 * - Execution: Inserts new record
 * - Expectation: Verifies final state
 */
@Test
@Preparation
@Expectation
fun `should load and verify product data`() {
```

---

## Testcontainers Integration

### Setup Pattern

Use Kotest's container extension for lifecycle management.

```kotlin
import org.testcontainers.containers.PostgreSQLContainer
import io.kotest.extensions.testcontainers.perSpec

class PostgreSQLIntegrationTest : AnnotationSpec() {

    companion object {
        private val postgres = PostgreSQLContainer("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")

        private var sharedRegistry: DataSourceRegistry? = null
        private var sharedDataSource: DataSource? = null
    }

    private lateinit var sql: Sql

    init {
        // Register container extension
        register(postgres.perSpec())
    }

    fun getDbTesterRegistry(): DataSourceRegistry {
        if (sharedRegistry == null) {
            initializeRegistry()
        }
        return sharedRegistry!!
    }

    private fun initializeRegistry() {
        sharedDataSource = createDataSource(postgres)
        sharedRegistry = DataSourceRegistry().apply {
            registerDefault(sharedDataSource!!)
        }
    }

    override fun extensions(): List<Extension> =
        listOf(DatabaseTestExtension(::getDbTesterRegistry))

    @BeforeAll
    fun setupSpec() {
        if (sharedDataSource == null) {
            initializeRegistry()
        }
        sql = Sql(sharedDataSource!!)
        executeScript("ddl/schema.sql")
    }

    @AfterAll
    fun cleanupSpec() {
        sql.close()
    }
}
```

### Container Configuration

```kotlin
// PostgreSQL
val postgres = PostgreSQLContainer("postgres:latest")
    .withDatabaseName("testdb")
    .withUsername("testuser")
    .withPassword("testpass")

// MySQL
val mysql = MySQLContainer("mysql:latest")
    .withDatabaseName("testdb")
    .withUsername("testuser")
    .withPassword("testpass")

// SQL Server
val mssql = MSSQLServerContainer("mcr.microsoft.com/mssql/server:latest")
    .acceptLicense()
    .withPassword("StrongPassword123!")

// Oracle
val oracle = OracleContainer("gvenzl/oracle-free:latest")
    .withDatabaseName("testdb")
    .withUsername("testuser")
    .withPassword("testpass")
```

---

## Complete Example

```kotlin
package example.feature

import io.github.seijikohara.dbtester.api.annotation.Expectation
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.DataSourceRegistry
import io.github.seijikohara.dbtester.kotest.DatabaseTestExtension
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.matchers.shouldNotBe
import org.h2.jdbcx.JdbcDataSource
import javax.sql.DataSource

/**
 * Demonstrates the minimal convention-based database testing approach with Kotest.
 *
 * This specification illustrates:
 * - CSV file resolution based on test class and method names
 * - Method-level [Preparation] and [Expectation] annotations
 */
class MinimalExampleTest : AnnotationSpec() {

    private lateinit var dataSource: DataSource
    private lateinit var sql: Sql

    companion object {
        private var sharedRegistry: DataSourceRegistry? = null
        private var sharedDataSource: DataSource? = null

        private fun initializeSharedResources() {
            sharedDataSource = JdbcDataSource().apply {
                setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
                setUser("sa")
                setPassword("")
            }
            sharedRegistry = DataSourceRegistry().apply {
                registerDefault(sharedDataSource!!)
            }
        }

        private fun getDbTesterRegistry(): DataSourceRegistry {
            if (sharedRegistry == null) {
                initializeSharedResources()
            }
            return sharedRegistry!!
        }
    }

    // Kotest 6: Register extensions in init block (extensions() is final)
    init {
        extensions(DatabaseTestExtension(registryProvider = { getDbTesterRegistry() }))
    }

    /**
     * Sets up H2 in-memory database connection and schema.
     */
    @BeforeAll
    fun setupSpec() {
        if (sharedDataSource == null) {
            initializeSharedResources()
        }
        dataSource = sharedDataSource!!
        sql = Sql(dataSource)
        executeScript("ddl/schema.sql")
    }

    /**
     * Cleans up database resources.
     */
    @AfterAll
    fun cleanupSpec() {
        sql.close()
    }

    /**
     * Demonstrates the minimal convention-based test.
     */
    @Test
    @Preparation
    @Expectation
    fun `should load and verify product data`() {
        // when
        sql.execute("""
            INSERT INTO products (id, name, price)
            VALUES (3, 'Keyboard', 79.99)
        """)

        // then - expectation phase verifies database state
        sql.firstRow("SELECT * FROM products WHERE id = 3") shouldNotBe null
    }

    /**
     * Executes a SQL script from classpath.
     *
     * @param scriptPath the classpath resource path
     */
    private fun executeScript(scriptPath: String) {
        val resource = javaClass.classLoader.getResource(scriptPath)
            ?: throw IllegalStateException("Script not found: $scriptPath")

        resource.readText()
            .split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { sql.execute(it) }
    }
}
```
