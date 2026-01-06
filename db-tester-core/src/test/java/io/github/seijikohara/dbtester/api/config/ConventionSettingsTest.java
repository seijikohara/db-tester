package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ConventionSettings}. */
@DisplayName("ConventionSettings")
class ConventionSettingsTest {

  /** Tests for the ConventionSettings class. */
  ConventionSettingsTest() {}

  /** Tests for the standard factory method. */
  @Nested
  @DisplayName("standard() factory method")
  class StandardMethod {

    /** Tests for the standard method. */
    StandardMethod() {}

    /** Verifies that standard returns instance with default values. */
    @Test
    @Tag("normal")
    @DisplayName("should return instance with default values when called")
    void should_return_instance_with_default_values_when_called() {
      // Given & When
      final var settings = ConventionSettings.standard();

      // Then
      assertAll(
          "should have default values",
          () -> assertNull(settings.baseDirectory(), "baseDirectory should be null"),
          () ->
              assertEquals(
                  ConventionSettings.DEFAULT_EXPECTATION_SUFFIX,
                  settings.expectationSuffix(),
                  "expectationSuffix should be default"),
          () ->
              assertEquals(
                  ConventionSettings.DEFAULT_SCENARIO_MARKER,
                  settings.scenarioMarker(),
                  "scenarioMarker should be default"),
          () ->
              assertEquals(
                  DataFormat.CSV, settings.dataFormat(), "dataFormat should be CSV by default"),
          () ->
              assertEquals(
                  TableMergeStrategy.UNION_ALL,
                  settings.tableMergeStrategy(),
                  "tableMergeStrategy should be UNION_ALL by default"),
          () ->
              assertEquals(
                  ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
                  settings.loadOrderFileName(),
                  "loadOrderFileName should be default"),
          () ->
              assertTrue(
                  settings.globalExcludeColumns().isEmpty(),
                  "globalExcludeColumns should be empty by default"));
    }

    /** Verifies that standard returns consistent instances. */
    @Test
    @Tag("normal")
    @DisplayName("should return equal instances on multiple calls")
    void should_return_equal_instances_on_multiple_calls() {
      // Given & When
      final var settings1 = ConventionSettings.standard();
      final var settings2 = ConventionSettings.standard();

      // Then
      assertEquals(settings1, settings2, "standard instances should be equal");
    }
  }

  /** Tests for the record constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor accepts custom values. */
    @Test
    @Tag("normal")
    @DisplayName("should accept custom values when provided")
    void should_accept_custom_values_when_provided() {
      // Given
      final var baseDir = "/custom/base";
      final var suffix = "/outcome";
      final var marker = "[TestCase]";
      final var format = DataFormat.TSV;
      final var strategy = TableMergeStrategy.FIRST;
      final var loadOrderFileName = "custom-load-order.txt";

      // When
      final var settings =
          new ConventionSettings(
              baseDir, suffix, marker, format, strategy, loadOrderFileName, Set.of());

      // Then
      assertAll(
          "should have custom values",
          () -> assertEquals(baseDir, settings.baseDirectory(), "baseDirectory should match"),
          () ->
              assertEquals(suffix, settings.expectationSuffix(), "expectationSuffix should match"),
          () -> assertEquals(marker, settings.scenarioMarker(), "scenarioMarker should match"),
          () -> assertEquals(format, settings.dataFormat(), "dataFormat should match"),
          () ->
              assertEquals(
                  strategy, settings.tableMergeStrategy(), "tableMergeStrategy should match"),
          () ->
              assertEquals(
                  loadOrderFileName,
                  settings.loadOrderFileName(),
                  "loadOrderFileName should match"));
    }

    /** Verifies that constructor accepts null base directory. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept null base directory")
    void should_accept_null_base_directory() {
      // Given & When
      final var settings =
          new ConventionSettings(
              null,
              ConventionSettings.DEFAULT_EXPECTATION_SUFFIX,
              ConventionSettings.DEFAULT_SCENARIO_MARKER,
              DataFormat.CSV,
              TableMergeStrategy.UNION_ALL,
              ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
              Set.of());

      // Then
      assertNull(settings.baseDirectory(), "baseDirectory should be null");
    }

    /** Verifies that constructor accepts empty strings. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept empty strings for suffix and marker")
    void should_accept_empty_strings_for_suffix_and_marker() {
      // Given & When
      final var settings =
          new ConventionSettings(
              null, "", "", DataFormat.CSV, TableMergeStrategy.UNION_ALL, "", Set.of());

      // Then
      assertAll(
          "should accept empty strings",
          () -> assertEquals("", settings.expectationSuffix(), "expectationSuffix should be empty"),
          () -> assertEquals("", settings.scenarioMarker(), "scenarioMarker should be empty"),
          () ->
              assertEquals("", settings.loadOrderFileName(), "loadOrderFileName should be empty"));
    }
  }

  /** Tests for record equality. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsAndHashCode {

    /** Tests for equals and hashCode. */
    EqualsAndHashCode() {}

