package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.operation.Operation;
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
                  DataFormat.CSV, convention.getDataFormat(), "dataFormat should default to CSV"),
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
}
