/**
 * DB Tester Core module providing internal implementation for database testing.
 *
 * <p>This module provides SPI implementations for the API module. Internal packages are not
 * exported to end users; they are only accessible within this module and via the SPI mechanism.
 */
module io.github.seijikohara.dbtester.core {
  // API module dependency
  requires transitive io.github.seijikohara.dbtester.api;

  // Required dependencies
  requires transitive java.sql;
  requires transitive org.jspecify;
  requires static org.slf4j;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.dataformat.csv;
  requires com.fasterxml.jackson.dataformat.yaml;

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
