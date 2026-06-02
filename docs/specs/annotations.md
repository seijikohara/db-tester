---
title: "Annotations - DB Tester"
description: "Reference for DB Tester annotations: @DataSet, @ExpectedDataSet, @DataSetSource, @ColumnStrategy, Strategy, and RowOrdering."
---

# Annotations

## API Layers

The `db-tester-api` module exports packages organized into three layers by intended audience:

| Layer | Packages | Audience | Stability |
|-------|----------|----------|-----------|
| **User API** | `annotation`, `config`, `operation`, `exception`, `preparation` | All users | Stable |
| **Advanced API** | `assertion`, `export`, `domain`, `dataset` | Users with programmatic needs | Stable |
| **Extension SPI** | `spi`, `loader`, `context`, `scenario` | Framework integrators | Evolving SPI |

### User API

The User API contains the types that most users interact with directly:

- **`annotation`** -- `@DataSet`, `@ExpectedDataSet`, `@DataSetSource`, `@ColumnStrategy`
- **`config`** -- `Configuration`, `ConventionSettings`, `DataSourceRegistry`, `ExpectationContext`
- **`operation`** -- `Operation` enum (`CLEAN_INSERT`, `INSERT`, `TRUNCATE_INSERT`, and others)
- **`exception`** -- Framework exceptions (passive consumption via catch/inspect)
- **`preparation`** -- `DatabasePreparation` for programmatic test data setup

### Advanced API

The Advanced API provides programmatic access to assertions, datasets, and type-safe value objects:

- **`assertion`** -- `DatabaseAssertion`, `DatabaseQueryAssertion` for programmatic database verification
- **`export`** -- `DataSetExporter` for exporting database content to files
- **`domain`** -- Type-safe value objects (`CellValue`, `TableName`, `ColumnName`, `ComparisonStrategy`)
- **`dataset`** -- `TableSet`, `Table`, `Row` interfaces for dataset representation

### Extension SPI

The Extension SPI targets framework integrators who build custom test extensions or data loaders:

- **`spi`** -- `OperationProvider`, `ExpectationProvider`, `DataSetLoaderProvider`
- **`loader`** -- `DataSetLoader`, `ExpectedTableSet` for custom data loading
- **`context`** -- `TestContext` for framework-agnostic test execution
- **`scenario`** -- `ScenarioNameResolver` for custom scenario name resolution

## @DataSet

Declares the datasets to apply before a test method executes.

**Location**: `io.github.seijikohara.dbtester.api.annotation.DataSet`

**Target**: `METHOD`, `TYPE`, `ANNOTATION_TYPE`

**Attributes**:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `sources` | `DataSetSource[]` | `{}` | Dataset sources to execute; empty triggers convention-based discovery |
| `operation` | `Operation` | `CLEAN_INSERT` | Database operation to apply |
| `tableOrdering` | `TableOrderingStrategy` | `AUTO` | Strategy for determining table processing order |
| `batchSize` | `int` | `-1` | Rows per batch for INSERT operations; `-1` uses global setting, `0` uses single batch |

**Annotation Inheritance**:

- Class-level annotations are inherited by subclasses
- Method-level annotations override class-level declarations
- Annotated with `@Inherited`

**Example**:

```java
@DataSet
void testMethod() { }

@DataSet(operation = Operation.INSERT)
void testWithInsertOnly() { }

@DataSet(tableOrdering = TableOrderingStrategy.FOREIGN_KEY)
void testWithForeignKeyOrdering() { }

@DataSet(sources = @DataSetSource(resourceLocation = "custom/path"))
void testWithCustomPath() { }
```

## @ExpectedDataSet

Declares the datasets that define the expected database state after test execution.

**Location**: `io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet`

**Target**: `METHOD`, `TYPE`, `ANNOTATION_TYPE`

**Attributes**:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `sources` | `DataSetSource[]` | `{}` | Dataset sources for verification; empty triggers convention-based discovery |
| `tableOrdering` | `TableOrderingStrategy` | `AUTO` | Strategy for determining table processing order during verification |
| `rowOrdering` | `RowOrdering` | `UNSET` | Row comparison strategy; `UNSET` defers to global `VerificationSettings` |
| `retryCount` | `int` | `-1` | Retry attempts for verification; `-1` uses global setting |
| `retryDelayMillis` | `long` | `-1` | Delay between retries in milliseconds; `-1` uses global setting |

