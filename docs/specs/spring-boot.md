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

## Automatic DataSource Discovery

All three starters share the same auto-configuration behavior via `db-tester-spring-support`:

1. Detect the Spring `ApplicationContext`
2. Find `DataSource` beans
3. Resolve the default `DataSource` using the following priority:
   1. Single `DataSource` bean (automatic default)
   2. `@Primary`-annotated `DataSource`
   3. `DataSource` bean named `"dataSource"`
4. Register all beans with `DataSourceRegistry`

Manual DataSource registration is not required. The starter handles registration automatically.

## Extension Registration

::: code-group

Each framework provides a `@SpringBootDatabaseTest` annotation that registers the extension and
discovers the DataSource from the Spring context.

::: code-group

```java [JUnit]
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositoryTest {

    @Test
    @DataSet
    @ExpectedDataSet
    void testCreateUser() {
        // DataSource automatically registered from Spring context
    }
}
```

```groovy [Spock]
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

```kotlin [Kotest]
@SpringBootTest
@SpringBootDatabaseTest
class UserRepositorySpec : AnnotationSpec() {

    @Test
    @DataSet
    @ExpectedDataSet
    fun `should create user`() {
        // DataSource automatically registered from Spring context
    }
}
```

:::

The `@SpringBootDatabaseTest` annotation is the recommended activation for all three frameworks. The
underlying `SpringBootDatabaseTestExtension` remains available for manual registration when a
framework-native idiom is preferred (`@ExtendWith(SpringBootDatabaseTestExtension.class)` for JUnit,
`extensions(SpringBootDatabaseTestExtension())` for Kotest).

| Framework | Recommended Activation | Manual Alternative |
|-----------|------------------------|--------------------|
| JUnit | `@SpringBootDatabaseTest` | `@ExtendWith(SpringBootDatabaseTestExtension.class)` |
| Spock | `@SpringBootDatabaseTest` | -- |
| Kotest | `@SpringBootDatabaseTest` | `extensions(SpringBootDatabaseTestExtension())` |

## Multiple DataSources

For multiple data sources, define beans with `@Primary` and `@Qualifier`:

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

Reference named data sources in annotations:

```java
@Test
@DataSet(sources = {
    @DataSetSource(dataSourceName = ""),          // Primary (default)
    @DataSetSource(dataSourceName = "secondary")  // Secondary
})
void testMultipleDatabases() { }
```

## Configuration Properties

Configure via `application.properties` or `application.yml`.
All properties apply to all three starters.

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

## Auto-Configuration Classes

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
