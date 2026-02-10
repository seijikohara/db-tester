/**
 * DB Tester API module providing public interfaces for database testing.
 *
 * <p>This module exports packages in three categories:
 *
 * <h2>User API</h2>
 *
 * <p>Packages intended for test authors writing database tests. These packages form the stable,
 * primary API surface and follow semantic versioning for backward compatibility.
 *
 * <ul>
 *   <li>{@code annotation} — Declarative test annotations ({@code @DataSet},
 *       {@code @ExpectedDataSet}, {@code @DataSetSource})
 *   <li>{@code assertion} — Programmatic database assertion utilities
 *   <li>{@code config} — Configuration types ({@code Configuration}, {@code DataSourceRegistry})
 *   <li>{@code exception} — Framework exception hierarchy
 *   <li>{@code export} — Data export API for writing database content to files
 *   <li>{@code operation} — Database operation enumerations ({@code Operation}, {@code
 *       TableOrderingStrategy})
 *   <li>{@code preparation} — Programmatic test data preparation facade
 * </ul>
 *
 * <h2>Extension API</h2>
 *
 * <p>Packages intended for framework integrators building custom testing extensions (JUnit, Spock,
 * Kotest, or custom frameworks). These packages are stable but target advanced users.
 *
 * <ul>
 *   <li>{@code context} — Test context abstraction for framework-agnostic test execution
 *   <li>{@code loader} — Dataset loading SPI
 *   <li>{@code scenario} — Scenario name resolution for test framework integration
 *   <li>{@code spi} — Service Provider Interfaces for core extensibility
 * </ul>
 *
 * <h2>Domain Types</h2>
 *
 * <p>Shared value objects and data structures used across both User API and Extension API. These
 * types appear in method signatures throughout the framework.
 *
 * <ul>
 *   <li>{@code dataset} — Format-agnostic dataset abstractions ({@code TableSet}, {@code Table},
 *       {@code Row})
 *   <li>{@code domain} — Type-safe value objects for database identifiers and metadata
 * </ul>
 */
module io.github.seijikohara.dbtester.api {
  // Required modules (transitive because DataSource and @Nullable are part of our public API)
  requires transitive java.sql;
  requires transitive org.jspecify;
  requires static org.slf4j;

  // --- User API: packages for test authors ---
  exports io.github.seijikohara.dbtester.api.annotation;
  exports io.github.seijikohara.dbtester.api.assertion;
  exports io.github.seijikohara.dbtester.api.config;
  exports io.github.seijikohara.dbtester.api.exception;
  exports io.github.seijikohara.dbtester.api.export;
  exports io.github.seijikohara.dbtester.api.operation;
  exports io.github.seijikohara.dbtester.api.preparation;

  // --- Extension API: packages for framework integrators ---
  exports io.github.seijikohara.dbtester.api.context;
  exports io.github.seijikohara.dbtester.api.loader;
  exports io.github.seijikohara.dbtester.api.scenario;
  exports io.github.seijikohara.dbtester.api.spi;

  // --- Domain types: shared value objects ---
  exports io.github.seijikohara.dbtester.api.dataset;
  exports io.github.seijikohara.dbtester.api.domain;

  // SPI for implementations
  uses io.github.seijikohara.dbtester.api.spi.AssertionProvider;
  uses io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
  uses io.github.seijikohara.dbtester.api.spi.ExportProvider;
  uses io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
  uses io.github.seijikohara.dbtester.api.spi.ExpectationSupport;
  uses io.github.seijikohara.dbtester.api.spi.OperationProvider;
  uses io.github.seijikohara.dbtester.api.spi.PreparationSupport;
  uses io.github.seijikohara.dbtester.api.spi.TypeHandler;
  uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
}
