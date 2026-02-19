---
title: "Spring Boot Integration - DB Tester"
description: "Integrate DB Tester with Spring Boot using auto-configuration starters for JUnit, Spock, and Kotest."
---

# Spring Boot Integration

## Dependencies

::: code-group

```kotlin [JUnit]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")
}
```

```groovy [Spock]
dependencies {
    testImplementation platform("io.github.seijikohara:db-tester-bom:VERSION")
    testImplementation "io.github.seijikohara:db-tester-spock-spring-boot-starter"
}
```

```kotlin [Kotest]
dependencies {
    testImplementation(platform("io.github.seijikohara:db-tester-bom:VERSION"))
    testImplementation("io.github.seijikohara:db-tester-kotest-spring-boot-starter")
}
```

:::

## JUnit Spring Boot Starter

**Module**: `db-tester-junit-spring-boot-starter`

**Extension**: `SpringBootDatabaseTestExtension`

### Automatic DataSource Discovery

The Spring Boot extension performs these steps automatically:
1. Detects the Spring `ApplicationContext`
2. Finds `DataSource` beans
3. Resolves the default `DataSource` using the following priority:
   1. Single `DataSource` bean (automatic default)
   2. `@Primary`-annotated `DataSource`
   3. `DataSource` bean named `"dataSource"`
4. Registers all beans with `DataSourceRegistry`

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

## Configuration Properties

Configure via `application.properties` or `application.yml`:

```properties
# Enable or disable DB Tester (default: true)
db-tester.enabled=true

# Auto-register DataSource beans (default: true)
db-tester.auto-register-data-sources=true

# Data format (AUTO, CSV, TSV, JSON, or YAML)
db-tester.convention.data-format=AUTO

# Expectation directory suffix
db-tester.convention.expectation-suffix=/expected

# Scenario marker column name
db-tester.convention.scenario-marker=[Scenario]

# Table merge strategy (FIRST, LAST, UNION, UNION_ALL)
db-tester.convention.table-merge-strategy=UNION_ALL

# Default preparation operation
db-tester.operation.preparation=CLEAN_INSERT

# Default expectation operation (typically NONE for verification only)
db-tester.operation.expectation=NONE
```

Property names use singular form (`convention`, `operation`).

## Spock Spring Boot Starter

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

## Kotest Spring Boot Starter

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

## Auto-Configuration

Auto-configuration classes:

| Module | Auto-Configuration Class |
|--------|-------------------------|
| JUnit Starter | `DbTesterJUnitAutoConfiguration` |
| Spock Starter | `DbTesterSpockAutoConfiguration` |
| Kotest Starter | `DbTesterKotestAutoConfiguration` |

## Related Specifications

- [Test Frameworks Overview](test-frameworks) - Supported frameworks summary
- [JUnit](junit) - JUnit integration
- [Spock](spock) - Spock integration
- [Kotest](kotest) - Kotest integration
- [Lifecycle](lifecycle) - Lifecycle hooks and executor classes
- [Configuration](configuration) - Configuration options
