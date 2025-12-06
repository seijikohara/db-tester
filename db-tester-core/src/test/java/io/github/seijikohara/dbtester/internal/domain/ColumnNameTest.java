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

/** Unit tests for {@link ColumnName}. */
@DisplayName("ColumnName")
class ColumnNameTest {

  /** Tests for the ColumnName class. */
  ColumnNameTest() {}

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
      final var name = "ID";

      // When
      final var columnName = new ColumnName(name);

      // Then
      assertEquals(name, columnName.value(), "value should match");
    }

    /** Verifies that constructor preserves case. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve case")
    void should_preserve_case() {
      // Given
      final var mixedCase = "UserName";

      // When
      final var columnName = new ColumnName(mixedCase);

      // Then
      assertEquals(mixedCase, columnName.value(), "should preserve case");
    }

    /** Verifies that constructor trims whitespace. */
    @Test
    @Tag("normal")
    @DisplayName("should trim whitespace from name")
    void should_trim_whitespace_from_name() {
      // Given
      final var nameWithSpaces = "  EMAIL  ";

      // When
      final var columnName = new ColumnName(nameWithSpaces);

      // Then
      assertEquals("EMAIL", columnName.value(), "should trim whitespace");
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
              () -> new ColumnName(blankName),
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
          () -> new ColumnName(emptyName),
          "should throw IllegalArgumentException for empty name");
    }

    /** Verifies that constructor accepts special characters in column names. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept special characters in column names")
    void should_accept_special_characters_in_column_names() {
      // Given
      final var nameWithSpecialChars = "CREATED_AT";

      // When
      final var columnName = new ColumnName(nameWithSpecialChars);

      // Then
      assertEquals(nameWithSpecialChars, columnName.value(), "should accept special characters");
    }
  }

  /** Tests for the compareTo method. */
  @Nested
  @DisplayName("compareTo(ColumnName) method")
  class CompareToMethod {

    /** Tests for the compareTo method. */
    CompareToMethod() {}

    /** Verifies that compareTo orders lexicographically. */
    @Test
    @Tag("normal")
    @DisplayName("should order lexicographically")
    void should_order_lexicographically() {
      // Given
      final var email = new ColumnName("EMAIL");
      final var id = new ColumnName("ID");
      final var idEqual = new ColumnName("ID");
      final var name = new ColumnName("NAME");

      // When & Then
      assertAll(
          "should order lexicographically",
          () -> assertTrue(email.compareTo(id) < 0, "EMAIL should be before ID"),
          () -> assertTrue(id.compareTo(name) < 0, "ID should be before NAME"),
          () -> assertTrue(name.compareTo(email) > 0, "NAME should be after EMAIL"),
          () -> assertEquals(0, id.compareTo(idEqual), "ID should equal ID"));
    }

    /** Verifies that compareTo is case-sensitive. */
    @Test
    @Tag("edge-case")
    @DisplayName("should be case-sensitive when comparing")
    void should_be_case_sensitive_when_comparing() {
      // Given
      final var uppercase = new ColumnName("ID");
      final var lowercase = new ColumnName("id");

      // When & Then
      assertNotEquals(0, uppercase.compareTo(lowercase), "case-sensitive comparison");
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
      final var name1 = new ColumnName("ID");
      final var name2 = new ColumnName("ID");

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
      final var name1 = new ColumnName("ID");
      final var name2 = new ColumnName("NAME");

      // When & Then
      assertNotEquals(name1, name2, "should not be equal");
    }

    /** Verifies that instances with different case are not equal. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not be equal when case differs")
    void should_not_be_equal_when_case_differs() {
      // Given
      final var uppercase = new ColumnName("ID");
      final var lowercase = new ColumnName("id");

      // When & Then
      assertNotEquals(uppercase, lowercase, "case-sensitive equality");
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
      final var columnName = new ColumnName("EMAIL");

      // When
      final var result = columnName.toString();

      // Then
      assertTrue(result.contains("EMAIL"), "should contain the value");
    }
  }
}
