package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
                  "/expected", settings.expectationSuffix(), "expectationSuffix should be default"),
          () ->
              assertEquals(
                  "[Scenario]", settings.scenarioMarker(), "scenarioMarker should be default"));
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

      // When
      final var settings = new ConventionSettings(baseDir, suffix, marker);

      // Then
      assertAll(
          "should have custom values",
          () -> assertEquals(baseDir, settings.baseDirectory(), "baseDirectory should match"),
          () ->
              assertEquals(suffix, settings.expectationSuffix(), "expectationSuffix should match"),
          () -> assertEquals(marker, settings.scenarioMarker(), "scenarioMarker should match"));
    }

    /** Verifies that constructor accepts null base directory. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept null base directory")
    void should_accept_null_base_directory() {
      // Given & When
      final var settings = new ConventionSettings(null, "/expected", "[Scenario]");

      // Then
      assertNull(settings.baseDirectory(), "baseDirectory should be null");
    }

    /** Verifies that constructor accepts empty strings. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept empty strings for suffix and marker")
    void should_accept_empty_strings_for_suffix_and_marker() {
      // Given & When
      final var settings = new ConventionSettings(null, "", "");

      // Then
      assertAll(
          "should accept empty strings",
          () -> assertEquals("", settings.expectationSuffix(), "expectationSuffix should be empty"),
          () -> assertEquals("", settings.scenarioMarker(), "scenarioMarker should be empty"));
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
      final var settings1 = new ConventionSettings("/base", "/expected", "[Scenario]");
      final var settings2 = new ConventionSettings("/base", "/expected", "[Scenario]");

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
      final var settings1 = new ConventionSettings(null, "/expected", "[Scenario]");
      final var settings2 = new ConventionSettings(null, "/expected", "[Scenario]");

      // When & Then
      assertEquals(settings1, settings2, "should be equal with null baseDirectory");
    }
  }
}
