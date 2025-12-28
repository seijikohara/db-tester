---
paths: "**/*.java"
---

# Java Code Style Rules

**All rules in this document are mandatory unless explicitly marked as Optional.**

**Reference**: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

---

## Quick Reference

| Category | Rule |
|----------|------|
| Variables | All parameters and local variables must be `final`; use `final var` for local declarations |
| Null Safety | Every package requires `package-info.java` with `@NullMarked`; use `Optional<T>` for return values and `@Nullable` for parameters/fields |
| Immutability | All classes must be immutable; no setters; return immutable collections |
| Collections | Return specific immutable types (`List`, `Set`); accept abstract types (`Collection`, `Iterable`) |
| Loops | Use Stream API; for/while loops are prohibited for collection processing |
| Exceptions | Catch specific types; wrap external exceptions at module boundaries |
| Javadoc | Required for all classes and methods; enforced by DocLint |
| Formatting | Use `String.format()` for string formatting |

---

## Code Organization

### Class Structure

Members are ordered as follows:

1. Static fields (constants first, then mutable static fields)
2. Instance fields
3. Constructors
4. Static methods (public → package-private → private)
5. Instance methods (public → package-private/protected → private)
6. Nested classes/records

**Logger placement**: `private static final Logger logger` belongs with static fields (after constants).

### Import Statements

**Requirements**:
- Import specific classes, one per line, sorted alphabetically
- Star imports (`import java.util.*;`) are prohibited
- Use fully qualified names to resolve ambiguity between same-named classes

```java
// Correct
import java.util.List;
import java.util.Optional;

// Incorrect
import java.util.*;
```

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `DataProcessor` |
| Methods/Variables | camelCase | `processData` |
| Constants | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| Packages | lowercase | `io.github.seijikohara.dbtester` |

---

## Language Fundamentals

### Variables and Parameters

**`final` modifier**:
- All parameters and local variables must be `final`

**`final var`**:
- Use for all local variable declarations

```java
final var dataSet = new CsvDataSet(path);
final var name = dataSet.getTableName();
```

**Unnamed variables `_`**:
- Use for unused lambda parameters or catch blocks

```java
.filter(_ -> !patternNames.isEmpty())
catch (final SQLException _) { return false; }
```

### Text Blocks and String Formatting

**Text blocks** (`"""`):
- Closing `"""` on own line includes trailing newline
- Closing `"""` immediately after content excludes trailing newline
- Indentation auto-stripped based on closing `"""` position

**String formatting**:
- Use `String.format()` for all formatting (single-line and multi-line)
- Avoid `.formatted()` method

```java
// Single-line
final var message = String.format("Processing table: %s", tableName);

// Multi-line with text block
final var error = String.format("""
    Failed to load dataset:
      File: %s
      Reason: %s
    """, fileName, reason);
```

### Pattern Matching and Switch

**Switch expressions**:
- Use for all mapping logic; avoid traditional switch statements
- Enum switches: No `default` branch (compiler enforces exhaustiveness)
- Non-enum switches: `default` branch required; throw `IllegalArgumentException`

```java
// Enum switch (exhaustive, no default)
return switch (status) {
  case ACTIVE -> "Active";
  case INACTIVE -> "Inactive";
  case PENDING -> "Pending";
};

// Non-enum switch (default required)
return switch (type) {
  case "csv" -> "text/csv";
  case "json" -> "application/json";
  default -> throw new IllegalArgumentException(
      String.format("Unknown type: %s", type));
};
```

**Primitive type patterns** (with guard clauses):
```java
return switch (value) {
  case Integer i when i > 0 -> "positive";
  case Integer i when i < 0 -> "negative";
  case Integer _ -> "zero";
  default -> "unknown";
};
```

### Constructors

**Constructor prologue**:
- Execute validation and computation before `super()` or `this()`
- Instance fields and methods cannot be accessed before `super()` or `this()`

**Validation order**:
- Argument validation → `super()` → Additional initialization

