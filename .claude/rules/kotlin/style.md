---
paths: "**/*.kt"
---

# Kotlin Code Style Rules

**All rules in this document are mandatory unless explicitly marked as Optional.**

**Reference**: [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

---

## Quick Reference

| Category | Rule |
|----------|------|
| Immutability | Use `val` by default; `var` only when mutation is required |
| Null Safety | Avoid nullable types; use `?.`, `?:`, `!!` appropriately |
| Type Inference | Omit types when inference is clear; explicit types for public API |
| Collections | Use immutable collections by default; `listOf`, `setOf`, `mapOf` |
| Functions | **Always use expression body**; chain expressions with scope functions |
| Scope Functions | Use `apply`, `also`, `let`, `run`, `with` idiomatically |
| KDoc | Required for all public classes and functions |

---

## Code Organization

### File Structure

**Source file layout**:

1. Package statement
2. Import statements
3. Top-level declarations

**Single-class files**: Name the file after the class (e.g., `DatabaseTestExtension.kt`).

**Multiple declarations**: Use a descriptive name for the file content.

```kotlin
// File: DatabaseExtensions.kt
package io.github.seijikohara.dbtester.kotest

fun DataSource.toRegistry(): DataSourceRegistry = ...
fun TestCase.findAnnotation<T>(): T? = ...
```

### Class Structure

Members are ordered as follows:

1. Companion object
2. Properties (primary constructor properties included)
3. Init blocks
4. Secondary constructors
5. Functions (public → internal → private)
6. Nested classes and objects

**Logger placement**: Logger belongs in companion object.

```kotlin
class DatabaseTestExtension : TestCaseExtension {

    companion object {
        private val logger = LoggerFactory.getLogger(DatabaseTestExtension::class.java)
    }

    // Properties
    private val registry: DataSourceRegistry = DataSourceRegistry()

    // Functions
    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase) -> TestResult
    ): TestResult {
        // implementation
    }
}
```

### Import Statements

**Requirements**:
- Import specific classes; star imports prohibited
- Organize in groups: stdlib, third-party, project
- Sort alphabetically within groups

```kotlin
// Standard library
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

// Third-party
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase

// Project
import io.github.seijikohara.dbtester.api.annotation.Preparation
import io.github.seijikohara.dbtester.api.config.Configuration
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes/Objects | PascalCase | `DatabaseTestExtension` |
| Functions/Properties | camelCase | `getTestContext` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `io.github.seijikohara.dbtester.kotest` |
| Type parameters | Single uppercase letter or PascalCase | `T`, `TestType` |

**Backing properties**: Use underscore prefix.

```kotlin
private val _items = mutableListOf<String>()
val items: List<String> get() = _items
```

---

## Language Fundamentals

### Immutability

**Prefer `val` over `var`**: Declare all properties and variables as `val` unless mutation is required.

```kotlin
// Correct
val config = Configuration.builder().build()
val tables = dataSet.tables

// Incorrect (unless mutation is necessary)
var config = Configuration.builder().build()
```

**Immutable collections**: Use `listOf`, `setOf`, `mapOf` for immutable collections.

```kotlin
// Correct
val tables: List<String> = listOf("users", "orders")

// For mutable needs
val mutableTables = mutableListOf<String>()
```

### Null Safety

**Avoid nullable types**: Design APIs to minimize nullable types.

**Safe call operator `?.`**: Use for optional operations.

```kotlin
val name = user?.profile?.name
```

**Elvis operator `?:`**: Use for default values.

```kotlin
val name = user?.name ?: "Unknown"
```

**Not-null assertion `!!`**: Use only when null is impossible and not caught by compiler.

```kotlin
// Acceptable: After explicit null check
requireNotNull(config) { "Configuration must not be null" }
val timeout = config!!.timeout
```

**`require` and `check`**: Use for preconditions and state validation.

```kotlin
fun process(input: String) {
    require(input.isNotBlank()) { "Input must not be blank" }
    // ...
}
```

### Type Inference

**Omit types when clear from context**:

```kotlin
// Correct
val dataSource = createDataSource()
val tables = listOf("users", "orders")

// Explicit type required for public API
fun getConfiguration(): Configuration = ...
```

**Explicit types for public API**: Return types on public functions.

```kotlin
// Public API - explicit return type
fun createExtension(): DatabaseTestExtension {
    return DatabaseTestExtension()
}

// Private - inference acceptable
private fun buildConfig() = Configuration.builder().build()
```

### Expression-Oriented Style

**Expression body is mandatory**: All functions and methods in production code must use expression body syntax. Chain operations using scope functions, collection operators, or method chaining.

```kotlin
// Correct - expression body with chaining
fun getTableName(): String = "users"

fun isActive(): Boolean = status == Status.ACTIVE

fun process(input: String): String =
    input.trim()
        .let { validate(it) }
        .let { transform(it) }

fun findUser(id: Long): User? =
    repository.findById(id)
        .orElse(null)

fun loadDataSets(testCase: TestCase): List<DataSet> =
    resolveDataSetPaths(testCase)
        .map { loadDataSet(it) }
        .filter { it.isNotEmpty() }

// Correct - complex logic with run or let
fun execute(config: Configuration): Result =
    config.validate()
        .let { validConfig ->
            processor.process(validConfig)
        }
        .also { logger.info("Execution completed: $it") }

// Incorrect - block body is prohibited in production code
fun process(input: String): String {
    val trimmed = input.trim()
    val validated = validate(trimmed)
    return transform(validated)
}
```

**Scope functions for chaining**: Use `let`, `run`, `also`, `apply` to chain multiple operations.

```kotlin
// Chain with let for transformations
fun resolveScenarioName(testCase: TestCase): String =
    testCase.name.testName
        .let { sanitizeName(it) }
        .let { resolveFromAnnotation(testCase) ?: it }

// Chain with also for side effects
fun createRegistry(): DataSourceRegistry =
    DataSourceRegistry()
        .also { registry -> dataSources.forEach { registry.register(it.key, it.value) } }
        .also { logger.info("Registry created with ${dataSources.size} sources") }

// Chain with apply for initialization
fun createConfiguration(): Configuration =
    Configuration.builder()
        .apply { schemaName(schema) }
        .apply { timeout(timeoutMs) }
        .build()
```

**`when` expressions**: Use for multi-branch conditions.

```kotlin
val result = when (status) {
    Status.ACTIVE -> "Active"
    Status.INACTIVE -> "Inactive"
    Status.PENDING -> "Pending"
}

// With subject
val description = when {
    value < 0 -> "negative"
    value == 0 -> "zero"
    else -> "positive"
}
```

---

## Idiomatic Kotlin

### Data Classes

Use data classes for value objects.

```kotlin
data class TestContext(
    val testClass: KClass<*>,
    val testMethod: String,
    val configuration: Configuration,
    val registry: DataSourceRegistry
)
```

**Requirements**:
- At least one primary constructor parameter
- Parameters must be `val` or `var` (prefer `val`)
- Cannot be abstract, open, sealed, or inner

### Sealed Classes

Use sealed classes for restricted class hierarchies.

```kotlin
sealed class DataSetResult {
    data class Success(val dataSet: DataSet) : DataSetResult()
    data class Error(val message: String, val cause: Throwable?) : DataSetResult()
    data object Empty : DataSetResult()
}

// Exhaustive when
fun handle(result: DataSetResult): String = when (result) {
    is DataSetResult.Success -> "Loaded: ${result.dataSet.name}"
    is DataSetResult.Error -> "Error: ${result.message}"
    DataSetResult.Empty -> "No data"
}
```

### Object Declarations

**Singleton objects**: Use `object` for singletons.

```kotlin
object DefaultConfiguration {
    val schemaName: String = "public"
    val timeout: Long = 30_000L
}
```

**Companion objects**: Use for factory methods and constants.

```kotlin
class Configuration private constructor(
    val schemaName: String,
    val timeout: Long
) {
    companion object {
        private const val DEFAULT_TIMEOUT = 30_000L

        fun create(schemaName: String): Configuration =
            Configuration(schemaName, DEFAULT_TIMEOUT)

        fun withTimeout(schemaName: String, timeout: Long): Configuration =
            Configuration(schemaName, timeout)
    }
}
```

### Extension Functions

Use extension functions to add functionality to existing classes.

```kotlin
fun TestCase.findPreparation(): Preparation? =
    this.spec::class.findAnnotation<Preparation>()

fun DataSource.executeScript(script: String) {
    connection.use { conn ->
        conn.createStatement().use { stmt ->
            stmt.execute(script)
        }
    }
}
```

**Guidelines**:
- Use for utility functions that operate on a type
- Prefer extension functions over utility classes
- Place in appropriately named files (e.g., `TestCaseExtensions.kt`)

---

## String Handling

**String templates**: Use `${}` for variable interpolation.

```kotlin
val message = "Processing table: $tableName"
val formatted = "Found ${items.size} items in ${dataSet.name}"
```

**Raw strings**: Use for multi-line text and regex patterns.

```kotlin
val sql = """
    SELECT id, name, email
    FROM users
    WHERE status = 'active'
""".trimIndent()

val pattern = """\d{4}-\d{2}-\d{2}""".toRegex()
```

**`trimIndent()` and `trimMargin()`**: Use for indentation handling.

```kotlin
// trimIndent - removes common indent
val text = """
    Line 1
    Line 2
""".trimIndent()

// trimMargin - uses | as margin
val text = """
    |Line 1
    |Line 2
""".trimMargin()
```

---

## Collections

### Collection Functions

Use functional transformations instead of loops.

```kotlin
// Transform
val names = users.map { it.name }

// Filter
val active = users.filter { it.isActive }

// Find
val admin = users.find { it.role == Role.ADMIN }

// Check
val allActive = users.all { it.isActive }
val anyAdmin = users.any { it.role == Role.ADMIN }

// Reduce
val total = orders.sumOf { it.amount }
```

**Chained operations**:

```kotlin
val result = records
    .filter { it.isActive }
    .map { it.value }
    .filterNot { it.isBlank() }
    .map { it.uppercase() }
    .toList()
```

**Destructuring**:

```kotlin
// Map entries
for ((key, value) in map) {
    println("$key: $value")
}

// Data classes
users.forEach { (name, email, age) ->
    println("$name ($email): $age")
}
```

### Sequence Processing

Use sequences for large collections or chain operations.

```kotlin
// Sequence for lazy evaluation
val result = records.asSequence()
    .filter { it.isActive }
    .map { it.transform() }
    .take(100)
    .toList()
```

**When to use sequences**:
- Large collections (thousands of elements)
- Multiple intermediate operations
- Short-circuiting operations (`first`, `take`)

---

## Functions and Lambdas

### Default and Named Parameters

Use default parameters instead of overloading.

```kotlin
fun createConfiguration(
    schemaName: String = "public",
    timeout: Long = 30_000L,
    retryCount: Int = 3
): Configuration = Configuration(schemaName, timeout, retryCount)

// Call with named parameters
val config = createConfiguration(timeout = 60_000L)
```

### Lambda Expressions

**Trailing lambda**: Place lambda outside parentheses when it is the last parameter.

```kotlin
list.forEach { item ->
    println(item)
}

list.map { it.uppercase() }
```

**`it` implicit parameter**: Use for single-parameter lambdas when clear.

```kotlin
// Clear - use it
val names = users.map { it.name }

// Ambiguous - use explicit parameter
val result = items.fold(0) { acc, item -> acc + item.value }
```

**Lambda return**: Use `return@label` for non-local returns.

```kotlin
list.forEach { item ->
    if (item.isInvalid) return@forEach
    process(item)
}
```

### Scope Functions

| Function | Object Reference | Return Value | Use Case |
|----------|------------------|--------------|----------|
| `let` | `it` | Lambda result | Null checks, transformations |
| `run` | `this` | Lambda result | Object configuration, computations |
| `with` | `this` | Lambda result | Calling multiple methods |
| `apply` | `this` | Context object | Object configuration |
| `also` | `it` | Context object | Additional effects |

**Examples**:

```kotlin
// let - null check and transform
val length = name?.let { it.trim().length }

// apply - object configuration
val dataSource = JdbcDataSource().apply {
    setURL("jdbc:h2:mem:test")
    setUser("sa")
    setPassword("")
}

// also - side effects
val result = processData().also { logger.info("Processed: $it") }

// run - compute result from object
val result = configuration.run {
    "$schemaName:$timeout"
}

// with - multiple operations
with(registry) {
    register("primary", primaryDataSource)
    register("secondary", secondaryDataSource)
}
```

---

## Error Handling

**Use `require` for preconditions**:

```kotlin
fun process(input: String, count: Int) {
    require(input.isNotBlank()) { "Input must not be blank" }
    require(count > 0) { "Count must be positive" }
    // ...
}
```

**Use `check` for state validation**:

```kotlin
fun execute() {
    check(isInitialized) { "Must be initialized before execution" }
    // ...
}
```

**Exception handling**:

```kotlin
try {
    loadData()
} catch (e: IOException) {
    throw DataSetLoadException("Failed to load: $path", e)
} catch (e: SQLException) {
    throw DatabaseTesterException("Database error", e)
}
```

**`runCatching` and `Result`**:

```kotlin
val result = runCatching { loadData() }
    .getOrElse { emptyList() }

val result = runCatching { parseConfig() }
    .onFailure { logger.error("Parse failed", it) }
    .getOrThrow()
```

---

## Coroutines

**Suspend functions**: Use expression body with `withContext`.

```kotlin
suspend fun loadDataAsync(): DataSet =
    withContext(Dispatchers.IO) {
        dataLoader.load(path)
    }
```

**Structured concurrency**: Use expression body with coroutine scopes.

```kotlin
suspend fun processAll(items: List<Item>): List<Result> =
    coroutineScope {
        items
            .map { item -> async { process(item) } }
            .awaitAll()
    }
```

**Kotest integration**: Use expression body with `runCatching` for test extensions.

```kotlin
override suspend fun intercept(
    testCase: TestCase,
    execute: suspend (TestCase) -> TestResult
): TestResult =
    runCatching { prepare(testCase) }
        .mapCatching { execute(testCase) }
        .also { verify(testCase) }
        .getOrThrow()
```

**Alternative pattern**: When cleanup must run regardless of success, use expression with `run`.

```kotlin
override suspend fun intercept(
    testCase: TestCase,
    execute: suspend (TestCase) -> TestResult
): TestResult =
    prepare(testCase).run {
        execute(testCase).also { verify(testCase) }
    }
```

---

## Java Interoperability

**JvmStatic**: Use for companion object members callable from Java.

```kotlin
companion object {
    @JvmStatic
    fun create(): DatabaseTestExtension = DatabaseTestExtension()
}
```

**JvmField**: Use for properties accessible as fields from Java.

```kotlin
companion object {
    @JvmField
    val DEFAULT_TIMEOUT = 30_000L
}
```

**JvmOverloads**: Generate overloads for default parameters.

```kotlin
@JvmOverloads
fun createConfiguration(
    schemaName: String = "public",
    timeout: Long = 30_000L
): Configuration = Configuration(schemaName, timeout)
```

**Nullability annotations**: Kotlin nullability is preserved for Java callers via `@NotNull` and `@Nullable` annotations.

---

## Documentation

### KDoc Requirements

**All public elements require KDoc** (enforced by Dokka).

**Class documentation**:

```kotlin
/**
 * Kotest extension for database testing with CSV-based test data management.
 *
 * This extension implements [TestCaseExtension] to intercept test execution,
 * handling data preparation before tests and expectation verification after.
 *
 * @property registry the data source registry for database connections
 * @see TestCaseExtension
 * @see Preparation
 * @see Expectation
 */
class DatabaseTestExtension(
    private val registry: DataSourceRegistry
) : TestCaseExtension {
```

**Function documentation**:

```kotlin
/**
 * Finds the [Preparation] annotation for the given test case.
 *
 * Searches the test method first, then the spec class for the annotation.
 *
 * @param testCase the test case to search
 * @return the [Preparation] annotation, or null if not found
 */
private fun findPreparation(testCase: TestCase): Preparation? {
```

**Property documentation**:

```kotlin
/** The data source registry for database connections. */
private val registry: DataSourceRegistry
```

### KDoc Tags

| Tag | Usage |
|-----|-------|
| `@param` | Document function parameters |
| `@return` | Document return value |
| `@throws` | Document exceptions |
| `@property` | Document constructor properties |
| `@see` | Reference related elements |
| `@sample` | Reference sample code |

---

## Logging

Use **SLF4J** for all logging.

**Logger initialization** in companion object:

```kotlin
companion object {
    private val logger = LoggerFactory.getLogger(DatabaseTestExtension::class.java)
}
```

**Parameterized logging**:

```kotlin
logger.info("Loading dataset from {}", path)
logger.debug("Processing {} items", items.size)
logger.error("Failed to connect: {}", url, exception)
```

**Log levels**:

| Level | Use Case |
|-------|----------|
| `error` | Errors preventing normal operation |
| `warn` | Potential issues without execution interruption |
| `info` | Important business/lifecycle events |
| `debug` | Detailed diagnostic information |
| `trace` | Detailed diagnostics for troubleshooting |
