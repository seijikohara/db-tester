package io.github.seijikohara.dbtester.api.export;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ExportConfiguration}. */
@DisplayName("ExportConfiguration")
class ExportConfigurationTest {

  /** Tests for the ExportConfiguration class. */
  ExportConfigurationTest() {}

  /** Tests for the defaults() method. */
  @Nested
  @DisplayName("defaults() method")
  class DefaultsMethod {

    /** Tests for the defaults method. */
    DefaultsMethod() {}

    /** Verifies that defaults returns a configuration with default values. */
    @Test
    @Tag("normal")
    @DisplayName("should return configuration with default values when called")
    void shouldReturnDefaults_whenCalled() {
      // When
      final var config = ExportConfiguration.defaults();

      // Then
      assertAll(
          "configuration should expose default values",
          () ->
              assertEquals(
                  ExportConfiguration.DEFAULT_NULL_VALUE,
                  config.nullValue(),
                  "null value should be the default"),
          () ->
              assertEquals(
                  LobHandling.BASE64, config.lobHandling(), "LOB handling should be BASE64"),
          () ->
              assertEquals(
                  false, config.writeLoadOrderFile(), "load order file generation should be off"));
    }
  }

  /** Tests for equals, hashCode, and toString. */
  @Nested
  @DisplayName("equality and toString")
  class EqualityTests {

    /** Tests for equality and toString. */
    EqualityTests() {}

    /** Verifies that two configurations with identical values are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when values are identical")
    void shouldBeEqual_whenValuesAreIdentical() {
      // Given
      final var first = ExportConfiguration.builder().nullValue("NULL").build();
      final var second = ExportConfiguration.builder().nullValue("NULL").build();

      // Then
      assertAll(
          "configurations with identical values should be equal",
          () -> assertEquals(first, second, "instances should be equal"),
          () -> assertEquals(first.hashCode(), second.hashCode(), "hash codes should match"));
    }

    /** Verifies that configurations with different values are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when values differ")
    void shouldNotBeEqual_whenValuesDiffer() {
      // Given
      final var first = ExportConfiguration.builder().nullValue("NULL").build();
      final var second =
          ExportConfiguration.builder()
              .nullValue("NULL")
              .lobHandling(LobHandling.OMIT)
              .timestampFormatter(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
              .build();

      // Then
      assertNotEquals(first, second, "instances with different values should not be equal");
    }

    /** Verifies that toString includes the configured field values. */
    @Test
    @Tag("normal")
    @DisplayName("should include field values when toString called")
    void shouldIncludeFieldValues_whenToStringCalled() {
      // Given
      final var config =
          ExportConfiguration.builder().nullValue("NULL").lobHandling(LobHandling.OMIT).build();

      // When
      final var text = config.toString();

      // Then
      assertAll(
          "string representation should include field values",
          () -> assertTrue(text.contains("ExportConfiguration["), "should name the type"),
          () -> assertTrue(text.contains("nullValue=NULL"), "should include null value"),
          () -> assertTrue(text.contains("lobHandling=OMIT"), "should include LOB handling"));
    }
  }
}