```java
public ValidatedWidget(final String name, final int size) {
  // Prologue: validation before super()
  if (name.isBlank()) {
    throw new IllegalArgumentException("name must not be blank");
  }
  if (size <= 0) {
    throw new IllegalArgumentException("size must be positive");
  }
  super(name, size);
}
```

---

## Type Design

### Records and Immutability

**Records**:
- Immutable data carriers with thread safety by design
- Use compact constructors for domain validation (format checks, range validation, business constraints)
- Null checks are not required (NullAway enforces at compile time)

```java
public record AppSettings(String name, int timeout) {
  public AppSettings {
    if (name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    if (timeout <= 0) {
      throw new IllegalArgumentException("timeout must be positive");
    }
  }
}
```

**Immutability requirements for all classes**:

| Requirement | Implementation |
|-------------|----------------|
| All fields final | No mutable state after construction |
| No setters | State cannot be modified |
| Immutable collection returns | Use `.toList()`, `List.copyOf()`, `Set.copyOf()`, `Map.copyOf()` |
| No arrays in API | Arrays are mutable; use collections |
| No builder pattern | Use constructors or factory methods (external library builders permitted) |

### Collections and Arrays

**Arrays in public APIs**:
- Arrays are prohibited in public APIs (varargs permitted for convenience methods)

**Return types (Producer)** — Be specific and immutable:

| Return Type | Use When | How to Create |
|-------------|----------|---------------|
| `List<T>` | Ordered, may contain duplicates | `.toList()` or `List.copyOf()` |
| `Set<T>` | Unique elements | `Set.copyOf()` |
| `Map<K,V>` | Key-value pairs | `Map.copyOf()` |

**Parameter types (Consumer)** — Be abstract:

| Need | Parameter Type |
|------|----------------|
| Iteration only | `Iterable<T>` |
| Read-only collection | `Collection<T>` |
| Order/index required | `List<T>` |
| Uniqueness required | `Set<T>` |
| Key-value lookup | `Map<K,V>` |

**Wildcards**:
- Producer Extends: Use `Collection<? extends T>` for read-only access
- Consumer Super: Use `Collection<? super T>` for write-only access
- Apply only when generic flexibility is required

---

## Null Safety

This project uses JSpecify annotations with NullAway for compile-time null safety.

### JSpecify Configuration

**Package-level `@NullMarked`**: Every package requires its own `package-info.java`:

```java
@NullMarked
package io.github.seijikohara.dbtester.api;

import org.jspecify.annotations.NullMarked;
```

**Subpackage requirement**: `@NullMarked` does not propagate to subpackages. Every subpackage must have its own `package-info.java`.

**NullAway configuration** in `build.gradle.kts`:
```kotlin
options.errorprone {
    check("NullAway", CheckSeverity.ERROR)
    option("NullAway:AnnotatedPackages", "io.github.seijikohara.dbtester,example")
    option("NullAway:JSpecifyMode", "true")
    option("NullAway:TreatGeneratedAsUnannotated", "true")
    option("NullAway:CheckOptionalEmptiness", "true")
    option("NullAway:CheckContracts", "true")
    option("NullAway:HandleTestAssertionLibraries", "true")
}
```

**Transitive dependency**: JSpecify exposed as `api` dependency for consumer null-safety.

### Package Structure

```
io.github.seijikohara.dbtester/
├── package-info.java                    // @NullMarked - REQUIRED
├── api/
│   ├── package-info.java                // @NullMarked - REQUIRED
│   └── dataset/
│       └── package-info.java            // @NullMarked - REQUIRED
└── internal/
    ├── package-info.java                // @NullMarked - REQUIRED
    └── loader/
        └── package-info.java            // @NullMarked - REQUIRED
```

**Impact of missing `package-info.java`**:
- Kotlin consumers see platform types (`Type!`) instead of proper nullable/non-null types
- NullAway does not enforce null safety in unmarked packages

**Build verification**: The `verifyNullMarkedPackages` Gradle task checks all Java packages:
```bash
./gradlew verifyNullMarkedPackages
```
This task runs automatically as part of `check` and fails the build if any package is missing `package-info.java` or `@NullMarked` annotation.

