/**
 * DB Tester Core module providing internal implementation for database testing.
 *
 * <p>This module provides SPI implementations for the API module and exports internal packages for
 * framework integration (JUnit, Spock extensions).
 */
module io.github.seijikohara.dbtester.core {
  // API module dependency
  requires transitive io.github.seijikohara.dbtester.api;

  // Required dependencies
  requires transitive java.sql;
  requires transitive org.jspecify;
  requires transitive org.slf4j;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.csv;

  // Internal packages exported for framework integration
  exports io.github.seijikohara.dbtester.internal.assertion;
  exports io.github.seijikohara.dbtester.internal.dataset;
  exports io.github.seijikohara.dbtester.internal.domain;
  exports io.github.seijikohara.dbtester.internal.format;
  exports io.github.seijikohara.dbtester.internal.format.csv;
  exports io.github.seijikohara.dbtester.internal.format.parser;
  exports io.github.seijikohara.dbtester.internal.format.spi;
  exports io.github.seijikohara.dbtester.internal.format.tsv;
  exports io.github.seijikohara.dbtester.internal.jdbc;
  exports io.github.seijikohara.dbtester.internal.loader;
  exports io.github.seijikohara.dbtester.internal.scenario;
  exports io.github.seijikohara.dbtester.internal.spi;

  // Internal SPI uses
  uses io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
  uses io.github.seijikohara.dbtester.internal.format.spi.FormatProvider;

  // SPI implementations for API module
  provides io.github.seijikohara.dbtester.api.spi.AssertionProvider with
      io.github.seijikohara.dbtester.internal.spi.DefaultAssertionProvider;
  provides io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider with
      io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider;
  provides io.github.seijikohara.dbtester.api.spi.OperationProvider with
      io.github.seijikohara.dbtester.internal.spi.DefaultOperationProvider;
  provides io.github.seijikohara.dbtester.api.spi.ExpectationProvider with
      io.github.seijikohara.dbtester.internal.spi.DefaultExpectationProvider;

  // Internal format providers
  provides io.github.seijikohara.dbtester.internal.format.spi.FormatProvider with
      io.github.seijikohara.dbtester.internal.format.csv.CsvFormatProvider,
      io.github.seijikohara.dbtester.internal.format.tsv.TsvFormatProvider;
}
