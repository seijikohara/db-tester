package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link VerificationSettings}. */
@DisplayName("VerificationSettings")
class VerificationSettingsTest {

  /** Tests for the VerificationSettings class. */
  VerificationSettingsTest() {}

  /** Tests for standard() factory method. */
  @Nested
  @DisplayName("standard()")
  class StandardTest {

    /** Tests for standard() factory method. */
    StandardTest() {}

    /** Verifies that standard returns instance with default values. */
    @Test
    @Tag("normal")
    @DisplayName("should return instance with default values when called")
    void shouldReturnInstanceWithDefaultValues_whenCalled() {
      // When
      final var settings = VerificationSettings.standard();

      // Then
      assertAll(
          "standard settings should have default values",
          () -> assertNotNull(settings, "instance should not be null"),
          () ->
              assertTrue(
                  settings.globalExcludeColumns().isEmpty(),
                  "globalExcludeColumns should be empty"),
          () ->
              assertTrue(
                  settings.globalColumnStrategies().isEmpty(),
                  "globalColumnStrategies should be empty"),
          () ->
              assertEquals(
                  RowOrdering.ORDERED, settings.rowOrdering(), "rowOrdering should be ORDERED"),
          () -> assertEquals(0, settings.retryCount(), "retryCount should be 0"),
          () ->
              assertEquals(
                  Duration.ofMillis(100), settings.retryDelay(), "retryDelay should be 100ms"));
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
    @DisplayName("should create builder with default values when called")
    void shouldCreateBuilderWithDefaultValues_whenCalled() {
      // When
      final var builder = VerificationSettings.builder();

      // Then
      assertNotNull(builder, "builder should not be null");
    }

    /** Verifies that builder builds settings with custom global exclude columns. */
    @Test
    @Tag("normal")
    @DisplayName("should build settings when custom global exclude columns provided")
    void shouldBuildSettings_whenCustomGlobalExcludeColumnsProvided() {
      // Given
      final var excludeColumns = Set.of("CREATED_AT", "UPDATED_AT");

      // When
      final var settings =
          VerificationSettings.builder().globalExcludeColumns(excludeColumns).build();

      // Then
      assertEquals(
          excludeColumns, settings.globalExcludeColumns(), "globalExcludeColumns should match");
    }

    /** Verifies that builder builds settings with custom global column strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should build settings when custom global column strategies provided")
    void shouldBuildSettings_whenCustomGlobalColumnStrategiesProvided() {
      // Given
      final var strategies = Map.of("timestamp", ColumnStrategyMapping.ignore("timestamp"));

      // When
      final var settings =
          VerificationSettings.builder().globalColumnStrategies(strategies).build();

      // Then
      assertEquals(
          1,
          settings.globalColumnStrategies().size(),
          "globalColumnStrategies should have one entry");
    }

    /** Verifies that builder builds settings with custom row ordering. */
    @Test
    @Tag("normal")
    @DisplayName("should build settings when custom row ordering provided")
    void shouldBuildSettings_whenCustomRowOrderingProvided() {
      // When
      final var settings =
          VerificationSettings.builder().rowOrdering(RowOrdering.UNORDERED).build();

      // Then
      assertEquals(
          RowOrdering.UNORDERED, settings.rowOrdering(), "rowOrdering should be UNORDERED");
    }

    /** Verifies that builder rejects RowOrdering.UNSET. */
    @Test
    @Tag("error")
    @DisplayName("throws exception when RowOrdering.UNSET is provided")
    void shouldThrowException_whenRowOrderingUnsetProvided() {
      // Given
      final var builder = VerificationSettings.builder();

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> builder.rowOrdering(RowOrdering.UNSET),
              "should reject RowOrdering.UNSET");
      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("UNSET"), "exception message should mention UNSET");
    }

    /** Verifies that builder builds settings with custom retry count. */
    @Test
    @Tag("normal")
    @DisplayName("should build settings when custom retry count provided")
    void shouldBuildSettings_whenCustomRetryCountProvided() {
      // When
      final var settings = VerificationSettings.builder().retryCount(3).build();

      // Then
      assertEquals(3, settings.retryCount(), "retryCount should be 3");
    }

