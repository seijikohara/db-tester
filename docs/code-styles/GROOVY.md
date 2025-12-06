# Groovy Code Style Rules

**All rules in this document are mandatory.**

**Reference**: [Apache Groovy Style Guide](https://groovy-lang.org/style-guide.html)

---

## Table of Contents

1. [Quick Reference](#quick-reference)
2. [Code Organization](#code-organization)
   - [Class Structure](#class-structure)
   - [Import Statements](#import-statements)
   - [Naming Conventions](#naming-conventions)
3. [Language Fundamentals](#language-fundamentals)
   - [Syntax Conventions](#syntax-conventions)
   - [Type Declarations](#type-declarations)
   - [Variables](#variables)
4. [Idiomatic Groovy Patterns](#idiomatic-groovy-patterns)
   - [Property Access](#property-access)
   - [Safe Navigation](#safe-navigation)
   - [Object Initialization](#object-initialization)
5. [String Handling](#string-handling)
   - [GStrings and Interpolation](#gstrings-and-interpolation)
   - [Multiline Strings](#multiline-strings)
6. [Collections](#collections)
   - [Native Syntax](#native-syntax)
   - [GDK Methods](#gdk-methods)
7. [Closures](#closures)
8. [Error Handling](#error-handling)
9. [Documentation](#documentation)
10. [Logging](#logging)

---

## Quick Reference

| Category | Rule |
|----------|------|
| Semicolons | Omit semicolons |
| Parentheses | Omit for top-level calls and when closure is last parameter |
| Return | Omit in closures and simple methods; use explicit return for clarity in complex methods |
| Type declarations | Use explicit types for public APIs; `def` for local variables when type is obvious |
| Visibility | Omit `public` modifier (Groovy default) |
| Property access | Use `object.property` instead of `object.getProperty()` |
| String interpolation | Use GStrings with `${}` for variable interpolation |
| Collections | Use native syntax `[]` for lists and `[:]` for maps |
| Null safety | Use safe navigation `?.` and Elvis operator `?:` |
| Javadoc | Required for all classes and methods; enforced by DocLint |

---

## Code Organization

### Class Structure

Members are ordered as follows:

1. Static fields (constants first)
2. Instance fields
3. Constructors
4. Static methods
5. Instance methods (public → package-private → private)
6. Nested classes

**Logger placement**: `private static final Logger logger` belongs with static fields (after constants).

### Import Statements

**Requirements**:
- Import specific classes, one per line, sorted alphabetically
- Star imports (`import java.util.*`) are prohibited
- Use fully qualified names to resolve ambiguity

```groovy
// Correct
import groovy.sql.Sql
import javax.sql.DataSource
import spock.lang.Specification

// Incorrect
import java.util.*
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `DatabaseTestInterceptor` |
| Methods/Variables | camelCase | `createTestContext` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `io.github.seijikohara.dbtester.spock` |

---

## Language Fundamentals

### Syntax Conventions

**Semicolons**: Omit semicolons. Groovy makes them optional.

```groovy
// Correct
def name = 'value'
def list = [1, 2, 3]

// Incorrect
def name = 'value';
def list = [1, 2, 3];
```

**Parentheses**: Omit for top-level method calls and when closures are the last parameter.

```groovy
// Correct
list.each { println it }
list.collect { it * 2 }

// Incorrect
list.each() { println it }
list.collect() { it * 2 }
```

**Return statements**: Omit in closures and simple methods. Use explicit return for clarity in complex methods.

```groovy
// Closure - omit return
def doubled = list.collect { it * 2 }

// Simple method - omit return
String getName() {
    name
}

// Complex method - explicit return for clarity
String processData(String input) {
    if (input == null) {
        return 'default'
    }
    def result = transform(input)
    if (result.isEmpty()) {
        return 'empty'
    }
    return result
}
```

### Type Declarations

**Avoid redundant `def` with explicit types**:

```groovy
// Correct
String name = 'value'
def name = 'value'

// Incorrect - never mix def with type
def String name = 'value'
```

**Public APIs**: Use explicit types for method parameters, return types, and public fields.

```groovy
// Public API - explicit types
String processData(String input, int count) {
    // implementation
}

// Private/local - def is acceptable
private void helper() {
    def result = computeValue()
}
```

**Visibility**: Omit `public` modifier. Groovy classes and methods are public by default.

```groovy
// Correct
class MyClass {
    String name
    void doSomething() { }
}

// Incorrect
public class MyClass {
    public String name
    public void doSomething() { }
}
```

### Variables

**Local variables**: Use `def` for local variables when type is obvious from context.

```groovy
def list = [1, 2, 3]
def map = [name: 'value']
def sql = new Sql(dataSource)
```

**Fields**: Use explicit types for instance and static fields.

```groovy
class MySpec extends Specification {
    DataSource dataSource
    Sql sql
    static DataSourceRegistry sharedRegistry
}
```

---

## Idiomatic Groovy Patterns

### Property Access

Use property notation instead of getter/setter method calls.

```groovy
// Correct
def name = object.name
object.status = 'active'

// Incorrect
def name = object.getName()
object.setStatus('active')
```

### Safe Navigation

Use `?.` operator to prevent null pointer exceptions.

```groovy
// Correct
def name = user?.profile?.name

// Incorrect
def name = null
if (user != null && user.profile != null) {
    name = user.profile.name
}
```

**Elvis operator**: Use `?:` for default values.

```groovy
// Correct
def result = name ?: 'Unknown'

// Incorrect
def result = name != null ? name : 'Unknown'
```

### Object Initialization

**Named parameters**: Use for bean initialization.

```groovy
def server = new Server(name: 'Obelix', cluster: aCluster)
```

**`tap()` method**: Use for initialization with method calls.

```groovy
def dataSource = new JdbcDataSource().tap {
    setURL('jdbc:h2:mem:test')
    setUser('sa')
    setPassword('')
}
```

**`with()` method**: Use for multiple operations on same object.

```groovy
server.with {
    name = application.name
    status = 'active'
    start()
}
```

---

## String Handling

### GStrings and Interpolation

Use double quotes with `${}` for variable interpolation.

```groovy
// Simple variable
def message = "Processing table: $tableName"

// Expression
def message = "Found ${list.size()} items"

// Method call
def message = "Name: ${user.getName()}"
```

**Single quotes**: Use for plain strings without interpolation.

```groovy
def query = 'SELECT * FROM users'
def constant = 'FIXED_VALUE'
```

### Multiline Strings

Use triple quotes for multiline strings.

```groovy
// Without interpolation
def sql = '''
    SELECT id, name, email
    FROM users
    WHERE status = 'active'
'''

// With interpolation
def message = """
    Failed to load dataset:
      File: $fileName
      Reason: $reason
"""
```

**SQL statements**: Use triple-quoted strings for SQL.

```groovy
sql.execute '''
    INSERT INTO TABLE1 (ID, COLUMN1, COLUMN2)
    VALUES (1, 'Test', 99.99)
'''
```

---

## Collections

### Native Syntax

Use Groovy's native collection literals.

```groovy
// Lists
def list = [1, 4, 6, 9]
def emptyList = []

// Maps
def map = [CA: 'California', MI: 'Michigan']
def emptyMap = [:]

// Ranges
def range = 1..10      // inclusive
def exclusive = 1..<10  // exclusive end
```

**Collection operations**:

```groovy
// Check membership
assert 4 in list

// Add to map
map << [WA: 'Washington']

// Spread operator
def names = users*.name
```

### GDK Methods

Use functional methods for collection processing.

```groovy
// Transform
def doubled = list.collect { it * 2 }

// Filter
def active = users.findAll { it.active }

// Find single
def first = users.find { it.name == 'John' }

// Check all/any
def allActive = users.every { it.active }
def anyActive = users.any { it.active }

// Iterate
list.each { println it }
list.eachWithIndex { item, index -> println "$index: $item" }

// Reduce
def sum = list.inject(0) { acc, val -> acc + val }
```

**Method chaining**:

```groovy
resource.text
    .split(';')
    .collect { it.trim() }
    .findAll { !it.empty }
    .each { sql.execute(it) }
```

---

## Closures

**Single parameter**: Use implicit `it` for single-parameter closures.

```groovy
list.each { println it }
list.findAll { it > 5 }
```

**Multiple parameters**: Name parameters explicitly.

```groovy
map.each { key, value -> println "$key: $value" }
list.eachWithIndex { item, index -> println "$index: $item" }
```

**Type declaration**: Specify types when clarity is needed.

```groovy
def processor = { String input, int count ->
    input * count
}
```

**Method references**: Use `&` operator for method references.

```groovy
def names = users.collect { it.getName() }
// Or with method reference
def names = users*.name
```

---

## Error Handling

**Exception handling**: Catch exceptions without specifying type when appropriate.

```groovy
try {
    riskyOperation()
} catch (any) {
    logger.error('Operation failed', any)
}
```

**Specific exceptions**: Catch specific types when handling differs.

```groovy
try {
    loadData()
} catch (IOException e) {
    throw new DataSetLoadException("Failed to load: $path", e)
} catch (SQLException e) {
    throw new DatabaseTesterException("Database error", e)
}
```

**Resource cleanup**: Use `?.close()` for safe cleanup.

```groovy
def cleanupSpec() {
    sql?.close()
}
```

---

## Documentation

### Javadoc Requirements

**All Groovy code requires Javadoc** (enforced by DocLint):

1. **Classes**: Overview and purpose
2. **Methods**: Description with `@param`, `@return`, `@throws` as needed
3. **Fields**: Single-line Javadoc for non-obvious fields

```groovy
/**
 * Spock method interceptor that handles database testing operations.
 *
 * <p>This interceptor executes the preparation phase before the test method
 * and the expectation verification phase after the test method completes.
 */
class DatabaseTestInterceptor implements IMethodInterceptor {

    /** The preparation annotation (may be null). */
    private final Preparation preparation

    /**
     * Creates a new interceptor with the given annotations.
     *
     * @param preparation the preparation annotation (may be null)
     * @param expectation the expectation annotation (may be null)
     */
    DatabaseTestInterceptor(Preparation preparation, Expectation expectation) {
        this.preparation = preparation
        this.expectation = expectation
    }

    /**
     * Gets the configuration from the specification instance.
     *
     * @param invocation the method invocation
     * @return the configuration
     */
    private Configuration getOrCreateConfiguration(IMethodInvocation invocation) {
        // implementation
    }
}
```

---

## Logging

Use **SLF4J** for all logging.

**Logger initialization**:

```groovy
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MyClass {
    private static final Logger logger = LoggerFactory.getLogger(MyClass)
}
```

**Parameterized logging**: Use placeholders instead of string concatenation.

```groovy
logger.info('Loading dataset from {}', path)
logger.error('Failed to connect: {}', dbUrl, exception)
logger.debug('Processing {} items', list.size())
```

**Log levels**:

| Level | Use Case |
|-------|----------|
| `error` | Errors preventing normal operation |
| `warn` | Potential issues without execution interruption |
| `info` | Important business/lifecycle events |
| `debug` | Detailed diagnostic information |
| `trace` | Detailed diagnostics for troubleshooting |

