package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
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

  /** Tests for standard() factory method. */
  @Nested
  @DisplayName("standard()")
  class StandardTest {

    /** Tests for standard() factory method. */
    StandardTest() {}

    /** Verifies that standard returns instance with default values. */
    @Test
    @Tag("normal")
    @DisplayName("returns instance with default values")
    void returnsInstanceWithDefaultValues() {
      final var settings = ConventionSettings.standard();

      assertNotNull(settings);
      assertNull(settings.baseDirectory());
      assertEquals(ConventionSettings.DEFAULT_EXPECTATION_SUFFIX, settings.expectationSuffix());
      assertEquals(ConventionSettings.DEFAULT_SCENARIO_MARKER, settings.scenarioMarker());
      assertEquals(DataFormat.CSV, settings.dataFormat());
      assertEquals(TableMergeStrategy.UNION_ALL, settings.tableMergeStrategy());
      assertEquals(ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME, settings.loadOrderFileName());
      assertTrue(settings.globalExcludeColumns().isEmpty());
      assertTrue(settings.globalColumnStrategies().isEmpty());
      assertEquals(RowOrdering.ORDERED, settings.rowOrdering());
      assertNull(settings.queryTimeout());
      assertEquals(0, settings.retryCount());
      assertEquals(Duration.ofMillis(100), settings.retryDelay());
      assertEquals(TransactionMode.SINGLE_TRANSACTION, settings.transactionMode());
    }
  }

  /** Tests for builder() factory method. */
  @Nested
  @DisplayName("builder()")
  class BuilderTest {

    /** Tests for builder() factory method. */
    BuilderTest() {}

    /** Verifies that builder creates builder with default values. */
    @Test
    @Tag("normal")
    @DisplayName("creates builder with default values")
    void createsBuilderWithDefaultValues() {
      final var builder = ConventionSettings.builder();

      assertNotNull(builder);
    }

    /** Verifies that builder builds settings with custom base directory. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom base directory")
    void buildsSettingsWithCustomBaseDirectory() {
      final var settings = ConventionSettings.builder().baseDirectory("/test/data").build();

      assertEquals("/test/data", settings.baseDirectory());
    }

    /** Verifies that builder builds settings with custom expectation suffix. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom expectation suffix")
    void buildsSettingsWithCustomExpectationSuffix() {
      final var settings = ConventionSettings.builder().expectationSuffix("/verify").build();

      assertEquals("/verify", settings.expectationSuffix());
    }

    /** Verifies that builder builds settings with custom scenario marker. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom scenario marker")
    void buildsSettingsWithCustomScenarioMarker() {
      final var settings = ConventionSettings.builder().scenarioMarker("[Test]").build();

      assertEquals("[Test]", settings.scenarioMarker());
    }

    /** Verifies that builder builds settings with custom data format. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom data format")
    void buildsSettingsWithCustomDataFormat() {
      final var settings = ConventionSettings.builder().dataFormat(DataFormat.TSV).build();

      assertEquals(DataFormat.TSV, settings.dataFormat());
    }

    /** Verifies that builder builds settings with custom table merge strategy. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom table merge strategy")
    void buildsSettingsWithCustomTableMergeStrategy() {
      final var settings =
          ConventionSettings.builder().tableMergeStrategy(TableMergeStrategy.LAST).build();

      assertEquals(TableMergeStrategy.LAST, settings.tableMergeStrategy());
    }

    /** Verifies that builder builds settings with custom load order file name. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom load order file name")
    void buildsSettingsWithCustomLoadOrderFileName() {
      final var settings = ConventionSettings.builder().loadOrderFileName("order.txt").build();

      assertEquals("order.txt", settings.loadOrderFileName());
    }

    /** Verifies that builder builds settings with global exclude columns. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with global exclude columns")
    void buildsSettingsWithGlobalExcludeColumns() {
      final var excludeColumns = Set.of("created_at", "updated_at");
      final var settings =
          ConventionSettings.builder().globalExcludeColumns(excludeColumns).build();

      assertEquals(excludeColumns, settings.globalExcludeColumns());
    }

    /** Verifies that builder builds settings with global column strategies. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with global column strategies")
    void buildsSettingsWithGlobalColumnStrategies() {
      final var strategies = Map.of("timestamp", ColumnStrategyMapping.ignore("timestamp"));
      final var settings = ConventionSettings.builder().globalColumnStrategies(strategies).build();

      assertEquals(1, settings.globalColumnStrategies().size());
    }

    /** Verifies that builder builds settings with custom row ordering. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom row ordering")
    void buildsSettingsWithCustomRowOrdering() {
      final var settings = ConventionSettings.builder().rowOrdering(RowOrdering.UNORDERED).build();

      assertEquals(RowOrdering.UNORDERED, settings.rowOrdering());
    }

    /** Verifies that builder builds settings with custom query timeout. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom query timeout")
    void buildsSettingsWithCustomQueryTimeout() {
      final var timeout = Duration.ofSeconds(30);
      final var settings = ConventionSettings.builder().queryTimeout(timeout).build();

      assertEquals(timeout, settings.queryTimeout());
    }

    /** Verifies that builder builds settings with custom retry count. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom retry count")
    void buildsSettingsWithCustomRetryCount() {
      final var settings = ConventionSettings.builder().retryCount(3).build();

      assertEquals(3, settings.retryCount());
    }

    /** Verifies that builder throws exception for negative retry count. */
    @Test
    @Tag("exceptional")
    @DisplayName("throws exception for negative retry count")
    void throwsExceptionForNegativeRetryCount() {
      assertThrows(
          IllegalArgumentException.class, () -> ConventionSettings.builder().retryCount(-1));
    }

    /** Verifies that builder builds settings with custom retry delay. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom retry delay")
    void buildsSettingsWithCustomRetryDelay() {
      final var delay = Duration.ofSeconds(1);
      final var settings = ConventionSettings.builder().retryDelay(delay).build();

      assertEquals(delay, settings.retryDelay());
    }

    /** Verifies that builder builds settings with custom transaction mode. */
    @Test
    @Tag("normal")
    @DisplayName("builds settings with custom transaction mode")
    void buildsSettingsWithCustomTransactionMode() {
      final var settings =
          ConventionSettings.builder().transactionMode(TransactionMode.AUTO_COMMIT).build();

      assertEquals(TransactionMode.AUTO_COMMIT, settings.transactionMode());
    }
  }

  /** Tests for with* methods. */
  @Nested
  @DisplayName("with* methods")
  class WithMethodsTest {

    /** Tests for with* methods. */
    WithMethodsTest() {}

    /** Verifies that withBaseDirectory creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withBaseDirectory creates new instance")
    void withBaseDirectoryCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withBaseDirectory("/new/path");

      assertNotEquals(original, modified);
      assertEquals("/new/path", modified.baseDirectory());
      assertNull(original.baseDirectory());
    }

    /** Verifies that withExpectationSuffix creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withExpectationSuffix creates new instance")
    void withExpectationSuffixCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withExpectationSuffix("/verify");

      assertEquals("/verify", modified.expectationSuffix());
    }

    /** Verifies that withScenarioMarker creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withScenarioMarker creates new instance")
    void withScenarioMarkerCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withScenarioMarker("[Case]");

      assertEquals("[Case]", modified.scenarioMarker());
    }

    /** Verifies that withDataFormat creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withDataFormat creates new instance")
    void withDataFormatCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withDataFormat(DataFormat.TSV);

      assertEquals(DataFormat.TSV, modified.dataFormat());
    }

    /** Verifies that withTableMergeStrategy creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withTableMergeStrategy creates new instance")
    void withTableMergeStrategyCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withTableMergeStrategy(TableMergeStrategy.FIRST);

      assertEquals(TableMergeStrategy.FIRST, modified.tableMergeStrategy());
    }

    /** Verifies that withLoadOrderFileName creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withLoadOrderFileName creates new instance")
    void withLoadOrderFileNameCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withLoadOrderFileName("custom.txt");

      assertEquals("custom.txt", modified.loadOrderFileName());
    }

    /** Verifies that withGlobalExcludeColumns creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withGlobalExcludeColumns creates new instance")
    void withGlobalExcludeColumnsCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var columns = Set.of("id", "version");
      final var modified = original.withGlobalExcludeColumns(columns);

      assertEquals(columns, modified.globalExcludeColumns());
    }

    /** Verifies that withGlobalColumnStrategies creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withGlobalColumnStrategies creates new instance")
    void withGlobalColumnStrategiesCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var strategies = Map.of("col", ColumnStrategyMapping.ignore("col"));
      final var modified = original.withGlobalColumnStrategies(strategies);

      assertEquals(1, modified.globalColumnStrategies().size());
    }

    /** Verifies that withRowOrdering creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withRowOrdering creates new instance")
    void withRowOrderingCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withRowOrdering(RowOrdering.UNORDERED);

      assertEquals(RowOrdering.UNORDERED, modified.rowOrdering());
    }

    /** Verifies that withQueryTimeout creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withQueryTimeout creates new instance")
    void withQueryTimeoutCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var timeout = Duration.ofMinutes(1);
      final var modified = original.withQueryTimeout(timeout);

      assertEquals(timeout, modified.queryTimeout());
    }

    /** Verifies that withRetryCount creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withRetryCount creates new instance")
    void withRetryCountCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withRetryCount(5);

      assertEquals(5, modified.retryCount());
    }

    /** Verifies that withRetryDelay creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withRetryDelay creates new instance")
    void withRetryDelayCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var delay = Duration.ofMillis(500);
      final var modified = original.withRetryDelay(delay);

      assertEquals(delay, modified.retryDelay());
    }

    /** Verifies that withTransactionMode creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("withTransactionMode creates new instance")
    void withTransactionModeCreatesNewInstance() {
      final var original = ConventionSettings.standard();
      final var modified = original.withTransactionMode(TransactionMode.NONE);

      assertEquals(TransactionMode.NONE, modified.transactionMode());
    }
  }

  /** Tests for toBuilder() method. */
  @Nested
  @DisplayName("toBuilder()")
  class ToBuilderTest {

    /** Tests for toBuilder() method. */
    ToBuilderTest() {}

    /** Verifies that toBuilder creates builder with current values. */
    @Test
    @Tag("normal")
    @DisplayName("creates builder with current values")
    void createsBuilderWithCurrentValues() {
      final var original =
          ConventionSettings.builder()
              .baseDirectory("/test")
              .dataFormat(DataFormat.TSV)
              .retryCount(2)
              .build();

      final var rebuilt = original.toBuilder().build();

      assertEquals(original, rebuilt);
    }

    /** Verifies that toBuilder allows modification of copied values. */
    @Test
    @Tag("normal")
    @DisplayName("allows modification of copied values")
    void allowsModificationOfCopiedValues() {
      final var original = ConventionSettings.builder().retryCount(1).build();

      final var modified = original.toBuilder().retryCount(5).build();

      assertEquals(1, original.retryCount());
      assertEquals(5, modified.retryCount());
    }
  }

  /** Tests for equals and hashCode. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsHashCodeTest {

    /** Tests for equals and hashCode. */
    EqualsHashCodeTest() {}

    /** Verifies that equals returns true for same values. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns true for same values")
    void equalsReturnsTrueForSameValues() {
      final var settings1 = ConventionSettings.standard();
      final var settings2 = ConventionSettings.standard();

      assertEquals(settings1, settings2);
    }

    /** Verifies that equals returns false for different values. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different values")
    void equalsReturnsFalseForDifferentValues() {
      final var settings1 = ConventionSettings.standard();
      final var settings2 = ConventionSettings.builder().retryCount(5).build();

      assertNotEquals(settings1, settings2);
    }

    /** Verifies that equals returns true for same instance. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns true for same instance")
    void equalsReturnsTrueForSameInstance() {
      final var settings = ConventionSettings.standard();

      assertEquals(settings, settings);
    }

    /** Verifies that equals returns false for null. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for null")
    void equalsReturnsFalseForNull() {
      final var settings = ConventionSettings.standard();

      assertNotEquals(null, settings);
    }

    /** Verifies that equals returns false for different type. */
    @Test
    @Tag("normal")
    @DisplayName("equals returns false for different type")
    void equalsReturnsFalseForDifferentType() {
      final var settings = ConventionSettings.standard();

      assertNotEquals("string", settings);
    }

    /** Verifies that hashCode is consistent for equal objects. */
    @Test
    @Tag("normal")
    @DisplayName("hashCode is consistent for equal objects")
    void hashCodeIsConsistentForEqualObjects() {
      final var settings1 = ConventionSettings.standard();
      final var settings2 = ConventionSettings.standard();

      assertEquals(settings1.hashCode(), settings2.hashCode());
    }
  }

  /** Tests for toString() method. */
  @Nested
  @DisplayName("toString()")
  class ToStringTest {

    /** Tests for toString() method. */
    ToStringTest() {}

    /** Verifies that toString returns string representation. */
    @Test
    @Tag("normal")
    @DisplayName("returns string representation")
    void returnsStringRepresentation() {
      final var settings = ConventionSettings.standard();

      final var result = settings.toString();

      assertNotNull(result);
      assertTrue(result.contains("ConventionSettings"));
      assertTrue(result.contains("dataFormat"));
      assertTrue(result.contains("CSV"));
    }
  }

  /** Tests for constants. */
  @Nested
  @DisplayName("constants")
  class ConstantsTest {

    /** Tests for constants. */
    ConstantsTest() {}

    /** Verifies that DEFAULT_EXPECTATION_SUFFIX has expected value. */
    @Test
    @Tag("normal")
    @DisplayName("DEFAULT_EXPECTATION_SUFFIX has expected value")
    void defaultExpectationSuffixHasExpectedValue() {
      assertEquals("/expected", ConventionSettings.DEFAULT_EXPECTATION_SUFFIX);
    }

    /** Verifies that DEFAULT_SCENARIO_MARKER has expected value. */
    @Test
    @Tag("normal")
    @DisplayName("DEFAULT_SCENARIO_MARKER has expected value")
    void defaultScenarioMarkerHasExpectedValue() {
      assertEquals("[Scenario]", ConventionSettings.DEFAULT_SCENARIO_MARKER);
    }

    /** Verifies that DEFAULT_LOAD_ORDER_FILE_NAME has expected value. */
    @Test
    @Tag("normal")
    @DisplayName("DEFAULT_LOAD_ORDER_FILE_NAME has expected value")
    void defaultLoadOrderFileNameHasExpectedValue() {
      assertEquals("load-order.txt", ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME);
    }
  }
}
