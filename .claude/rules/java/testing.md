---
paths: "**/src/test/**/*.java"
---

# Java Testing Code Style Rules

**All rules in this document are mandatory unless explicitly marked as Optional.**

---

## Table of Contents

- [Testing Framework](#testing-framework)
- [Test Class Structure](#test-class-structure)
- [Nested Test Classes](#nested-test-classes)
- [Test Method Naming](#test-method-naming)
- [Test Method Annotations](#test-method-annotations)
- [Test Method Structure (AAA Pattern)](#test-method-structure-aaa-pattern)
- [Assertions](#assertions)
- [Mocking](#mocking)
- [Test Fixtures and Helpers](#test-fixtures-and-helpers)
- [Documentation](#documentation)
- [Import Statements](#import-statements)
- [Null Safety in Tests](#null-safety-in-tests)

---

## Testing Framework

### Required Dependencies

- **JUnit**: Primary testing framework
- **Mockito**: Mocking framework for test doubles
- **JUnit TempDir**: For temporary file system testing

### Framework Annotations

| Annotation | Purpose |
|------------|---------|
| `@Test` | Marks a method as a test method |
| `@DisplayName` | Human-readable test description |
| `@Nested` | Groups related tests in nested classes |
| `@Tag` | Categorizes tests (normal, edge-case, error) |
| `@BeforeEach` | Setup method executed before each test |
| `@TempDir` | Injects temporary directory for file tests |

---

## Test Class Structure

### Class-Level Requirements

1. **Package-private visibility**: Test classes are package-private (no `public` modifier)
2. **`@DisplayName` annotation**: Class name of the tested class
3. **Constructor with Javadoc**: Every test class requires a constructor with Javadoc
4. **Instance fields**: Declare test fixtures and mocks as instance fields

### Example Structure

```java
/** Unit tests for {@link TargetClass}. */
@DisplayName("TargetClass")
class TargetClassTest {

  /** Tests for the TargetClass class. */
  TargetClassTest() {}

  /** The instance under test. */
  private TargetClass target;

  /** Mock dependency for tests. */
  private Dependency mockDependency;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    mockDependency = mock(Dependency.class);
    target = new TargetClass(mockDependency);
  }

  // Nested test classes follow...
}
```

---

## Nested Test Classes

### Organization Strategy

Tests must be grouped by **method under test** using `@Nested` classes:

- Each public method requires its own nested class
- Constructors require a nested class named `ConstructorMethod`
- Logical groupings (e.g., "error handling", "multiple tables") are permitted

### Nested Class Requirements

1. **`@Nested` annotation**: Required for all nested test classes
2. **`@DisplayName` annotation**: Format: `"methodName() method"` or descriptive grouping
3. **Constructor with Javadoc**: Every nested class requires a constructor with Javadoc
4. **Instance fields**: Declare nested-class-specific fixtures if needed

### Naming Conventions

| Method Type | `@DisplayName` Format |
|-------------|------------------------|
| Instance method | `"methodName() method"` |
| Instance method with params | `"methodName(ParamType) method"` |
| Constructor | `"constructor"` |
| Logical grouping | Descriptive name (e.g., `"error handling"`) |

### Example

```java
/** Tests for the getValue() method. */
@Nested
@DisplayName("getValue(ColumnName) method")
class GetValueMethod {

  /** Tests for the getValue method. */
  GetValueMethod() {}

  /** Verifies that getValue returns value when column exists. */
  @Test
  @Tag("normal")
  @DisplayName("should return value when column exists")
  void shouldReturnValue_whenColumnExists() {
    // Test implementation
  }
}
```

---

## Test Method Naming

### Method Name Pattern

Use the `should[Expected]_when[Condition]` naming convention:

```
should{ExpectedBehavior}_when{Condition}
```

### Examples

| Scenario | Method Name |
|----------|-------------|
| Returns value for valid input | `shouldReturnValue_whenValidInputProvided` |
| Throws exception for null | `shouldThrowException_whenInputIsNull` |
| Creates instance successfully | `shouldCreateInstance_whenCalled` |
| Returns empty when not found | `shouldReturnEmpty_whenNotFound` |
| Filters by criteria | `shouldFilterRows_whenCriteriaProvided` |

### Rules

- camelCase with underscore separating "should" and "when" clauses is required
- Specific description of expected behavior is required
- Specific description of condition or trigger is required
- Abbreviations in method names are prohibited

---

## Test Method Annotations

### Required Annotations

Every test method must have:

1. `@Test`: Marks as test method
2. `@Tag`: Categorizes the test
3. `@DisplayName`: Human-readable description

### Tag Categories

| Tag | Use Case |
|-----|----------|
| `"normal"` | Standard successful path tests |
| `"edge-case"` | Boundary conditions, empty inputs, special cases |
| `"error"` | Exception scenarios, invalid inputs, failure paths |

### DisplayName Format

Format: `"should [expected behavior] when [condition]"`

```java
@Test
@Tag("normal")
@DisplayName("should return annotation when method has Preparation annotation")
void shouldReturnAnnotation_whenMethodHasPreparationAnnotation() {
  // ...
}

@Test
@Tag("edge-case")
@DisplayName("should create defensive copy when map is modified after construction")
void shouldCreateDefensiveCopy_whenMapIsModifiedAfterConstruction() {
  // ...
}

@Test
@Tag("error")
@DisplayName("should throw exception when directory does not exist")
void shouldThrowException_whenDirectoryDoesNotExist() {
  // ...
}
```

---

## Test Method Structure (AAA Pattern)

### Arrange-Act-Assert Pattern

Structure all tests using the AAA pattern with explicit comments:

```java
@Test
@Tag("normal")
@DisplayName("should return value when valid input provided")
void shouldReturnValue_whenValidInputProvided() {
  // Given
  final var input = createValidInput();
  final var expected = new ExpectedResult("value");

  // When
  final var result = target.process(input);

  // Then
  assertEquals(expected, result, "should return expected value");
}
```

### Section Guidelines

| Section | Purpose | Guidelines |
|---------|---------|-----------|
| `// Given` | Setup test data and preconditions | Initialize inputs, configure mocks, set up state |
| `// When` | Execute the method under test | Single method call (usually) |
| `// Then` | Verify results and side effects | Assertions with descriptive messages |

### Exception Testing Pattern

For exception tests, combine `// When` and `// Then`:

```java
@Test
@Tag("error")
@DisplayName("should throw exception when directory does not exist")
void shouldThrowException_whenDirectoryDoesNotExist(final @TempDir Path tempDir) {
  // Given
  final var nonExistentDir = tempDir.resolve("nonexistent");

  // When & Then
  final var exception = assertThrows(
      DataSetLoadException.class,
      () -> target.process(nonExistentDir));

  final var message = exception.getMessage();
  assertTrue(
      message != null && message.contains("does not exist"),
      "exception should mention directory does not exist");
}
```

---

## Assertions

### Preferred Assertions

Use JUnit assertions with static imports:

```java
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
```

### Assertion Messages

**All assertions must include descriptive messages**:

```java
// Correct: includes message
assertEquals(expected, actual, "should return expected value");
assertTrue(result.isPresent(), "should find annotation on method");
assertNotNull(instance, "instance should not be null");

// Incorrect: no message
assertEquals(expected, actual);
assertTrue(result.isPresent());
```

### Grouped Assertions with assertAll

Use `assertAll` to group related assertions:

```java
@Test
@Tag("normal")
@DisplayName("should store values when valid map provided")
void shouldStoreValues_whenValidMapProvided() {
  // Given
  final var values = Map.of(col1, value1, col2, value2);

  // When
  final var row = new CsvRow(values);

  // Then
  final var result = row.getValues();
  assertAll(
      "row should contain all provided values",
      () -> assertEquals(2, result.size(), "should have 2 entries"),
      () -> assertEquals(value1, result.get(col1), "should have value1 for column1"),
      () -> assertEquals(value2, result.get(col2), "should have value2 for column2"));
}
```

### assertAll Guidelines

- First parameter is the group description (what is being verified)
- Lambda expressions for individual assertions are required
- Each assertion must include its own message
- `assertAll` is preferred when verifying multiple properties of the same result

---

## Mocking

### Mockito Usage

Use Mockito for creating test doubles:

```java
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
```

### Mock Creation Patterns

**Instance field mocks** (initialized in `@BeforeEach`):

```java
/** Mock data source for tests. */
private DataSource mockDataSource;

@BeforeEach
void setUp() {
  mockDataSource = mock(DataSource.class);
}
```

**Helper methods for complex mocks**:

```java
/**
 * Creates a mock Table with the specified properties.
 *
 * @param tableName the table name
 * @param columns the columns
 * @param rows the rows
 * @return mock Table instance
 */
private static Table createMockTable(
    final TableName tableName,
    final List<ColumnName> columns,
    final List<Row> rows) {
  final var mockTable = mock(Table.class);
  when(mockTable.getName()).thenReturn(tableName);
  when(mockTable.getColumns()).thenReturn(columns);
  when(mockTable.getRows()).thenReturn(rows);
  return mockTable;
}
```

---

## Test Fixtures and Helpers

### Temporary Directories

Use `@TempDir` for file system tests:

```java
@Test
@Tag("normal")
@DisplayName("should return directory when table ordering file exists")
void shouldReturnDirectory_whenTableOrderingFileExists(final @TempDir Path tempDir)
    throws IOException {
  // Given
  createTableOrderingFile(tempDir, "CUSTOM_TABLE");
  createDataFile(tempDir, "TABLE1.csv");

  // When
  final var result = resolver.ensureTableOrdering(tempDir);

  // Then
  assertEquals(tempDir, result, "should return the directory");
}
```

### Helper Methods

Create helper methods for repetitive setup:

```java
/**
 * Creates a CSV file with the specified content.
 *
 * @param dir the directory to create the file in
 * @param fileName the file name
 * @param lines the CSV lines
 * @throws IOException if file creation fails
 */
private static void createCsvFile(final Path dir, final String fileName, final String... lines)
    throws IOException {
  final var content = String.join("\n", lines);
  Files.writeString(dir.resolve(fileName), content);
}
```

### Test Stub Classes

Define stub classes for annotation testing or complex scenarios:

```java
/** Test class with method-level Preparation annotation. */
static class TestClassWithMethodAnnotation {
  /** Test constructor. */
  TestClassWithMethodAnnotation() {}

  /** Test method with Preparation annotation. */
  @Preparation
  void testMethod() {}
}
```

**Stub Class Requirements**:
- `static` nested class definition is required
- Javadoc on class and constructor is required
- Javadoc on methods is required

---

## Documentation

### Javadoc Requirements

**All test code requires Javadoc** (enforced by DocLint):

1. **Test class**: `/** Unit tests for {@link TargetClass}. */`
2. **Test class constructor**: `/** Tests for the TargetClass class. */`
3. **Nested class**: `/** Tests for the methodName() method. */`
4. **Nested class constructor**: `/** Tests for the methodName method. */`
5. **Test methods**: Full Javadoc with `@param`, `@throws` as needed
6. **Helper methods**: Full Javadoc with `@param`, `@return`, `@throws`
7. **Stub classes**: Class and constructor Javadoc
8. **Instance fields**: Single-line Javadoc comment

### Test Method Javadoc Pattern

```java
/**
 * Verifies that [method] [behavior] when [condition].
 *
 * @param tempDir temporary directory for test files
 * @throws IOException if file operations fail
 */
@Test
@Tag("normal")
@DisplayName("should return directory when custom location provided")
void shouldReturnResolvedDirectory_whenCustomLocationProvided(final @TempDir Path tempDir)
    throws IOException {
  // ...
}
```

### Basic Test Method Javadoc

For tests without parameters or exceptions:

```java
/** Verifies that constructor creates instance when called. */
@Test
@Tag("normal")
@DisplayName("should create instance when called")
void shouldCreateInstance_whenCalled() {
  // ...
}
```

---

## Import Statements

### Static Imports

Use static imports for assertions and mocking:

```java
// JUnit assertions
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Mockito
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
```

### Standard Imports

```java
// JUnit
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
```

---

## Complete Example

The following example demonstrates all key principles and patterns:

```java
package io.github.seijikohara.dbtester.internal.loader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link DataProcessor}. */
@DisplayName("DataProcessor")
class DataProcessorTest {

  /** Tests for the DataProcessor class. */
  DataProcessorTest() {}

  /** The processor instance under test. */
  private DataProcessor processor;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    processor = new DataProcessor();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when called. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new DataProcessor();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the process() method. */
  @Nested
  @DisplayName("process(Path) method")
  class ProcessMethod {

    /** Tests for the process method. */
    ProcessMethod() {}

    /**
     * Verifies that process returns result when valid path provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return result when valid path provided")
    void shouldReturnResult_whenValidPathProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createDataFile(tempDir, "data.csv", "COL1,COL2", "A,B");

      // When
      final var result = processor.process(tempDir);

      // Then
      assertAll(
          "result should contain processed data",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.getItems().size(), "should have one item"));
    }

    /**
     * Verifies that process throws exception when directory does not exist.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when directory does not exist")
    void shouldThrowException_whenDirectoryDoesNotExist(final @TempDir Path tempDir) {
      // Given
      final var nonExistentDir = tempDir.resolve("nonexistent");

      // When & Then
      final var exception = assertThrows(
          DataSetLoadException.class,
          () -> processor.process(nonExistentDir));

      assertTrue(
          exception.getMessage().contains("does not exist"),
          "exception should mention directory does not exist");
    }
  }

  /**
   * Creates a data file with the specified content.
   *
   * @param dir the directory to create the file in
   * @param fileName the file name
   * @param lines the data lines
   * @throws IOException if file creation fails
   */
  private static void createDataFile(final Path dir, final String fileName, final String... lines)
      throws IOException {
    final var content = String.join("\n", lines);
    Files.writeString(dir.resolve(fileName), content);
  }
}
```

---

## Null Safety in Tests

### JSpecify and @NullMarked

Test code follows the same null safety principles as production code, with some specific considerations:

### Test Package Configuration

Test packages within library modules (e.g., `db-tester-core`) must include `@NullMarked` in their `package-info.java`:

```java
/** Unit tests for the loader package. */
@NullMarked
package io.github.seijikohara.dbtester.internal.loader;

import org.jspecify.annotations.NullMarked;
```

### Example/Sample Project Configuration

Example or sample projects (e.g., `examples/db-tester-example-junit`) also require `@NullMarked`:

- All Java packages in the project, including examples, must have `package-info.java` with `@NullMarked`
- This ensures consistent null safety across the entire codebase
- The `verifyNullMarkedPackages` Gradle task enforces this for all modules

```java
/**
 * Feature demonstration and integration tests.
 *
 * <p>This package contains comprehensive examples demonstrating all framework features using H2
 * in-memory database.
 */
@NullMarked
package example.feature;

import org.jspecify.annotations.NullMarked;
```

### Test Code Null Handling

In test code, null values are often intentionally used for testing edge cases:

```java
@Test
@Tag("edge-case")
@DisplayName("should handle null input gracefully")
void shouldHandleNullInput_whenNullProvided() {
  // Given
  final String input = null;

  // When & Then
  assertThrows(
      NullPointerException.class,
      () -> target.process(input));
}
```

### Mockito and Null Safety

Mock return values may be null by default for methods not stubbed:

```java
// Mock returns null by default for methods not stubbed
final var mockService = mock(Service.class);
// when(mockService.getValue()).thenReturn(null); // implicit

// Explicit stubbing is required when null behavior matters
when(mockService.getValue()).thenReturn(new Value("test"));
```
