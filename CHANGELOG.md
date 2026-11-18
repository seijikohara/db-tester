# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). See the
[versioning policy](docs/specs/versioning.md) for the definition of a breaking change and the support
guarantees for each API tier.

## [Unreleased]

## [0.9.0] - 2026-11-18

### Changed

- **Breaking:** Unified the comparison-strategy surface. The `Strategy` enum moved from
  `api.annotation` to `api.domain`, `ComparisonStrategy.Type` was removed in favor of `Strategy`,
  and `ComparisonStrategy.getType()`/`getPattern()` became `type()`/`pattern()`. Use
  `ComparisonStrategy.of(Strategy[, String])` to build instances.
- **Breaking:** Collapsed the operation and expectation SPI telescoping overloads to one canonical
  method each. `OperationProvider.execute(...)` now always takes `batchSize`,
  `ExpectationProvider.verifyExpectation(...)` always takes `ExpectationContext`, and the four-arg
  `ExpectationContext.of(...)` was removed.
- **Breaking:** A data mismatch from the `@ExpectedDataSet` annotation path now throws
  `java.lang.AssertionError` (carrying the structured diff) instead of `ValidationException`.
  `ValidationException` now signals only that verification could not complete (parse or strategy
  failure).
- **Breaking:** `db-tester-spock` and `db-tester-spock-spring-boot-starter` now require Groovy 6 and
  Spock 2.5 (`2.5-groovy-6.0`) instead of Groovy 5 and Spock 2.4 (`2.4-groovy-5.0`). The Spring Boot
  BOM still manages Groovy 5, so builds that apply it must override the managed `groovy.version`. See
  [Spring Boot Integration](docs/specs/spring-boot.md#groovy-version-with-the-spring-boot-bom).
  Groovy 6 moved the classic call-site runtime into `org.apache.groovy:groovy-callsite`. Libraries
  compiled for that runtime, such as `testcontainers-spock` 2.0.x, need it on the test runtime
  classpath.
- **Breaking:** Renamed the dataset accessors to record-style names. `Table.getName()`,
  `getColumns()`, `getRows()`, and `getRowCount()` became `name()`, `columns()`, `rows()`, and
  `rowCount()`. `Row.getValues()` and `getValue(ColumnName)` became `values()` and
  `value(ColumnName)`. `TableSet.getTables()`, `getTable(TableName)`, and `getDataSource()` became
  `tables()`, `table(TableName)`, and `dataSource()`.
- **Breaking:** Renamed the SPI and enum accessors to record-style names.
  `DataSetLoaderProvider.getLoader()` became `loader()`. `TypeHandler.getJavaType()`,
  `getSqlTypes()`, `getSupportedDatabases()`, and `getPriority()` became `javaType()`, `sqlTypes()`,
  `supportedDatabases()`, and `priority()`. `DataFormat.getExtension()` became `extension()`.
- **Breaking:** Renamed `PreparationConfig.tableOrderingStrategy()` to `tableOrdering()` and
  `withTableOrderingStrategy(...)` to `withTableOrdering(...)`, matching `@DataSet`,
  `@ExpectedDataSet`, and `ExpectationContext`.
- **Breaking:** Moved the shared Spring Boot starter wiring into `db-tester-spring-support`.
  `DbTesterProperties` and `DataSourceRegistrar` moved from each starter's `autoconfigure` package
  to `io.github.seijikohara.dbtester.spring.support`. The `db-tester` property prefix and property
  names are unchanged. The Spring configuration metadata now ships in the `db-tester-spring-support`
  jar. `db-tester-spring-support` now has a runtime dependency on `spring-boot-autoconfigure`, so
  projects that use the module without Spring Boot now receive Spring Boot transitively.
- **Breaking:** `db-tester-kotest` and `db-tester-kotest-spring-boot-starter` are now compiled with
  Kotlin 2.4 (2.4.21) instead of Kotlin 2.3 (2.3.21), and their POMs declare `kotlin-stdlib`
  2.4.21. Kotlin 2.2 and earlier compilers cannot read the Kotlin 2.4 class metadata.
- Upgraded the dependency versions that the published POMs declare: `spring-boot-autoconfigure`
  4.0.6 to 4.1.2, `spring-context` 7.0.7 to 7.0.10, `kotest-bom` 6.1.11 to 6.2.7, `junit-bom` 6.1.0
  to 6.1.3, `jackson-bom` 2.21.3 to 2.23.0, and `jspecify` 1.0.0 to 1.0.1. The Spring Boot starters
  are now built against Spring Boot 4.1.

### Added

- `@SpringBootDatabaseTest` activation annotation for the JUnit and Kotest Spring Boot starters,
  matching the existing Spock starter annotation.
- Functional `TypeHandler` SPI: custom type handlers are now consulted on database read and write,
  keyed by SQL type and database product. The built-in UUID, JSON, and ARRAY handlers are active for
  their declared databases.

### Removed

- **Breaking:** The unused `options` attribute on `@ColumnStrategy` and the corresponding
  `ComparisonStrategy` surface.
- **Breaking:** The redundant `ExecutionSettings.of(Duration, TransactionMode)` factory. Use
  `ExecutionSettings.builder()`.
- **Breaking:** The unused `Cell`, `Column`, and `ColumnMetadata` types from `api.domain`.
- **Breaking:** The `dbTesterDataSourceRegistry` bean that each Spring Boot starter registered. The
  Spock and Kotest starters no longer read a `DataSourceRegistry` bean of that name from the
  application context. All three starters now build the registry through `DataSourceRegistrar` and
  resolve the default `DataSource` the same way: the only `DataSource`, then the `@Primary` one,
  then the bean named `dataSource`. The Spock and Kotest starters no longer fall back to the first
  `DataSource` bean.
- **Breaking:** The public `DataSourceRegistrarSupport` members `DEFAULT_DATASOURCE_BEAN_NAME`,
  `resolveDefaultDataSource(...)`, `findPrimaryDataSource(...)`, and `findDataSourceByName(...)` in
  `db-tester-spring-support`. They are no longer public.
- Dead and orphan code: a duplicate `JsonNormalizer`, unused registry accessors, and inert build
  tooling.

## Released

Detailed release notes for published versions are available on the
[GitHub Releases page](https://github.com/seijikohara/db-tester/releases): v0.8.1, v0.8.0, v0.7.1,
v0.7.0, v0.6.0, v0.5.0, v0.4.0, v0.3.0, v0.2.0, v0.1.0, and v0.0.1.

[Unreleased]: https://github.com/seijikohara/db-tester/compare/v0.9.0...HEAD
[0.9.0]: https://github.com/seijikohara/db-tester/compare/v0.8.1...v0.9.0
