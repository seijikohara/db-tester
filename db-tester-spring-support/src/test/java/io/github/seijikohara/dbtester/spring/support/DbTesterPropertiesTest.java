package io.github.seijikohara.dbtester.spring.support;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.operation.Operation;
import java.time.Duration;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DbTesterProperties}. */
@DisplayName("DbTesterProperties")
class DbTesterPropertiesTest {

  /** Tests for the DbTesterProperties class. */
  DbTesterPropertiesTest() {}

  /** The properties instance under test. */
  private DbTesterProperties properties;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    properties = new DbTesterProperties();
  }

  /** Tests for default values. */
  @Nested
  @DisplayName("default values")
  class DefaultValues {

    /** Tests for default values. */
    DefaultValues() {}

    /** Verifies that default values are set correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct default values")
    void should_have_correct_default_values() {
      // Given - new instance

      // When & Then
      assertAll(
          "default values should be correct",
          () -> assertTrue(properties.isEnabled(), "enabled should default to true"),
          () ->
              assertTrue(
                  properties.isAutoRegisterDataSources(),
                  "autoRegisterDataSources should default to true"));
    }
  }

  /** Tests for the enabled property. */
  @Nested
  @DisplayName("enabled property")
  class EnabledProperty {

    /** Tests for the enabled property. */
    EnabledProperty() {}

    /** Verifies that enabled can be set to false. */
    @Test
    @Tag("normal")
    @DisplayName("should allow setting enabled to false")
    void should_allow_setting_enabled_to_false() {
      // Given
      assertTrue(properties.isEnabled(), "should start as true");

      // When
      properties.setEnabled(false);

      // Then
      assertFalse(properties.isEnabled(), "should be false after setting");
    }

    /** Verifies that enabled can be toggled. */
    @Test
    @Tag("normal")
    @DisplayName("should allow toggling enabled")
    void should_allow_toggling_enabled() {
      // Given
      properties.setEnabled(false);

      // When
      properties.setEnabled(true);

      // Then
      assertTrue(properties.isEnabled(), "should be true after toggling");
    }
  }

  /** Tests for the autoRegisterDataSources property. */
  @Nested
  @DisplayName("autoRegisterDataSources property")
  class AutoRegisterDataSourcesProperty {

    /** Tests for the autoRegisterDataSources property. */
    AutoRegisterDataSourcesProperty() {}

    /** Verifies that autoRegisterDataSources can be set to false. */
    @Test
    @Tag("normal")
    @DisplayName("should allow setting autoRegisterDataSources to false")
    void should_allow_setting_auto_register_to_false() {
      // Given
      assertTrue(properties.isAutoRegisterDataSources(), "should start as true");

      // When
      properties.setAutoRegisterDataSources(false);

      // Then
      assertFalse(properties.isAutoRegisterDataSources(), "should be false after setting");
    }

    /** Verifies that autoRegisterDataSources can be toggled. */
    @Test
    @Tag("normal")
    @DisplayName("should allow toggling autoRegisterDataSources")
    void should_allow_toggling_auto_register() {
      // Given
      properties.setAutoRegisterDataSources(false);

      // When
      properties.setAutoRegisterDataSources(true);

      // Then
      assertTrue(properties.isAutoRegisterDataSources(), "should be true after toggling");
    }
  }

  /** Tests for property independence. */
  @Nested
  @DisplayName("property independence")
  class PropertyIndependence {

    /** Tests for property independence. */
    PropertyIndependence() {}

    /** Verifies that properties are independent. */
    @Test
    @Tag("normal")
    @DisplayName("should maintain independent property values")
    void should_maintain_independent_property_values() {
      // Given & When
      properties.setEnabled(false);
      properties.setAutoRegisterDataSources(true);

      // Then
      assertAll(
          "properties should be independent",
          () -> assertFalse(properties.isEnabled(), "enabled should be false"),
          () ->
              assertTrue(
                  properties.isAutoRegisterDataSources(),
                  "autoRegisterDataSources should be true"));
    }
  }

  /** Tests for the convention property. */
  @Nested
  @DisplayName("convention property")
  class ConventionProperty {

    /** Tests for the convention property. */
    ConventionProperty() {}

    /** Verifies that convention has correct default values. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct default values")
    void should_have_correct_default_values() {
      // Given & When
      final var convention = properties.getConvention();

      // Then
      assertAll(
          "convention default values should be correct",
          () -> assertNotNull(convention, "convention should not be null"),
          () -> assertNull(convention.getBaseDirectory(), "baseDirectory should default to null"),
          () ->
              assertEquals(
                  ConventionSettings.DEFAULT_EXPECTATION_SUFFIX,
                  convention.getExpectationSuffix(),
                  "expectationSuffix should default to "
                      + ConventionSettings.DEFAULT_EXPECTATION_SUFFIX),
          () ->
              assertEquals(
                  ConventionSettings.DEFAULT_SCENARIO_MARKER,
                  convention.getScenarioMarker(),
                  "scenarioMarker should default to " + ConventionSettings.DEFAULT_SCENARIO_MARKER),
          () ->
              assertEquals(
                  DataFormat.AUTO, convention.getDataFormat(), "dataFormat should default to AUTO"),
          () ->
              assertEquals(
                  TableMergeStrategy.UNION_ALL,
                  convention.getTableMergeStrategy(),
                  "tableMergeStrategy should default to UNION_ALL"),
          () ->
              assertEquals(
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                  convention.getLoadOrderFileName(),
                  "loadOrderFileName should default to "
                      + ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME));
    }

    /** Verifies that convention properties can be modified. */
    @Test
    @Tag("normal")
    @DisplayName("should allow modifying convention properties")
    void should_allow_modifying_convention_properties() {
      // Given
      final var convention = properties.getConvention();

      // When
      convention.setBaseDirectory("/custom/base");
      convention.setExpectationSuffix("/verify");
      convention.setScenarioMarker("[TestCase]");
      convention.setDataFormat(DataFormat.TSV);
      convention.setTableMergeStrategy(TableMergeStrategy.FIRST);
      convention.setLoadOrderFileName("custom-order.txt");

      // Then
      assertAll(
          "convention modified values should be correct",
          () ->
              assertEquals("/custom/base", convention.getBaseDirectory(), "baseDirectory mismatch"),
          () ->
              assertEquals(
                  "/verify", convention.getExpectationSuffix(), "expectationSuffix mismatch"),
          () ->
              assertEquals("[TestCase]", convention.getScenarioMarker(), "scenarioMarker mismatch"),
          () -> assertEquals(DataFormat.TSV, convention.getDataFormat(), "dataFormat mismatch"),
          () ->
              assertEquals(
                  TableMergeStrategy.FIRST,
                  convention.getTableMergeStrategy(),
                  "tableMergeStrategy mismatch"),
          () ->
              assertEquals(
                  "custom-order.txt",
                  convention.getLoadOrderFileName(),
                  "loadOrderFileName mismatch"));
    }

    /** Verifies that convention can be replaced. */
    @Test
    @Tag("normal")
    @DisplayName("should allow replacing convention")
    void should_allow_replacing_convention() {
      // Given
      final var newConvention = new DbTesterProperties.ConventionProperties();
      newConvention.setDataFormat(DataFormat.TSV);

      // When
      properties.setConvention(newConvention);

      // Then
      assertEquals(
          DataFormat.TSV, properties.getConvention().getDataFormat(), "dataFormat mismatch");
    }
  }

  /** Tests for the operation property. */
  @Nested
  @DisplayName("operation property")
  class OperationProperty {

    /** Tests for the operation property. */
    OperationProperty() {}

    /** Verifies that operation has correct default values. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct default values")
    void should_have_correct_default_values() {
      // Given & When
      final var operation = properties.getOperation();

      // Then
      assertAll(
          "operation default values should be correct",
          () -> assertNotNull(operation, "operation should not be null"),
          () ->
              assertEquals(
                  Operation.CLEAN_INSERT,
                  operation.getPreparation(),
                  "preparation should default to CLEAN_INSERT"),
          () ->
              assertEquals(
                  Operation.NONE,
                  operation.getExpectation(),
                  "expectation should default to NONE"));
    }

    /** Verifies that operation properties can be modified. */
    @Test
    @Tag("normal")
    @DisplayName("should allow modifying operation properties")
    void should_allow_modifying_operation_properties() {
      // Given
      final var operation = properties.getOperation();

      // When
      operation.setPreparation(Operation.INSERT);
      operation.setExpectation(Operation.DELETE_ALL);

      // Then
      assertAll(
          "operation modified values should be correct",
          () -> assertEquals(Operation.INSERT, operation.getPreparation(), "preparation mismatch"),
          () ->
              assertEquals(
                  Operation.DELETE_ALL, operation.getExpectation(), "expectation mismatch"));
    }

    /** Verifies that operation can be replaced. */
    @Test
    @Tag("normal")
    @DisplayName("should allow replacing operation")
    void should_allow_replacing_operation() {
      // Given
      final var newOperation = new DbTesterProperties.OperationProperties();
      newOperation.setPreparation(Operation.TRUNCATE_INSERT);

      // When
      properties.setOperation(newOperation);

      // Then
      assertEquals(
          Operation.TRUNCATE_INSERT,
          properties.getOperation().getPreparation(),
          "preparation mismatch");
    }
  }

  /** Tests for the verification property. */
  @Nested
  @DisplayName("verification property")
  class VerificationProperty {

    /** Tests for the verification property. */
    VerificationProperty() {}

    /** Verifies that verification has correct default values. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct default values")
    void should_have_correct_default_values() {
      // Given & When
      final var verification = properties.getVerification();

      // Then
      assertAll(
          "verification default values should be correct",
          () -> assertNotNull(verification, "verification should not be null"),
          () ->
              assertTrue(
                  verification.getGlobalExcludeColumns().isEmpty(),
                  "globalExcludeColumns should default to empty"),
          () ->
              assertEquals(
                  RowOrdering.ORDERED,
                  verification.getRowOrdering(),
                  "rowOrdering should default to ORDERED"),
          () -> assertEquals(0, verification.getRetryCount(), "retryCount should default to 0"),
          () ->
              assertEquals(
                  Duration.ofMillis(100),
                  verification.getRetryDelay(),
                  "retryDelay should default to 100ms"));
    }

    /** Verifies that verification properties can be modified. */
    @Test
    @Tag("normal")
    @DisplayName("should allow modifying verification properties")
    void should_allow_modifying_verification_properties() {
      // Given
      final var verification = properties.getVerification();

      // When
      verification.setGlobalExcludeColumns(Set.of("created_at", "updated_at"));
      verification.setRowOrdering(RowOrdering.UNORDERED);
      verification.setRetryCount(3);
      verification.setRetryDelay(Duration.ofSeconds(1));

      // Then
      assertAll(
          "verification modified values should be correct",
          () ->
              assertEquals(
                  Set.of("created_at", "updated_at"),
                  verification.getGlobalExcludeColumns(),
                  "globalExcludeColumns mismatch"),
          () ->
              assertEquals(
                  RowOrdering.UNORDERED, verification.getRowOrdering(), "rowOrdering mismatch"),
          () -> assertEquals(3, verification.getRetryCount(), "retryCount mismatch"),
          () ->
              assertEquals(
                  Duration.ofSeconds(1), verification.getRetryDelay(), "retryDelay mismatch"));
    }

    /** Verifies that verification can be replaced. */
    @Test
    @Tag("normal")
    @DisplayName("should allow replacing verification")
    void should_allow_replacing_verification() {
      // Given
      final var newVerification = new DbTesterProperties.VerificationProperties();
      newVerification.setRowOrdering(RowOrdering.UNORDERED);
      newVerification.setRetryCount(5);

      // When
      properties.setVerification(newVerification);

      // Then
      assertAll(
          "replaced verification should be correct",
          () ->
              assertEquals(
                  RowOrdering.UNORDERED,
                  properties.getVerification().getRowOrdering(),
                  "rowOrdering mismatch"),
          () ->
              assertEquals(5, properties.getVerification().getRetryCount(), "retryCount mismatch"));
    }
  }

  /** Tests for the execution property. */
  @Nested
  @DisplayName("execution property")
  class ExecutionProperty {

    /** Tests for the execution property. */
    ExecutionProperty() {}

    /** Verifies that execution has correct default values. */
    @Test
    @Tag("normal")
    @DisplayName("should have correct default values")
    void should_have_correct_default_values() {
      // Given & When
      final var execution = properties.getExecution();

      // Then
      assertAll(
          "execution default values should be correct",
          () -> assertNotNull(execution, "execution should not be null"),
          () -> assertNull(execution.getQueryTimeout(), "queryTimeout should default to null"),
          () ->
              assertEquals(
                  TransactionMode.SINGLE_TRANSACTION,
                  execution.getTransactionMode(),
                  "transactionMode should default to SINGLE_TRANSACTION"));
    }

    /** Verifies that execution properties can be modified. */
    @Test
    @Tag("normal")
    @DisplayName("should allow modifying execution properties")
    void should_allow_modifying_execution_properties() {
      // Given
      final var execution = properties.getExecution();

      // When
      execution.setQueryTimeout(Duration.ofSeconds(30));
      execution.setTransactionMode(TransactionMode.AUTO_COMMIT);

      // Then
      assertAll(
          "execution modified values should be correct",
          () ->
              assertEquals(
                  Duration.ofSeconds(30), execution.getQueryTimeout(), "queryTimeout mismatch"),
          () ->
              assertEquals(
                  TransactionMode.AUTO_COMMIT,
                  execution.getTransactionMode(),
                  "transactionMode mismatch"));
    }

    /** Verifies that execution can be replaced. */
    @Test
    @Tag("normal")
    @DisplayName("should allow replacing execution")
    void should_allow_replacing_execution() {
      // Given
      final var newExecution = new DbTesterProperties.ExecutionProperties();
      newExecution.setQueryTimeout(Duration.ofMinutes(1));
      newExecution.setTransactionMode(TransactionMode.AUTO_COMMIT);

      // When
      properties.setExecution(newExecution);

      // Then
      assertAll(
          "replaced execution should be correct",
          () ->
              assertEquals(
                  Duration.ofMinutes(1),
                  properties.getExecution().getQueryTimeout(),
                  "queryTimeout mismatch"),
          () ->
              assertEquals(
                  TransactionMode.AUTO_COMMIT,
                  properties.getExecution().getTransactionMode(),
                  "transactionMode mismatch"));
    }
  }
}