    /** Verifies that records with same values are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when values are the same")
    void should_be_equal_when_values_are_the_same() {
      // Given
      final var settings1 =
          new ConventionSettings(
              "/base",
              ConventionSettings.DEFAULT_EXPECTATION_SUFFIX,
              ConventionSettings.DEFAULT_SCENARIO_MARKER,
              DataFormat.CSV,
              TableMergeStrategy.UNION_ALL,
              ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
              Set.of());
      final var settings2 =
          new ConventionSettings(
              "/base",
              ConventionSettings.DEFAULT_EXPECTATION_SUFFIX,
              ConventionSettings.DEFAULT_SCENARIO_MARKER,
              DataFormat.CSV,
              TableMergeStrategy.UNION_ALL,
              ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
              Set.of());

      // When & Then
      assertAll(
          "should be equal",
          () -> assertEquals(settings1, settings2, "should be equal"),
          () -> assertEquals(settings1.hashCode(), settings2.hashCode(), "hashCodes should match"));
    }

    /** Verifies that records with null base directory are equal. */
    @Test
    @Tag("edge-case")
    @DisplayName("should be equal when both have null base directory")
    void should_be_equal_when_both_have_null_base_directory() {
      // Given
      final var settings1 =
          new ConventionSettings(
              null,
              ConventionSettings.DEFAULT_EXPECTATION_SUFFIX,
              ConventionSettings.DEFAULT_SCENARIO_MARKER,
              DataFormat.CSV,
              TableMergeStrategy.UNION_ALL,
              ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
              Set.of());
      final var settings2 =
          new ConventionSettings(
              null,
              ConventionSettings.DEFAULT_EXPECTATION_SUFFIX,
              ConventionSettings.DEFAULT_SCENARIO_MARKER,
              DataFormat.CSV,
              TableMergeStrategy.UNION_ALL,
              ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME,
              Set.of());

      // When & Then
      assertEquals(settings1, settings2, "should be equal with null baseDirectory");
    }
  }

  /** Tests for the withDataFormat method. */
  @Nested
  @DisplayName("withDataFormat() method")
  class WithDataFormatMethod {

    /** Tests for the withDataFormat method. */
    WithDataFormatMethod() {}

    /** Verifies that withDataFormat returns a new instance with the specified format. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with specified data format")
    void should_return_new_instance_with_specified_data_format() {
      // Given
      final var original = ConventionSettings.standard();

      // When
      final var modified = original.withDataFormat(DataFormat.TSV);

      // Then
      assertAll(
          "should have new data format while preserving other values",
          () -> assertEquals(DataFormat.TSV, modified.dataFormat(), "dataFormat should be TSV"),
          () ->
              assertEquals(
                  original.baseDirectory(), modified.baseDirectory(), "baseDirectory should match"),
          () ->
              assertEquals(
                  original.expectationSuffix(),
                  modified.expectationSuffix(),
                  "expectationSuffix should match"),
          () ->
              assertEquals(
                  original.scenarioMarker(),
                  modified.scenarioMarker(),
                  "scenarioMarker should match"),
          () ->
              assertEquals(
                  original.tableMergeStrategy(),
                  modified.tableMergeStrategy(),
                  "tableMergeStrategy should match"),
          () ->
              assertEquals(
                  original.loadOrderFileName(),
                  modified.loadOrderFileName(),
                  "loadOrderFileName should match"));
    }
  }

  /** Tests for the withTableMergeStrategy method. */
  @Nested
  @DisplayName("withTableMergeStrategy() method")
  class WithTableMergeStrategyMethod {

    /** Tests for the withTableMergeStrategy method. */
    WithTableMergeStrategyMethod() {}

    /** Verifies that withTableMergeStrategy returns a new instance with the specified strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with specified merge strategy")
    void should_return_new_instance_with_specified_merge_strategy() {
      // Given
      final var original = ConventionSettings.standard();

      // When
      final var modified = original.withTableMergeStrategy(TableMergeStrategy.FIRST);

      // Then
      assertAll(
          "should have new merge strategy while preserving other values",
          () ->
              assertEquals(
                  TableMergeStrategy.FIRST,
                  modified.tableMergeStrategy(),
                  "tableMergeStrategy should be FIRST"),
          () ->
              assertEquals(
                  original.baseDirectory(), modified.baseDirectory(), "baseDirectory should match"),
          () ->
              assertEquals(
                  original.expectationSuffix(),
                  modified.expectationSuffix(),
                  "expectationSuffix should match"),
          () ->
              assertEquals(
                  original.scenarioMarker(),
                  modified.scenarioMarker(),
                  "scenarioMarker should match"),
          () ->
              assertEquals(original.dataFormat(), modified.dataFormat(), "dataFormat should match"),
          () ->
              assertEquals(
                  original.loadOrderFileName(),
                  modified.loadOrderFileName(),
                  "loadOrderFileName should match"));
    }
  }

  /** Tests for the withLoadOrderFileName method. */
  @Nested
  @DisplayName("withLoadOrderFileName() method")
  class WithLoadOrderFileNameMethod {

    /** Tests for the withLoadOrderFileName method. */
    WithLoadOrderFileNameMethod() {}

    /** Verifies that withLoadOrderFileName returns a new instance with the specified file name. */
    @Test
    @Tag("normal")
    @DisplayName("should return new instance with specified load order file name")
    void should_return_new_instance_with_specified_load_order_file_name() {
      // Given
      final var original = ConventionSettings.standard();
      final var customFileName = "custom-order.txt";

      // When
      final var modified = original.withLoadOrderFileName(customFileName);

      // Then
      assertAll(
          "should have new load order file name while preserving other values",
          () ->
              assertEquals(
                  customFileName,
                  modified.loadOrderFileName(),
                  "loadOrderFileName should be custom"),
          () ->
              assertEquals(
                  original.baseDirectory(), modified.baseDirectory(), "baseDirectory should match"),
          () ->
              assertEquals(
                  original.expectationSuffix(),
                  modified.expectationSuffix(),
                  "expectationSuffix should match"),
          () ->
              assertEquals(
                  original.scenarioMarker(),
                  modified.scenarioMarker(),
                  "scenarioMarker should match"),
          () ->
              assertEquals(original.dataFormat(), modified.dataFormat(), "dataFormat should match"),
          () ->
              assertEquals(
                  original.tableMergeStrategy(),
                  modified.tableMergeStrategy(),
                  "tableMergeStrategy should match"));
    }
  }
}
