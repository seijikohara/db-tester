# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project
adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). See the
[versioning policy](docs/specs/versioning.md) for the definition of a breaking change and the support
guarantees for each API tier.

## [Unreleased]

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
- Dead and orphan code: a duplicate `JsonNormalizer`, unused registry accessors, and inert build
  tooling.

## Released

Detailed release notes for published versions are available on the
[GitHub Releases page](https://github.com/seijikohara/db-tester/releases): v0.8.1, v0.8.0, v0.7.1,
v0.7.0, v0.6.0, v0.5.0, v0.4.0, v0.3.0, v0.2.0, v0.1.0, and v0.0.1.

[Unreleased]: https://github.com/seijikohara/db-tester/compare/v0.8.1...HEAD
