package io.github.seijikohara.dbtester.internal.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ScenarioName}. */
@DisplayName("ScenarioName")
class ScenarioNameTest {

  /** Tests for the ScenarioName class. */
  ScenarioNameTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor creates instance with valid name. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when valid name provided")
    void should_create_instance_when_valid_name_provided() {
      // Given
      final var name = "testCreateUser";

      // When
      final var scenarioName = new ScenarioName(name);

      // Then
      assertEquals(name, scenarioName.value(), "value should match");
    }

    /** Verifies that constructor trims whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should trim whitespace from name")
    void should_trim_whitespace_from_name() {
      // Given
      final var nameWithSpaces = "  shouldSaveUser  ";

      // When
      final var scenarioName = new ScenarioName(nameWithSpaces);

      // Then
      assertEquals("shouldSaveUser", scenarioName.value(), "should trim whitespace");
    }

    /** Verifies that constructor throws exception for blank name. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when name is blank")
    void should_throw_exception_when_name_is_blank() {
      // Given
      final var blankName = "   ";

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> new ScenarioName(blankName),
              "should throw IllegalArgumentException");
      assertTrue(
          Objects.requireNonNull(exception.getMessage()).contains("must not be blank"),
          "message should indicate blank not allowed");
    }

    /** Verifies that constructor throws exception for empty name. */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when name is empty")
    void should_throw_exception_when_name_is_empty() {
      // Given
      final var emptyName = "";

      // When & Then
      assertThrows(
          IllegalArgumentException.class,
          () -> new ScenarioName(emptyName),
          "should throw IllegalArgumentException for empty name");
    }

    /** Verifies that constructor accepts names with various characters. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept names with various characters")
    void should_accept_names_with_various_characters() {
      // Given
      final var nameWithSpecialChars = "should_create_user_with_email";

      // When
      final var scenarioName = new ScenarioName(nameWithSpecialChars);

      // Then
      assertEquals(nameWithSpecialChars, scenarioName.value(), "should accept various characters");
    }
  }

  /** Tests for the compareTo method. */
  @Nested
  @DisplayName("compareTo(ScenarioName) method")
  class CompareToMethod {

    /** Tests for the compareTo method. */
    CompareToMethod() {}

    /** Verifies that compareTo orders lexicographically. */
    @Test
    @Tag("normal")
    @DisplayName("should order lexicographically")
    void should_order_lexicographically() {
      // Given
      final var alpha = new ScenarioName("alpha");
      final var alphaEqual = new ScenarioName("alpha");
      final var beta = new ScenarioName("beta");
      final var gamma = new ScenarioName("gamma");

      // When & Then
      assertAll(
          "should order lexicographically",
          () -> assertTrue(alpha.compareTo(beta) < 0, "alpha should be before beta"),
          () -> assertTrue(beta.compareTo(gamma) < 0, "beta should be before gamma"),
          () -> assertEquals(0, alpha.compareTo(alphaEqual), "alpha should equal alpha"));
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
      final var name1 = new ScenarioName("testScenario");
      final var name2 = new ScenarioName("testScenario");

      // When & Then
      assertAll(
          "should be equal",
          () -> assertEquals(name1, name2, "should be equal"),
          () -> assertEquals(name1.hashCode(), name2.hashCode(), "hashCodes should match"));
    }

    /** Verifies that instances with different values are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when values differ")
    void should_not_be_equal_when_values_differ() {
      // Given
      final var name1 = new ScenarioName("scenario1");
      final var name2 = new ScenarioName("scenario2");

      // When & Then
      assertNotEquals(name1, name2, "should not be equal");
    }
  }
}