    /** Verifies that builder builds settings with custom retry delay. */
    @Test
    @Tag("normal")
    @DisplayName("should build settings when custom retry delay provided")
    void shouldBuildSettings_whenCustomRetryDelayProvided() {
      // Given
      final var delay = Duration.ofSeconds(1);

      // When
      final var settings = VerificationSettings.builder().retryDelay(delay).build();

      // Then
      assertEquals(delay, settings.retryDelay(), "retryDelay should be 1 second");
    }

    /** Verifies that builder builds settings with all custom values. */
    @Test
    @Tag("normal")
    @DisplayName("should build settings when all custom values provided")
    void shouldBuildSettings_whenAllCustomValuesProvided() {
      // Given
      final var excludeColumns = Set.of("VERSION");
      final var strategies = Map.of("col", ColumnStrategyMapping.ignore("col"));
      final var delay = Duration.ofMillis(500);

      // When
      final var settings =
          VerificationSettings.builder()
              .globalExcludeColumns(excludeColumns)
              .globalColumnStrategies(strategies)
              .rowOrdering(RowOrdering.UNORDERED)
              .retryCount(5)
              .retryDelay(delay)
              .build();

      // Then
      assertAll(
          "settings should have all custom values",
          () ->
              assertEquals(
                  excludeColumns,
                  settings.globalExcludeColumns(),
                  "globalExcludeColumns should match"),
          () ->
              assertEquals(
                  1,
                  settings.globalColumnStrategies().size(),
                  "globalColumnStrategies should have one entry"),
          () ->
              assertEquals(
                  RowOrdering.UNORDERED, settings.rowOrdering(), "rowOrdering should be UNORDERED"),
          () -> assertEquals(5, settings.retryCount(), "retryCount should be 5"),
          () -> assertEquals(delay, settings.retryDelay(), "retryDelay should be 500ms"));
    }

    /** Verifies that builder throws exception for negative retry count. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when negative retry count provided")
    void shouldThrowException_whenNegativeRetryCountProvided() {
      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> VerificationSettings.builder().retryCount(-1),
          "should throw IllegalArgumentException for negative retryCount");
    }

    /** Verifies that builder throws exception for null global exclude columns. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when null global exclude columns provided")
    @SuppressWarnings("NullAway")
    void shouldThrowException_whenNullGlobalExcludeColumnsProvided() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> VerificationSettings.builder().globalExcludeColumns(null),
          "should throw NullPointerException for null globalExcludeColumns");
    }

    /** Verifies that builder throws exception for null global column strategies. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when null global column strategies provided")
    @SuppressWarnings("NullAway")
    void shouldThrowException_whenNullGlobalColumnStrategiesProvided() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> VerificationSettings.builder().globalColumnStrategies(null),
          "should throw NullPointerException for null globalColumnStrategies");
    }

    /** Verifies that builder throws exception for null row ordering. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when null row ordering provided")
    @SuppressWarnings("NullAway")
    void shouldThrowException_whenNullRowOrderingProvided() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> VerificationSettings.builder().rowOrdering(null),
          "should throw NullPointerException for null rowOrdering");
    }

    /** Verifies that builder throws exception for null retry delay. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when null retry delay provided")
    @SuppressWarnings("NullAway")
    void shouldThrowException_whenNullRetryDelayProvided() {
      // When & Then
      assertThrows(
          NullPointerException.class,
          () -> VerificationSettings.builder().retryDelay(null),
          "should throw NullPointerException for null retryDelay");
    }
  }

  /** Tests for with* methods. */
  @Nested
  @DisplayName("with* methods")
  class WithMethodsTest {

    /** Tests for with* methods. */
    WithMethodsTest() {}

