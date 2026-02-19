---
title: "JUnit Integration - DB Tester"
description: "Integrate DB Tester with JUnit using DatabaseTestExtension for annotation-driven database testing."
---

# JUnit Integration

## Module

`db-tester-junit`

## Extension Class

**Location**: `io.github.seijikohara.dbtester.junit.jupiter.extension.DatabaseTestExtension`

**Implemented Interfaces**:
- `BeforeEachCallback` - DataSet phase execution
- `AfterEachCallback` - ExpectedDataSet phase verification
- `ParameterResolver` - `ExtensionContext` injection

## Registration

**Recommended** — Use the `@DatabaseTest` composed annotation:

```java
@DatabaseTest
class UserRepositoryTest {
    // ...
}
```

`@DatabaseTest` is equivalent to `@ExtendWith(DatabaseTestExtension.class)` with less boilerplate.

**Alternative** — Register the extension directly:

```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {
    // ...
}
```

## DataSource Registration

Register data sources in `@BeforeAll`:

```java
@DatabaseTest
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

## Configuration Customization

```java
@BeforeAll
static void setup(ExtensionContext context) {
    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(dataSource);

    var config = Configuration.builder()
        .conventions(ConventionSettings.builder()
            .dataFormat(DataFormat.TSV)
            .build())
        .build();
    DatabaseTestExtension.setConfiguration(context, config);
}
```

## Static Methods

| Method | Description |
|--------|-------------|
| `getRegistry(ExtensionContext)` | Returns or creates DataSourceRegistry |
| `setConfiguration(ExtensionContext, Configuration)` | Sets custom configuration |

## Nested Test Classes

The extension shares state across nested test classes:

```java
@DatabaseTest
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

## Annotation Precedence

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

## Test Failure Behavior

When a test method throws an exception, `@ExpectedDataSet` verification is skipped. The database state is unpredictable after a test failure, so verification results would be misleading. This behavior is consistent across all framework integrations (JUnit, Spock, Kotest).

## Multiple DataSource Usage

Register multiple data sources to test across databases:

```java
@BeforeAll
static void setUp(ExtensionContext context) throws SQLException {
    var primaryDs = createDataSource("jdbc:h2:mem:primary;DB_CLOSE_DELAY=-1");
    var secondaryDs = createDataSource("jdbc:h2:mem:secondary;DB_CLOSE_DELAY=-1");

    var registry = DatabaseTestExtension.getRegistry(context);
    registry.registerDefault(primaryDs);
    registry.register("secondary", secondaryDs);
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

## Related Specifications

- [Test Frameworks Overview](test-frameworks) - Supported frameworks summary
- [Spock](spock) - Spock integration
- [Kotest](kotest) - Kotest integration
- [Spring Boot](spring-boot) - Spring Boot auto-configuration
- [Lifecycle](lifecycle) - Lifecycle hooks and executor classes
- [Annotations](annotations) - Annotation details
- [Configuration](configuration) - Configuration options
