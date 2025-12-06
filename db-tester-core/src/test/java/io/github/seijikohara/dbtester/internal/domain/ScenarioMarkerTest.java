package io.github.seijikohara.dbtester.internal.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ScenarioMarker}. */
@DisplayName("ScenarioMarker")
class ScenarioMarkerTest {

  /** Tests for the ScenarioMarker class. */
  ScenarioMarkerTest() {}

  /** Tests for the DEFAULT constant. */
  @Nested
  @DisplayName("DEFAULT constant")
  class DefaultConstant {

    /** Tests for the DEFAULT constant. */
    DefaultConstant() {}

    /** Verifies that DEFAULT is not null. */
    @Test
    @Tag("normal")
    @DisplayName("should not be null")
    void should_not_be_null() {
      // Given & When & Then
      assertNotNull(ScenarioMarker.DEFAULT, "DEFAULT should not be null");
    }

    /** Verifies that DEFAULT has expected value. */
    @Test
    @Tag("normal")
    @DisplayName("should have value [Scenario]")
    void should_have_value_scenario() {
      // Given & When
      final var value = ScenarioMarker.DEFAULT.value();

      // Then
      assertEquals("[Scenario]", value, "DEFAULT should have value [Scenario]");
    }
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor creates instance with valid marker. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when valid marker provided")
    void should_create_instance_when_valid_marker_provided() {
      // Given
      final var marker = "[TestCase]";

      // When
      final var scenarioMarker = new ScenarioMarker(marker);

      // Then
      assertEquals(marker, scenarioMarker.value(), "value should match");
    }

    /** Verifies that constructor trims whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should trim whitespace from marker")
    void should_trim_whitespace_from_marker() {
      // Given
      final var markerWithSpaces = "  [Test]  ";

      // When
      final var scenarioMarker = new ScenarioMarker(markerWithSpaces);

      // Then
      assertEquals("[Test]", scenarioMarker.value(), "should trim whitespace");
    }

    /** Verifies that constructor throws exception for blank marker. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when marker is blank")
    void should_throw_exception_when_marker_is_blank() {
      // Given
      final var blankMarker = "   ";

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new ScenarioMarker(blankMarker),
              "should throw IllegalArgumentException");
      assertTrue(
          Objects.requireNonNull(exception.getMessage()).contains("must not be blank"),
          "message should indicate blank not allowed");
    }

    /** Verifies that constructor throws exception for empty marker. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when marker is empty")
    void should_throw_exception_when_marker_is_empty() {
      // Given
      final var emptyMarker = "";

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new ScenarioMarker(emptyMarker),
          "should throw IllegalArgumentException for empty marker");
    }

    /** Verifies that constructor accepts custom markers without brackets. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept markers without brackets")
    void should_accept_markers_without_brackets() {
      // Given
      final var customMarker = "SCENARIO";

      // When
      final var scenarioMarker = new ScenarioMarker(customMarker);

      // Then
      assertEquals(customMarker, scenarioMarker.value(), "should accept custom markers");
    }
  }

  /** Tests for the compareTo method. */
  @Nested
  @DisplayName("compareTo(ScenarioMarker) method")
  class CompareToMethod {

    /** Tests for the compareTo method. */
    CompareToMethod() {}

    /** Verifies that compareTo orders lexicographically. */
    @Test
    @Tag("normal")
    @DisplayName("should order lexicographically")
    void should_order_lexicographically() {
      // Given
      final var alpha = new ScenarioMarker("[Alpha]");
      final var alphaEqual = new ScenarioMarker("[Alpha]");
      final var beta = new ScenarioMarker("[Beta]");
      final var scenario = new ScenarioMarker("[Scenario]");

      // When & Then
      assertAll(
          "should order lexicographically",
          () -> assertTrue(alpha.compareTo(beta) < 0, "[Alpha] should be before [Beta]"),
          () -> assertTrue(beta.compareTo(scenario) < 0, "[Beta] should be before [Scenario]"),
          () -> assertEquals(0, alpha.compareTo(alphaEqual), "[Alpha] should equal [Alpha]"));
    }
  }

  /** Tests for equals and hashCode. */
  @Nested
  @DisplayName("equals and hashCode")
  class EqualsAndHashCode {

    /** Tests for equals and hashCode. */
    EqualsAndHashCode() {}

    /** Verifies that instances with same value are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal when values are the same")
    void should_be_equal_when_values_are_the_same() {
      // Given
      final var marker1 = new ScenarioMarker("[Scenario]");
      final var marker2 = new ScenarioMarker("[Scenario]");

      // When & Then
      assertAll(
          "should be equal",
          () -> assertEquals(marker1, marker2, "should be equal"),
          () -> assertEquals(marker1.hashCode(), marker2.hashCode(), "hashCodes should match"));
    }

    /** Verifies that instances with different values are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when values differ")
    void should_not_be_equal_when_values_differ() {
      // Given
      final var marker1 = new ScenarioMarker("[Scenario]");
      final var marker2 = new ScenarioMarker("[TestCase]");

      // When & Then
      assertNotEquals(marker1, marker2, "should not be equal");
    }

    /** Verifies equality with DEFAULT constant. */
    @Test
    @Tag("normal")
    @DisplayName("should be equal to DEFAULT when value matches")
    void should_be_equal_to_default_when_value_matches() {
      // Given
      final var marker = new ScenarioMarker("[Scenario]");

      // When & Then
      assertEquals(ScenarioMarker.DEFAULT, marker, "should equal DEFAULT");
    }
  }
}