    /** Verifies that withGlobalExcludeColumns creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create new instance when withGlobalExcludeColumns called")
    void shouldCreateNewInstance_whenWithGlobalExcludeColumnsCalled() {
      // Given
      final var original = VerificationSettings.standard();
      final var columns = Set.of("ID", "VERSION");

      // When
      final var modified = original.withGlobalExcludeColumns(columns);

      // Then
      assertNotEquals(original, modified, "modified should not equal original");
      assertEquals(columns, modified.globalExcludeColumns(), "should have new exclude columns");
      assertTrue(
          original.globalExcludeColumns().isEmpty(),
          "original should remain with empty exclude columns");
    }

    /** Verifies that withGlobalColumnStrategies creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create new instance when withGlobalColumnStrategies called")
    void shouldCreateNewInstance_whenWithGlobalColumnStrategiesCalled() {
      // Given
      final var original = VerificationSettings.standard();
      final var strategies = Map.of("col", ColumnStrategyMapping.ignore("col"));

      // When
      final var modified = original.withGlobalColumnStrategies(strategies);

      // Then
      assertNotEquals(original, modified, "modified should not equal original");
      assertEquals(
          1, modified.globalColumnStrategies().size(), "should have one global column strategy");
    }

    /** Verifies that withRowOrdering creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create new instance when withRowOrdering called")
    void shouldCreateNewInstance_whenWithRowOrderingCalled() {
      // Given
      final var original = VerificationSettings.standard();

      // When
      final var modified = original.withRowOrdering(RowOrdering.UNORDERED);

      // Then
      assertNotEquals(original, modified, "modified should not equal original");
      assertEquals(
          RowOrdering.UNORDERED, modified.rowOrdering(), "should have UNORDERED row ordering");
      assertEquals(
          RowOrdering.ORDERED,
          original.rowOrdering(),
          "original should remain with ORDERED row ordering");
    }

    /** Verifies that withRetryCount creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create new instance when withRetryCount called")
    void shouldCreateNewInstance_whenWithRetryCountCalled() {
      // Given
      final var original = VerificationSettings.standard();

      // When
      final var modified = original.withRetryCount(5);

      // Then
      assertNotEquals(original, modified, "modified should not equal original");
      assertEquals(5, modified.retryCount(), "should have retryCount 5");
      assertEquals(0, original.retryCount(), "original should remain with retryCount 0");
    }

    /** Verifies that withRetryDelay creates new instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create new instance when withRetryDelay called")
    void shouldCreateNewInstance_whenWithRetryDelayCalled() {
      // Given
      final var original = VerificationSettings.standard();
      final var delay = Duration.ofMillis(500);

      // When
      final var modified = original.withRetryDelay(delay);

      // Then
      assertNotEquals(original, modified, "modified should not equal original");
      assertEquals(delay, modified.retryDelay(), "should have new retry delay");
      assertEquals(
          Duration.ofMillis(100), original.retryDelay(), "original should remain with 100ms delay");
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
    @DisplayName("should create builder with current values when toBuilder called")
    void shouldCreateBuilderWithCurrentValues_whenToBuilderCalled() {
      // Given
      final var original =
          VerificationSettings.builder()
              .globalExcludeColumns(Set.of("CREATED_AT"))
              .rowOrdering(RowOrdering.UNORDERED)
              .retryCount(2)
              .retryDelay(Duration.ofMillis(200))
              .build();

      // When
      final var rebuilt = original.toBuilder().build();

      // Then
      assertEquals(original, rebuilt, "rebuilt should equal original");
    }

    /** Verifies that toBuilder allows modification of copied values. */
    @Test
    @Tag("normal")
    @DisplayName("should allow modification when toBuilder called")
    void shouldAllowModification_whenToBuilderCalled() {
      // Given
      final var original = VerificationSettings.builder().retryCount(1).build();

      // When
      final var modified = original.toBuilder().retryCount(5).build();

      // Then
      assertEquals(1, original.retryCount(), "original should retain retryCount 1");
      assertEquals(5, modified.retryCount(), "modified should have retryCount 5");
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
    @DisplayName("should return true when equals called with same values")
    void shouldReturnTrue_whenEqualsCalledWithSameValues() {
      // Given
      final var settings1 = VerificationSettings.standard();
      final var settings2 = VerificationSettings.standard();

      // Then
      assertEquals(settings1, settings2, "settings with same values should be equal");
    }

    /** Verifies that equals returns false for different values. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when equals called with different values")
    void shouldReturnFalse_whenEqualsCalledWithDifferentValues() {
      // Given
      final var settings1 = VerificationSettings.standard();
      final var settings2 = VerificationSettings.builder().retryCount(5).build();

      // Then
      assertNotEquals(
          settings1, settings2, "settings with different retryCount should not be equal");
    }

    /** Verifies that equals returns true for same instance. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when equals called with same instance")
    void shouldReturnTrue_whenEqualsCalledWithSameInstance() {
      // Given
      final var settings = VerificationSettings.standard();

      // Then
      assertEquals(settings, settings, "settings should be equal to itself");
    }

    /** Verifies that equals returns false for null. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when equals called with null")
    void shouldReturnFalse_whenEqualsCalledWithNull() {
      // Given
      final var settings = VerificationSettings.standard();

      // Then
      assertNotEquals(null, settings, "settings should not be equal to null");
    }

    /** Verifies that equals returns false for different type. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when equals called with different type")
    void shouldReturnFalse_whenEqualsCalledWithDifferentType() {
      // Given
      final var settings = VerificationSettings.standard();

      // Then
      assertNotEquals("string", settings, "settings should not be equal to String");
    }

    /** Verifies that hashCode is consistent for equal objects. */
    @Test
    @Tag("normal")
    @DisplayName("should be consistent when hashCode called for equal objects")
    void shouldBeConsistent_whenHashCodeCalledForEqualObjects() {
      // Given
      final var settings1 = VerificationSettings.standard();
      final var settings2 = VerificationSettings.standard();

      // Then
      assertEquals(
          settings1.hashCode(), settings2.hashCode(), "hash codes should match for equal settings");
    }

    /** Verifies that equals returns false for different row ordering. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when equals called with different row ordering")
    void shouldReturnFalse_whenEqualsCalledWithDifferentRowOrdering() {
      // Given
      final var settings1 = VerificationSettings.standard();
      final var settings2 =
          VerificationSettings.builder().rowOrdering(RowOrdering.UNORDERED).build();

      // Then
      assertNotEquals(
          settings1, settings2, "settings with different rowOrdering should not be equal");
    }

    /** Verifies that equals returns false for different global exclude columns. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when equals called with different global exclude columns")
    void shouldReturnFalse_whenEqualsCalledWithDifferentGlobalExcludeColumns() {
      // Given
      final var settings1 = VerificationSettings.standard();
      final var settings2 =
          VerificationSettings.builder().globalExcludeColumns(Set.of("ID")).build();

      // Then
      assertNotEquals(
          settings1, settings2, "settings with different globalExcludeColumns should not be equal");
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
    @DisplayName("should return string representation when toString called")
    void shouldReturnStringRepresentation_whenToStringCalled() {
      // Given
      final var settings = VerificationSettings.standard();

      // When
      final var result = settings.toString();

      // Then
      assertAll(
          "toString should contain field values",
          () -> assertNotNull(result, "toString should not return null"),
          () ->
              assertTrue(
                  result.contains("VerificationSettings"),
                  "should contain class name 'VerificationSettings'"),
          () ->
              assertTrue(
                  result.contains("globalExcludeColumns"),
                  "should contain field name 'globalExcludeColumns'"),
          () ->
              assertTrue(result.contains("rowOrdering"), "should contain field name 'rowOrdering'"),
          () -> assertTrue(result.contains("ORDERED"), "should contain row ordering value"),
          () -> assertTrue(result.contains("retryCount"), "should contain field name 'retryCount'"),
          () ->
              assertTrue(result.contains("retryDelay"), "should contain field name 'retryDelay'"));
    }
  }
}
