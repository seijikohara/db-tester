/**
 * DB Tester JUnit module providing JUnit Jupiter extension for database testing.
 *
 * <p>This module provides the {@code @ExtendWith(DatabaseTestExtension.class)} extension for JUnit
 * 5 tests, enabling declarative database setup and verification using {@code @Preparation} and
 * {@code @Expectation} annotations.
 */
module io.github.seijikohara.dbtester.junit {
  // Public API exports
  exports io.github.seijikohara.dbtester.junit.jupiter.extension;
  exports io.github.seijikohara.dbtester.junit.jupiter.lifecycle;
  exports io.github.seijikohara.dbtester.junit.jupiter.spi;

  // Required dependencies (only API, not core - core is loaded at runtime via SPI)
  requires transitive io.github.seijikohara.dbtester.api;
  requires transitive org.junit.jupiter.api;
  requires java.sql;
  requires org.jspecify;
  requires static org.slf4j;

  // SPI service providers
  provides io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver with
      io.github.seijikohara.dbtester.junit.jupiter.spi.JUnitScenarioNameResolver;
}