**Verification Behavior**:

- Read-only comparison (no data modification)
- Validates actual database state against expected datasets
- Reports assertion failures via test framework

**Example**:

```java
@DataSet
@ExpectedDataSet
void testWithVerification() { }

@ExpectedDataSet(sources = @DataSetSource(resourceLocation = "expected/custom"))
void testWithCustomExpectation() { }

@ExpectedDataSet(tableOrdering = TableOrderingStrategy.ALPHABETICAL)
void testWithAlphabeticalOrdering() { }

@ExpectedDataSet(rowOrdering = RowOrdering.UNORDERED)
void testWithUnorderedComparison() { }

@ExpectedDataSet(retryCount = 3, retryDelayMillis = 500)
void testWithRetry() { }
```

## @DataSetSource

Configures individual dataset source parameters within `@DataSet` or `@ExpectedDataSet`.

**Location**: `io.github.seijikohara.dbtester.api.annotation.DataSetSource`

**Target**: None (`@Target({})`) - This annotation cannot apply directly to classes or methods.
Use it exclusively within `@DataSet#sources()` and `@ExpectedDataSet#sources()` arrays.

**Attributes**:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `resourceLocation` | `String` | `""` | Dataset directory path; empty uses convention-based discovery |
| `dataSourceName` | `String` | `""` | Named DataSource identifier; empty uses default |
| `scenarioNames` | `String[]` | `{}` | Scenario filters; empty uses test method name |
| `excludeColumns` | `String[]` | `{}` | Column names to exclude from verification (case-insensitive); effective only in `@ExpectedDataSet` |
| `columnStrategies` | `ColumnStrategy[]` | `{}` | Column-specific comparison strategies; effective only in `@ExpectedDataSet` |

**Resource Location Formats**:

| Format | Example | Resolution |
|--------|---------|------------|
| Classpath relative | `data/users` | From test classpath root |
| Classpath prefix | `classpath:data/users` | Explicit classpath resolution |
| Absolute path | `/tmp/testdata` | File system absolute path |
| Empty string | `""` | Convention-based discovery |

**Example**:

```java
@DataSet(sources = {
    @DataSetSource(dataSourceName = "primary"),
    @DataSetSource(dataSourceName = "secondary", resourceLocation = "secondary-data")
})
void testMultipleDataSources() { }

@DataSet(sources = @DataSetSource(scenarioNames = {"scenario1", "scenario2"}))
void testMultipleScenarios() { }

@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT", "VERSION"}
))
void testWithExcludedColumns() { }

@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
        @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
        @ColumnStrategy(name = "ID", strategy = Strategy.REGEX, pattern = "[a-f0-9-]{36}")
    }
))
void testWithColumnStrategies() { }
```

**Column Exclusion Behavior**:

- Column names normalize to uppercase for comparison
- Per-dataset exclusions combine with global exclusions from `VerificationSettings`
- Exclusions apply only to `@ExpectedDataSet` verification, not `@DataSet` preparation

**Column Strategy Behavior**:

- Column strategies override the default strict comparison for specific columns
- Annotation-level strategies override global strategies from `VerificationSettings`
- Exclusions take precedence: excluded columns are skipped before strategies apply

## @ColumnStrategy

Configures the comparison strategy for a column during expectation verification.

**Location**: `io.github.seijikohara.dbtester.api.annotation.ColumnStrategy`

**Target**: None (`@Target({})`) - Use exclusively within `@DataSetSource#columnStrategies()`.

**Attributes**:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `name` | `String` | (required) | Column name (case-insensitive) |
| `strategy` | `Strategy` | `STRICT` | Comparison strategy to use |
| `pattern` | `String` | `""` | Regex pattern for `REGEX` strategy |

## Strategy

Enum defining comparison strategy types for use in `@ColumnStrategy` annotations.

**Location**: `io.github.seijikohara.dbtester.api.domain.Strategy`

**Values**:

| Value | Description | Required Attribute |
|-------|-------------|-------------------|
| `STRICT` | Exact match using `equals()` (default) | -- |
| `IGNORE` | Skip comparison entirely | -- |
| `NUMERIC` | Type-aware numeric comparison | -- |
| `CASE_INSENSITIVE` | Case-insensitive string comparison | -- |
| `TIMESTAMP_FLEXIBLE` | Converts to UTC and ignores sub-second precision | -- |
| `NOT_NULL` | Verifies value is not null | -- |
| `REGEX` | Pattern matching using regular expressions | `pattern` |
| `DATE_FLEXIBLE` | Multi-format date comparison (ISO-8601, slashed, dot) | -- |
| `JSON_EQUIVALENT` | JSON structural comparison (ignores key order and whitespace) | -- |