### Null Handling Patterns

**Core principles**:
- All types are non-null by default in `@NullMarked` packages
- `Objects.requireNonNull()` is not required (NullAway guarantees at compile time)
- Nullable types require explicit `@Nullable` annotation

**Nullability annotations**:

| Context | Annotation | Example |
|---------|------------|---------|
| Return values that may be absent | `Optional<T>` | `public Optional<Widget> find(String id)` |
| Nullable parameters | `@Nullable` | `void process(@Nullable String name)` |
| Nullable fields | `@Nullable` | `private @Nullable Widget defaultWidget` |

**Null handling with Optional chains**:

```java
// Anti-pattern: explicit null checks
if (name != null) {
  return new SchemaName(name);
}
return null;

// Correct: Return Optional
public Optional<SchemaName> getSchemaName(@Nullable String name) {
  return Optional.ofNullable(name)
      .map(SchemaName::new);
}
```

**Summary table**:

| Scope | Null Checks | Reason |
|-------|-------------|--------|
| `@NullMarked` packages | None needed | Compile-time enforcement |
| `@Nullable` parameters/fields | `@Nullable` annotation | Documents nullability |
| Return values | Use `Optional<T>` | Never return null |
| Private methods | None needed | NullAway guarantees non-null |

---

## Functional Programming

This project adopts functional programming principles: immutability, pure functions, and declarative style.

### Core Principles

1. **Immutability First**: Transform data instead of mutating
2. **Pure Functions**: Output based solely on input; no side effects
3. **Declarative Style**: Express WHAT, not HOW
4. **No Null Returns**: Use `Optional<T>` for absent values
5. **Higher-Order Functions**: Functions accept/return other functions

### Stream API

**Streams over loops**:
- Use Stream API for all collection operations
- Use `IntStream.range()` for index-based iteration
- Exception applies only to performance-critical code with documented profiling evidence

**Method references**:
- Prefer method references over lambdas when lambda only delegates to a method

**Lambda parameter names**:
- Descriptive for unclear context
- Short for obvious context (`i`, `e`, `s`)
- `_` for unused parameters

**Side effects**:
- Intermediate operations (map, filter, flatMap) should be pure when possible
- Side effects permitted in terminal operations (`forEach`)
- `peek()` may be used for debugging/logging

```java
// Declarative stream processing
return records.stream()
    .filter(DataRecord::isActive)
    .map(DataRecord::value)
    .filter(value -> !value.isBlank())
    .map(String::toUpperCase)
    .toList();

// Index-based iteration
IntStream.range(0, items.size())
    .forEach(i -> logger.info("Item {}: {}", i, items.get(i)));
```

### Optional Patterns

**Return `Optional<T>` for potentially absent values** (returning null is prohibited):

```java
public Optional<DataRecord> findById(final String id) {
  return Optional.ofNullable(storage.get(id));
}
```

**Use Optional chains** instead of null checks:

```java
// Chain operations
return findById(id)
    .filter(DataRecord::isActive)
    .map(DataRecord::value)
    .orElse("DEFAULT");

// Required values
return findById(id)
    .orElseThrow(() -> new IllegalStateException(
        String.format("Record not found: %s", id)));
```

**Optional.get() usage**:
- Calling `Optional.get()` without `isPresent()` check is prohibited; prefer `orElse` or `orElseThrow`

**Anti-patterns to avoid**:

| Imperative (Avoid) | Functional (Use) |
|--------------------|------------------|
| for/while loops | Stream API |
| `if (x != null)` | Optional chains |
| Mutable accumulator | Stream reduction |
| Nested if/else | Stream/Optional filter |
| Modify in place | Transform and return new |

---

## Error Handling

### Exception Types

| Type | Use Case |
|------|----------|
| `IllegalArgumentException` | Invalid arguments |
| `IllegalStateException` | Invalid state/configuration |
| Custom exceptions | Domain-specific errors (in `api.exception` package) |

