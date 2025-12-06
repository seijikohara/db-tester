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

/** Unit tests for {@link SchemaName}. */
@DisplayName("SchemaName")
class SchemaNameTest {

  /** Tests for the SchemaName class. */
  SchemaNameTest() {}

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
      final var name = "PUBLIC";

      // When
      final var schemaName = new SchemaName(name);

      // Then
      assertEquals(name, schemaName.value(), "value should match");
    }

    /** Verifies that constructor trims whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should trim whitespace from name")
    void should_trim_whitespace_from_name() {
      // Given
      final var nameWithSpaces = "  APP_SCHEMA  ";

      // When
      final var schemaName = new SchemaName(nameWithSpaces);

      // Then
      assertEquals("APP_SCHEMA", schemaName.value(), "should trim whitespace");
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
              () -> new SchemaName(blankName),
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
          () -> new SchemaName(emptyName),
          "should throw IllegalArgumentException for empty name");
    }
  }

  /** Tests for the compareTo method. */
  @Nested
  @DisplayName("compareTo(SchemaName) method")
  class CompareToMethod {

    /** Tests for the compareTo method. */
    CompareToMethod() {}

    /** Verifies that compareTo orders lexicographically. */
    @Test
    @Tag("normal")
    @DisplayName("should order lexicographically")
    void should_order_lexicographically() {
      // Given
      final var app = new SchemaName("APP");
      final var appEqual = new SchemaName("APP");
      final var public_ = new SchemaName("PUBLIC");
      final var system = new SchemaName("SYSTEM");

      // When & Then
      assertAll(
          "should order lexicographically",
          () -> assertTrue(app.compareTo(public_) < 0, "APP should be before PUBLIC"),
          () -> assertTrue(public_.compareTo(system) < 0, "PUBLIC should be before SYSTEM"),
          () -> assertEquals(0, app.compareTo(appEqual), "APP should equal APP"));
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
      final var name1 = new SchemaName("PUBLIC");
      final var name2 = new SchemaName("PUBLIC");

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
      final var name1 = new SchemaName("PUBLIC");
      final var name2 = new SchemaName("PRIVATE");

      // When & Then
      assertNotEquals(name1, name2, "should not be equal");
    }
  }
}
