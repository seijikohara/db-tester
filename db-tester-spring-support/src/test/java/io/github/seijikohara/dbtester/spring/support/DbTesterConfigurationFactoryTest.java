package io.github.seijikohara.dbtester.spring.support;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import io.github.seijikohara.dbtester.api.domain.Strategy;
import io.github.seijikohara.dbtester.api.operation.Operation;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DbTesterConfigurationFactory}. */
@DisplayName("DbTesterConfigurationFactory")
class DbTesterConfigurationFactoryTest {

  /** Tests for the DbTesterConfigurationFactory class. */
  DbTesterConfigurationFactoryTest() {}

  /** The properties instance for testing. */
  private DbTesterProperties properties;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    properties = new DbTesterProperties();
  }

  /** Tests for the toConfiguration method. */
  @Nested
  @DisplayName("toConfiguration(DbTesterProperties) method")
  class ToConfigurationMethod {

    /** Tests for the toConfiguration method. */
    ToConfigurationMethod() {}

    /** Verifies that toConfiguration returns a non-null Configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should return Configuration with all sections populated")
    void should_return_configuration_with_all_sections() {
      // Given & When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      assertAll(
          "configuration sections should not be null",
          () -> assertNotNull(config, "configuration should not be null"),
          () -> assertNotNull(config.conventions(), "conventions should not be null"),
          () -> assertNotNull(config.operations(), "operations should not be null"),
          () -> assertNotNull(config.verification(), "verification should not be null"),
          () -> assertNotNull(config.execution(), "execution should not be null"),
          () -> assertNotNull(config.loader(), "loader should not be null"));
    }

    /** Verifies that custom convention properties are mapped to Configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should map custom convention properties to Configuration")
    void should_map_custom_convention_properties() {
      // Given
      properties.getConvention().setBaseDirectory("/custom/base");
      properties.getConvention().setExpectationSuffix("/verify");
      properties.getConvention().setScenarioMarker("[TestCase]");
      properties.getConvention().setDataFormat(DataFormat.TSV);
      properties.getConvention().setTableMergeStrategy(TableMergeStrategy.FIRST);
      properties.getConvention().setLoadOrderFileName("custom-order.txt");

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      final var conventions = config.conventions();
      assertAll(
          "convention properties should be mapped",
          () -> assertEquals("/custom/base", conventions.baseDirectory(), "baseDirectory mismatch"),
          () ->
              assertEquals(
                  "/verify", conventions.expectationSuffix(), "expectationSuffix mismatch"),
          () -> assertEquals("[TestCase]", conventions.scenarioMarker(), "scenarioMarker mismatch"),
          () -> assertEquals(DataFormat.TSV, conventions.dataFormat(), "dataFormat mismatch"),
          () ->
              assertEquals(
                  TableMergeStrategy.FIRST,
                  conventions.tableMergeStrategy(),
                  "tableMergeStrategy mismatch"),
          () ->
              assertEquals(
                  "custom-order.txt",
                  conventions.loadOrderFileName(),
                  "loadOrderFileName mismatch"));
    }

    /** Verifies that custom verification properties are mapped to Configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should map custom verification properties to Configuration")
    void should_map_custom_verification_properties() {
      // Given
      properties.getVerification().setGlobalExcludeColumns(Set.of("created_at", "updated_at"));
      properties.getVerification().setRowOrdering(RowOrdering.UNORDERED);
      properties.getVerification().setRetryCount(3);
      properties.getVerification().setRetryDelay(Duration.ofSeconds(2));

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      final var verification = config.verification();
      assertAll(
          "verification properties should be mapped",
          () ->
              assertEquals(
                  Set.of("created_at", "updated_at"),
                  verification.globalExcludeColumns(),
                  "globalExcludeColumns mismatch"),
          () ->
              assertEquals(
                  RowOrdering.UNORDERED, verification.rowOrdering(), "rowOrdering mismatch"),
          () -> assertEquals(3, verification.retryCount(), "retryCount mismatch"),
          () ->
              assertEquals(
                  Duration.ofSeconds(2), verification.retryDelay(), "retryDelay mismatch"));
    }

    /** Verifies that column strategies properties are mapped to Configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should map column strategies properties to Configuration")
    void should_map_column_strategies_properties() {
      // Given
      final var timestampStrategy = new DbTesterProperties.ColumnStrategyProperty();
      timestampStrategy.setColumnName("CREATED_AT");
      timestampStrategy.setStrategy(Strategy.TIMESTAMP_FLEXIBLE);

      final var ignoreStrategy = new DbTesterProperties.ColumnStrategyProperty();
      ignoreStrategy.setColumnName("updated_at");
      ignoreStrategy.setStrategy(Strategy.IGNORE);

      final var regexStrategy = new DbTesterProperties.ColumnStrategyProperty();
      regexStrategy.setColumnName("EMAIL");
      regexStrategy.setStrategy(Strategy.REGEX);
      regexStrategy.setPattern("^[a-z]+@[a-z]+\\.[a-z]+$");

      properties
          .getVerification()
          .setColumnStrategies(List.of(timestampStrategy, ignoreStrategy, regexStrategy));

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      final var strategies = config.verification().globalColumnStrategies();
      assertAll(
          "column strategies should be mapped",
          () -> assertEquals(3, strategies.size(), "should have 3 strategies"),
          () ->
              assertEquals(
                  ComparisonStrategy.TIMESTAMP_FLEXIBLE,
                  Objects.requireNonNull(strategies.get("CREATED_AT")).strategy(),
                  "CREATED_AT should have TIMESTAMP_FLEXIBLE strategy"),
          () ->
              assertEquals(
                  ComparisonStrategy.IGNORE,
                  Objects.requireNonNull(strategies.get("UPDATED_AT")).strategy(),
                  "UPDATED_AT should have IGNORE strategy"),
          () ->
              assertEquals(
                  Strategy.REGEX,
                  Objects.requireNonNull(strategies.get("EMAIL")).strategy().type(),
                  "EMAIL should have REGEX strategy type"));
    }

    /** Verifies that empty column strategies produces empty map. */
    @Test
    @Tag("edge-case")
    @DisplayName("should produce empty column strategies when none configured")
    void should_produce_empty_column_strategies_when_none_configured() {
      // Given - default properties

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      assertTrue(
          config.verification().globalColumnStrategies().isEmpty(),
          "column strategies should be empty");
    }

    /** Verifies that column strategies with null column name are filtered out. */
    @Test
    @Tag("edge-case")
    @DisplayName("should filter out column strategies with null column name")
    void should_filter_out_column_strategies_with_null_column_name() {
      // Given
      final var validStrategy = new DbTesterProperties.ColumnStrategyProperty();
      validStrategy.setColumnName("CREATED_AT");
      validStrategy.setStrategy(Strategy.IGNORE);

      final var invalidStrategy = new DbTesterProperties.ColumnStrategyProperty();
      invalidStrategy.setStrategy(Strategy.STRICT);

      properties.getVerification().setColumnStrategies(List.of(validStrategy, invalidStrategy));

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      assertEquals(
          1,
          config.verification().globalColumnStrategies().size(),
          "should have only 1 valid strategy");
    }

    /** Verifies that custom execution properties are mapped to Configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should map custom execution properties to Configuration")
    void should_map_custom_execution_properties() {
      // Given
      properties.getExecution().setQueryTimeout(Duration.ofSeconds(30));
      properties.getExecution().setTransactionMode(TransactionMode.AUTO_COMMIT);

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      final var execution = config.execution();
      assertAll(
          "execution properties should be mapped",
          () ->
              assertEquals(
                  Duration.ofSeconds(30), execution.queryTimeout(), "queryTimeout mismatch"),
          () ->
              assertEquals(
                  TransactionMode.AUTO_COMMIT,
                  execution.transactionMode(),
                  "transactionMode mismatch"));
    }

    /** Verifies that custom operation properties are mapped to Configuration. */
    @Test
    @Tag("normal")
    @DisplayName("should map custom operation properties to Configuration")
    void should_map_custom_operation_properties() {
      // Given
      properties.getOperation().setPreparation(Operation.INSERT);
      properties.getOperation().setExpectation(Operation.DELETE_ALL);

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      final var operations = config.operations();
      assertAll(
          "operation properties should be mapped",
          () -> assertEquals(Operation.INSERT, operations.preparation(), "preparation mismatch"),
          () ->
              assertEquals(Operation.DELETE_ALL, operations.expectation(), "expectation mismatch"));
    }

    /** Verifies that default properties produce default Configuration values. */
    @Test
    @Tag("normal")
    @DisplayName("should produce default values for all Configuration sections")
    void should_produce_default_values_for_all_sections() {
      // Given - default properties

      // When
      final var config = DbTesterConfigurationFactory.toConfiguration(properties);

      // Then
      assertAll(
          "default Configuration values should be correct",
          () -> assertNull(config.conventions().baseDirectory(), "baseDirectory should be null"),
          () ->
              assertEquals(
                  RowOrdering.ORDERED,
                  config.verification().rowOrdering(),
                  "rowOrdering should be ORDERED"),
          () -> assertEquals(0, config.verification().retryCount(), "retryCount should be 0"),
          () -> assertNull(config.execution().queryTimeout(), "queryTimeout should be null"),
          () ->
              assertEquals(
                  TransactionMode.SINGLE_TRANSACTION,
                  config.execution().transactionMode(),
                  "transactionMode should be SINGLE_TRANSACTION"),
          () ->
              assertEquals(
                  Operation.CLEAN_INSERT,
                  config.operations().preparation(),
                  "preparation should be CLEAN_INSERT"),
          () ->
              assertEquals(
                  Operation.NONE, config.operations().expectation(), "expectation should be NONE"));
    }
  }
}