**Framework exception hierarchy**:
```
api/exception/
├── DatabaseTesterException         # Base exception
├── ConfigurationException          # Configuration failures
├── DataSetLoadException            # Data loading failures
├── DataSourceNotFoundException     # Missing DataSource
└── ValidationException             # Validation failures
```

### Exception Handling Rules

1. **Catch specific exceptions**: List each type explicitly
   ```java
   catch (final SQLException | DataSetException e)
   ```

2. **Runtime exceptions**: Catching runtime exceptions without specific intent is prohibited

3. **Wrap external exceptions** at facade boundary
   ```java
   catch (final DatabaseUnitException e) {
     throw new DatabaseTesterException("Failed to execute", e);
   }
   ```

4. **Preserve stack traces**: Chain exceptions with cause parameter

5. **Re-throwing unchanged exceptions**: Remove catch blocks that only re-throw without modification

### Error Messages

- Use `String.format()` with contextual information
- Chain exceptions to preserve stack traces

```java
throw new DataSetLoadException(
    String.format("Failed to load data from: %s", path), cause);
```

---

## Documentation

### Documentation Style

All documentation must be **concise, technical, and formal**.

**Applies to**: Javadoc, code comments, technical documents, commit messages.

**Requirements**:
- Concise, direct statements
- Precise terminology
- Professional tone
- One concept per sentence

**Prohibited elements**:
- Casual expressions: "don't", "let's", "just", "simply"
- Exclamation marks
- Conversational phrases: "you should", "we recommend"
- Redundant modifiers: "very", "really", "quite"

| Informal | Formal |
|----------|--------|
| "You should use X" | "Use X" |
| "Don't do this" | "Anti-pattern" |
| "Simply call the method" | "Call the method" |

### Javadoc

**Strictly enforced** by DocLint with `doclint:all` and `-Werror`.

**Requirements**:
- All classes: Overview, purpose, usage examples
- All methods (including private): Complete Javadoc
  - `@param` for each parameter
  - `@return` for non-void methods (omit for void)
  - `@throws` only when exceptions thrown (omit otherwise)
- Code examples: Use `<pre>{@code ...}</pre>`
- Language: English only

```java
/**
 * Widget registry for managing widget instances.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * registry.register("widget1", new Widget());
 * }</pre>
 */
public final class WidgetRegistry {

  /**
   * Registers a widget.
   *
   * @param name unique identifier
   * @param widget the widget to register
   * @throws IllegalArgumentException if name is empty
   */
  public void register(final String name, final Widget widget) {
    // ...
  }
}
```

---

## Logging

Use **SLF4J** for all logging.

**Logger initialization**:
- Logger field must be named `logger` (lowercase), not `LOG` (uppercase)
- Logger field must be `private static final`
- Logger must be placed with static fields (after constants)

```java
private static final Logger logger = LoggerFactory.getLogger(ClassName.class);
```

**Parameterized logging**:
- String concatenation is prohibited; use parameterized logging
```java
logger.info("Loading dataset from {}", path);
logger.error("Failed to connect: {}", dbUrl, exception);
```

**Log levels**:

| Level | Use Case |
|-------|----------|
| `error` | Errors preventing normal operation (with stack trace) |
| `warn` | Potential issues without execution interruption |
| `info` | Important business/lifecycle events |
| `debug` | Detailed diagnostic information |
| `trace` | Detailed diagnostics (rarely used) |

---

## Thread Safety

**Immutable objects**:
- Prefer immutable objects (inherently thread-safe)

**Mutable state**:
- Use concurrent collections (`ConcurrentHashMap`, `CopyOnWriteArrayList`)
- Return immutable snapshots from public API
- Explicit synchronization is prohibited

```java
public final class WidgetRegistry {
  private final ConcurrentHashMap<String, Widget> widgets = new ConcurrentHashMap<>();

  public void register(final String name, final Widget widget) {
    widgets.put(name, widget);
  }

  public List<String> getNames() {
    return List.copyOf(widgets.keySet());  // Immutable snapshot
  }
}
```

