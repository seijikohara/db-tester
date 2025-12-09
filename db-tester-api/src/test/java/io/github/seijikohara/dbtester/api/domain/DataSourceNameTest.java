package io.github.seijikohara.dbtester.api.domain;

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

/** Unit tests for {@link DataSourceName}. */
@DisplayName("DataSourceName")
class DataSourceNameTest {

  /** Tests for the DataSourceName class. */
  DataSourceNameTest() {}

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
      final var name = "primaryDb";

      // When
      final var dataSourceName = new DataSourceName(name);

      // Then
      assertEquals(name, dataSourceName.value(), "value should match");
    }

    /** Verifies that constructor trims whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should trim whitespace from name")
    void should_trim_whitespace_from_name() {
      // Given
      final var nameWithSpaces = "  myDataSource  ";

      // When
      final var dataSourceName = new DataSourceName(nameWithSpaces);

      // Then
      assertEquals("myDataSource", dataSourceName.value(), "should trim whitespace");
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
              () -> new DataSourceName(blankName),
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
          () -> new DataSourceName(emptyName),
          "should throw IllegalArgumentException for empty name");
    }
  }

  /** Tests for the compareTo method. */
  @Nested
  @DisplayName("compareTo(DataSourceName) method")
  class CompareToMethod {

    /** Tests for the compareTo method. */
    CompareToMethod() {}

    /** Verifies that compareTo orders lexicographically. */
    @Test
    @Tag("normal")
    @DisplayName("should order lexicographically")
    void should_order_lexicographically() {
      // Given
      final var alpha = new DataSourceName("alpha");
      final var alphaEqual = new DataSourceName("alpha");
      final var beta = new DataSourceName("beta");
      final var gamma = new DataSourceName("gamma");

      // When & Then
      assertAll(
          "should order lexicographically",
          () -> assertTrue(alpha.compareTo(beta) < 0, "alpha should be before beta"),
          () -> assertTrue(beta.compareTo(gamma) < 0, "beta should be before gamma"),
          () -> assertTrue(gamma.compareTo(alpha) > 0, "gamma should be after alpha"),
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
      final var name1 = new DataSourceName("testDb");
      final var name2 = new DataSourceName("testDb");

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
      final var name1 = new DataSourceName("db1");
      final var name2 = new DataSourceName("db2");

      // When & Then
      assertNotEquals(name1, name2, "should not be equal");
    }
  }

  /** Tests for the toString method. */
  @Nested
  @DisplayName("toString() method")
  class ToStringMethod {

    /** Tests for the toString method. */
    ToStringMethod() {}

    /** Verifies that toString contains value. */
    @Test
    @Tag("normal")
    @DisplayName("should contain value in string representation")
    void should_contain_value_in_string_representation() {
      // Given
      final var name = new DataSourceName("myDb");

      // When
      final var result = name.toString();

      // Then
      assertTrue(result.contains("myDb"), "should contain the value");
    }
  }
}
