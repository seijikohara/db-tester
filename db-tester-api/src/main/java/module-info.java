/**
 * DB Tester API module providing public interfaces for database testing.
 *
 * <p>This module contains the public API that users interact with when using the db-tester library.
 * It defines annotations, configuration types, domain objects, and SPI interfaces that are
 * implemented by the core module.
 */
module io.github.seijikohara.dbtester.api {
  // Required modules (transitive because DataSource and @Nullable are part of our public API)
  requires transitive java.sql;
  requires transitive org.jspecify;

  // Export public API packages
  exports io.github.seijikohara.dbtester.api.annotation;
  exports io.github.seijikohara.dbtester.api.assertion;
  exports io.github.seijikohara.dbtester.api.config;
  exports io.github.seijikohara.dbtester.api.context;
  exports io.github.seijikohara.dbtester.api.dataset;
  exports io.github.seijikohara.dbtester.api.domain;
  exports io.github.seijikohara.dbtester.api.exception;
  exports io.github.seijikohara.dbtester.api.loader;
  exports io.github.seijikohara.dbtester.api.operation;
  exports io.github.seijikohara.dbtester.api.scenario;
  exports io.github.seijikohara.dbtester.api.spi;

  // SPI for implementations
  uses io.github.seijikohara.dbtester.api.spi.AssertionProvider;
  uses io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider;
  uses io.github.seijikohara.dbtester.api.spi.OperationProvider;
  uses io.github.seijikohara.dbtester.api.spi.ExpectationProvider;
  uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
}