**Examples**:

```java
@ExpectedDataSet(sources = @DataSetSource(
    columnStrategies = {
        @ColumnStrategy(name = "BIRTH_DATE", strategy = Strategy.DATE_FLEXIBLE),
        @ColumnStrategy(name = "METADATA", strategy = Strategy.JSON_EQUIVALENT)
    }
))
void testWithExtendedStrategies() { }
```

## RowOrdering

Enum defining row comparison strategies for use in `@ExpectedDataSet` annotations.

**Location**: `io.github.seijikohara.dbtester.api.config.RowOrdering`

**Values**:

| Value | Description |
|-------|-------------|
| `UNSET` | Annotation default sentinel. Defers to global `VerificationSettings.rowOrdering()`. |
| `ORDERED` | Positional comparison (row-by-row by index). Default behavior. |
| `UNORDERED` | Set-based comparison (rows matched regardless of position). |

`UNSET` is the default for `@ExpectedDataSet.rowOrdering()`.
When set, the global setting from `VerificationSettings` applies.

**When to Use**:

| Mode | Use Case |
|------|----------|
| `ORDERED` | Query includes ORDER BY; row order is significant; maximum performance |
| `UNORDERED` | No ORDER BY; row order not significant; database may return rows in unpredictable order |

**Performance Note**: Unordered comparison has O(n*m) complexity in the worst case.

## Column Comparison Examples

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

### Strategy Precedence

The framework applies strategies in this order:

1. `excludeColumns` -- Columns listed here are excluded from comparison
2. `columnStrategies` -- Per-column strategies override the default
3. `STRICT` -- Default comparison for columns without an explicit strategy

## Composed Meta-Annotations

Both `@DataSet` and `@ExpectedDataSet` include `ANNOTATION_TYPE` in their `@Target`,
which allows placing them on other annotations. This supports composing reusable
meta-annotations that encapsulate common dataset configurations. The framework's
`AnnotationUtils` discovers these annotations through recursive meta-annotation
traversal with cycle detection.

### Defining a Composed @DataSet Annotation

Create a custom annotation that wraps `@DataSet` with a fixed resource location:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@DataSet(sources = @DataSetSource(
    resourceLocation = "classpath:common/user-seed/"))
public @interface UserSeedData {}
```

Usage:

```java
@Test
@UserSeedData
@ExpectedDataSet
void shouldVerifyAfterSeeding() throws SQLException {
    // @UserSeedData loads data from classpath:common/user-seed/
}
```

### Defining a Composed @ExpectedDataSet Annotation

Create a custom annotation that wraps `@ExpectedDataSet` with column exclusions:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT", "UPDATED_AT"}))
public @interface VerifyIgnoringAuditColumns {}
```

### Two-Level Composition

Combine both composed annotations into a single annotation:

```java
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@UserSeedData
@VerifyIgnoringAuditColumns
public @interface UserDataTest {}
```

Usage:

```java
@Test
@UserDataTest  // Combines @UserSeedData + @VerifyIgnoringAuditColumns
void shouldSeedAndVerify() throws SQLException {
    // Framework traverses two levels of meta-annotation hierarchy
}
```

### Framework-Specific Syntax

**Groovy (Spock):**

```groovy
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE])
@Retention(RetentionPolicy.RUNTIME)
@DataSet(sources = @DataSetSource(
    resourceLocation = 'classpath:common/user-seed/'))
@interface UserSeedData {}
```

**Kotlin (Kotest):**

```kotlin
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.ANNOTATION_CLASS)
@Retention(AnnotationRetention.RUNTIME)
@DataSet(sources = [DataSetSource(
    resourceLocation = "classpath:common/user-seed/")])
annotation class UserSeedData
```

## Related Specifications

- [API Overview](public-api) - API layers and module organization
- [Dataset Interfaces](dataset-interfaces) - TableSet, Table, Row, and value objects
- [Programmatic API](assertion-api) - DatabaseAssertion, Export, and Preparation APIs
- [Configuration](configuration) - Configuration classes
- [Data Formats](data-formats) - Supported data file formats
