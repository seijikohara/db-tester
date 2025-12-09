package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CellValue}. */
@DisplayName("CellValue")
class CellValueTest {

  /** Tests for the CellValue class. */
  CellValueTest() {}

  /** Tests for the NULL constant. */
  @Nested
  @DisplayName("NULL constant")
  class NullConstant {

    /** Tests for the NULL constant. */
    NullConstant() {}

    /** Verifies that NULL constant is not null. */
    @Test
    @Tag("normal")
    @DisplayName("should not be null")
    void should_not_be_null() {
      // Given & When & Then
      assertNotNull(CellValue.NULL, "NULL constant should not be null");
    }

    /** Verifies that NULL constant has null value. */
    @Test
    @Tag("normal")
    @DisplayName("should have null value")
    void should_have_null_value() {
      // Given & When
      final var value = CellValue.NULL.value();

      // Then
      assertNull(value, "NULL constant should have null value");
    }

    /** Verifies that NULL constant isNull returns true. */
    @Test
    @Tag("normal")
    @DisplayName("should return true for isNull")
    void should_return_true_for_is_null() {
      // Given & When
      final var result = CellValue.NULL.isNull();

      // Then
      assertTrue(result, "NULL constant isNull should return true");
    }
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class Constructor {

    /** Tests for the constructor. */
    Constructor() {}

    /** Verifies that constructor accepts non-null values. */
    @Test
    @Tag("normal")
    @DisplayName("should accept non-null values")
    void should_accept_non_null_values() {
      // Given
      final var stringValue = "test";

      // When
      final var cellValue = new CellValue(stringValue);

      // Then
      assertEquals(stringValue, cellValue.value(), "should store the value");
    }

    /** Verifies that constructor accepts null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("should accept null values")
    void should_accept_null_values() {
      // Given & When
      final var cellValue = new CellValue(null);

      // Then
      assertNull(cellValue.value(), "should store null value");
    }

    /** Verifies that constructor accepts various types. */
    @Test
    @Tag("normal")
    @DisplayName("should accept various types")
    void should_accept_various_types() {
      // Given & When & Then
      assertAll(
          "should accept various types",
          () -> assertEquals(42, new CellValue(42).value(), "should accept Integer"),
          () -> assertEquals(3.14, new CellValue(3.14).value(), "should accept Double"),
          () -> assertEquals(true, new CellValue(true).value(), "should accept Boolean"),
          () ->
              assertEquals(
                  BigDecimal.TEN,
                  new CellValue(BigDecimal.TEN).value(),
                  "should accept BigDecimal"),
          () ->
              assertEquals(
                  LocalDate.of(2024, 1, 15),
                  new CellValue(LocalDate.of(2024, 1, 15)).value(),
                  "should accept LocalDate"));
    }
  }

  /** Tests for the isNull method. */
  @Nested
  @DisplayName("isNull() method")
  class IsNullMethod {

    /** Tests for the isNull method. */
    IsNullMethod() {}

    /** Verifies that isNull returns true for null value. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when value is null")
    void should_return_true_when_value_is_null() {
      // Given
      final var cellValue = new CellValue(null);

      // When
      final var result = cellValue.isNull();

      // Then
      assertTrue(result, "should return true for null value");
    }

    /** Verifies that isNull returns false for non-null value. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when value is not null")
    void should_return_false_when_value_is_not_null() {
      // Given
      final var cellValue = new CellValue("test");

      // When
      final var result = cellValue.isNull();

      // Then
      assertFalse(result, "should return false for non-null value");
    }

    /** Verifies that isNull returns true for NULL constant. */
    @Test
    @Tag("normal")
    @DisplayName("should return true for NULL constant")
    void should_return_true_for_null_constant() {
      // Given & When
      final var result = CellValue.NULL.isNull();

      // Then
      assertTrue(result, "should return true for NULL constant");
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
      final var value1 = new CellValue("test");
      final var value2 = new CellValue("test");

      // When & Then
      assertAll(
          "should be equal",
          () -> assertEquals(value1, value2, "should be equal"),
          () -> assertEquals(value1.hashCode(), value2.hashCode(), "hashCodes should match"));
    }

    /** Verifies that instances with null values are equal. */
    @Test
    @Tag("edge-case")
    @DisplayName("should be equal when both values are null")
    void should_be_equal_when_both_values_are_null() {
      // Given
      final var value1 = new CellValue(null);
      final var value2 = new CellValue(null);

      // When & Then
      assertAll(
          "should be equal with null values",
          () -> assertEquals(value1, value2, "should be equal"),
          () -> assertEquals(value1, CellValue.NULL, "should equal NULL constant"));
    }

    /** Verifies that instances with different values are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal when values differ")
    void should_not_be_equal_when_values_differ() {
      // Given
      final var value1 = new CellValue("test1");
      final var value2 = new CellValue("test2");

      // When & Then
      assertNotEquals(value1, value2, "should not be equal");
    }

    /** Verifies that null value and non-null value are not equal. */
    @Test
    @Tag("edge-case")
    @DisplayName("should not be equal when one is null and other is not")
    void should_not_be_equal_when_one_is_null_and_other_is_not() {
      // Given
      final var nullValue = new CellValue(null);
      final var nonNullValue = new CellValue("test");

      // When & Then
      assertNotEquals(nullValue, nonNullValue, "should not be equal");
    }
  }

  /** Tests for singleton usage of NULL. */
  @Nested
  @DisplayName("NULL singleton")
  class NullSingleton {

    /** Tests for NULL singleton. */
    NullSingleton() {}

    /** Verifies that NULL constant should be used for null values. */
    @Test
    @Tag("normal")
    @DisplayName("should prefer NULL constant over new instance for null values")
    void should_prefer_null_constant_over_new_instance() {
      // Given
      final var newNullValue = new CellValue(null);

      // When & Then
      assertAll(
          "NULL constant should be preferred",
          () -> assertEquals(CellValue.NULL, newNullValue, "should be equal to NULL constant"),
          () ->
              assertSame(
                  CellValue.NULL.value(),
                  newNullValue.value(),
                  "underlying null values should be same"));
    }
  }
}
