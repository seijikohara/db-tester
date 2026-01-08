package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Cell}. */
@DisplayName("Cell")
class CellTest {

  /** Tests for the Cell class. */
  CellTest() {}

  /** Tests for factory methods. */
  @Nested
  @DisplayName("factory methods")
  class FactoryMethods {

    /** Tests for factory methods. */
    FactoryMethods() {}

    /** Verifies that of(Column, CellValue) creates a cell. */
    @Test
    @Tag("normal")
    @DisplayName("of(Column, CellValue) creates cell")
    void shouldCreateCell_whenColumnAndCellValueProvided() {
      // Given
      final var column = Column.of("name");
      final var value = new CellValue("John");

      // When
      final var cell = Cell.of(column, value);

      // Then
      assertAll(
          "cell should have correct column and value",
          () -> assertSame(column, cell.getColumn(), "column should be the same instance"),
          () -> assertEquals(value, cell.getValue(), "value should match"),
          () -> assertEquals("name", cell.getColumnName().value(), "column name should be 'name'"));
    }

    /** Verifies that of(String, CellValue) creates a cell with a column. */
    @Test
    @Tag("normal")
    @DisplayName("of(String, CellValue) creates cell with a column")
    void shouldCreateCellWithColumn_whenStringAndCellValueProvided() {
      // When
      final var cell = Cell.of("email", new CellValue("test@example.com"));

      // Then
      assertAll(
          "cell should have correct column name and value",
          () ->
              assertEquals("email", cell.getColumnName().value(), "column name should be 'email'"),
          () ->
              assertEquals(
                  "test@example.com",
                  cell.getRawValue().orElseThrow(),
                  "raw value should be 'test@example.com'"));
    }

    /** Verifies that of(String, Object) creates a cell with raw value. */
    @Test
    @Tag("normal")
    @DisplayName("of(String, Object) creates cell with raw value")
    void shouldCreateCellWithRawValue_whenStringAndObjectProvided() {
      // When
      final var cell = Cell.of("age", 30);

      // Then
      assertAll(
          "cell should have correct column name and raw value",
          () -> assertEquals("age", cell.getColumnName().value(), "column name should be 'age'"),
          () -> assertEquals(30, cell.getRawValue().orElseThrow(), "raw value should be 30"));
    }

    /** Verifies that nullCell creates a NULL cell with column name. */
    @Test
    @Tag("normal")
    @DisplayName("nullCell creates NULL cell with column name")
    void shouldCreateNullCell_whenColumnProvided() {
      // When
      final var cell = Cell.nullCell(Column.of("optional"));

      // Then
      assertAll(
          "null cell should have correct properties",
          () -> assertTrue(cell.isNull(), "cell should be null"),
          () -> assertTrue(cell.getRawValue().isEmpty(), "raw value should be empty"),
          () ->
              assertEquals(
                  "optional", cell.getColumnName().value(), "column name should be 'optional'"));
    }

    /** Verifies that nullCell creates a cell with NULL value. */
    @Test
    @Tag("normal")
    @DisplayName("nullCell creates cell with NULL value")
    void shouldCreateCellWithNullValue_whenColumnProvided() {
      // Given
      final var column = Column.of("notes");

      // When
      final var cell = Cell.nullCell(column);

      // Then
      assertAll(
          "null cell should have correct column",
          () -> assertTrue(cell.isNull(), "cell should be null"),
          () -> assertSame(column, cell.getColumn(), "column should be the same instance"));
    }
  }

  /** Tests for value accessors. */
  @Nested
  @DisplayName("value accessors")
  class ValueAccessors {

    /** Tests for value accessors. */
    ValueAccessors() {}

    /** Verifies that getRawValue returns the underlying value. */
    @Test
    @Tag("normal")
    @DisplayName("getRawValue returns underlying value")
    void shouldReturnUnderlyingValue_whenGetRawValueCalled() {
      // Given
      final var cell = Cell.of("price", new BigDecimal("99.99"));

      // When
      final var rawValue = cell.getRawValue();

      // Then
      assertEquals(new BigDecimal("99.99"), rawValue.orElseThrow(), "raw value should be 99.99");
    }

    /** Verifies that getValueAsString returns a string representation. */
    @Test
    @Tag("normal")
    @DisplayName("getValueAsString returns string representation")
    void shouldReturnStringRepresentation_whenGetValueAsStringCalled() {
      // Given
      final var cell = Cell.of("count", 42);

      // When
      final var valueAsString = cell.getValueAsString();

      // Then
      assertEquals("42", valueAsString.orElseThrow(), "value as string should be '42'");
    }

    /** Verifies that getValueAsString returns empty for a NULL cell. */
    @Test
    @Tag("edge-case")
    @DisplayName("getValueAsString returns empty for NULL cell")
    void shouldReturnEmpty_whenGetValueAsStringCalledOnNullCell() {
      // Given
      final var cell = Cell.nullCell(Column.of("optional"));

      // When
      final var valueAsString = cell.getValueAsString();

      // Then
      assertTrue(valueAsString.isEmpty(), "value as string should be empty for null cell");
    }

    /** Verifies that getValueAsNumber returns a number for numeric value. */
    @Test
    @Tag("normal")
    @DisplayName("getValueAsNumber returns number for numeric value")
    void shouldReturnNumber_whenGetValueAsNumberCalledOnNumericValue() {
      // Given
      final var cell = Cell.of("amount", new BigDecimal("123.45"));

      // When
      final var valueAsNumber = cell.getValueAsNumber();

      // Then
      assertEquals(
          new BigDecimal("123.45"),
          valueAsNumber.orElseThrow(),
          "value as number should be 123.45");
    }

    /** Verifies that getValueAsNumber parses a string to number. */
    @Test
    @Tag("normal")
    @DisplayName("getValueAsNumber parses string to number")
    void shouldParseStringToNumber_whenGetValueAsNumberCalled() {
      // Given
      final var cell = Cell.of("amount", new CellValue("123.45"));

      // When
      final var valueAsNumber = cell.getValueAsNumber();

      // Then
      assertAll(
          "value should be parsed as number",
          () -> assertTrue(valueAsNumber.isPresent(), "value as number should be present"),
          () ->
              assertEquals(
                  0,
                  new BigDecimal("123.45").compareTo((BigDecimal) valueAsNumber.orElseThrow()),
                  "parsed number should equal 123.45"));
    }

    /** Verifies that getValueAsNumber returns empty for non-numeric string. */
    @Test
    @Tag("edge-case")
    @DisplayName("getValueAsNumber returns empty for non-numeric string")
    void shouldReturnEmpty_whenGetValueAsNumberCalledOnNonNumericString() {
      // Given
      final var cell = Cell.of("name", new CellValue("John"));

      // When
      final var valueAsNumber = cell.getValueAsNumber();

      // Then
      assertTrue(valueAsNumber.isEmpty(), "value as number should be empty for non-numeric string");
    }
  }

  /** Tests for shouldIgnore method. */
  @Nested
  @DisplayName("shouldIgnore")
  class ShouldIgnore {

    /** Tests for shouldIgnore method. */
    ShouldIgnore() {}

    /** Verifies that shouldIgnore returns true for IGNORE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("returns true for IGNORE strategy")
    void shouldReturnTrue_whenIgnoreStrategy() {
      // Given
      final var column = Column.builder("id").comparisonStrategy(ComparisonStrategy.IGNORE).build();
      final var cell = Cell.of(column, new CellValue(1));

      // When
      final var shouldIgnore = cell.shouldIgnore();

      // Then
      assertTrue(shouldIgnore, "shouldIgnore should return true for IGNORE strategy");
    }

    /** Verifies that shouldIgnore returns false for non-IGNORE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("returns false for non-IGNORE strategy")
    void shouldReturnFalse_whenNonIgnoreStrategy() {
      // Given
      final var column = Column.of("name");
      final var cell = Cell.of(column, new CellValue("John"));

      // When
      final var shouldIgnore = cell.shouldIgnore();

      // Then
      assertFalse(shouldIgnore, "shouldIgnore should return false for non-IGNORE strategy");
    }
  }

  /** Tests for matches method. */
  @Nested
  @DisplayName("matches")
  class Matches {

    /** Tests for matches method. */
    Matches() {}

    /** Verifies that matches uses column's comparison strategy. */
    @Test
    @Tag("normal")
    @DisplayName("uses column's comparison strategy")
    void shouldUseColumnComparisonStrategy_whenMatching() {
      // Given
      final var numericColumn =
          Column.builder("price").comparisonStrategy(ComparisonStrategy.NUMERIC).build();
      final var cell = Cell.of(numericColumn, new CellValue("99.99"));

      // When
      final var matches = cell.matches(new CellValue(new BigDecimal("99.99")));

      // Then
      assertTrue(matches, "cell should match using numeric comparison strategy");
    }

    /** Verifies that matches another cell using this column's strategy. */
    @Test
    @Tag("normal")
    @DisplayName("matches another cell using this column's strategy")
    void shouldMatchAnotherCell_whenUsingColumnStrategy() {
      // Given
      final var column =
          Column.builder("name").comparisonStrategy(ComparisonStrategy.CASE_INSENSITIVE).build();
      final var cell1 = Cell.of(column, new CellValue("HELLO"));
      final var cell2 = Cell.of(column, new CellValue("hello"));

      // When
      final var matches = cell1.matches(cell2);

      // Then
      assertTrue(matches, "cells should match using case-insensitive comparison");
    }

    /** Verifies that IGNORE strategy always matches. */
    @Test
    @Tag("normal")
    @DisplayName("IGNORE strategy always matches")
    void shouldAlwaysMatch_whenIgnoreStrategy() {
      // Given
      final var column =
          Column.builder("auto_id").comparisonStrategy(ComparisonStrategy.IGNORE).build();
      final var cell = Cell.of(column, new CellValue(1));

      // When & Then
      assertAll(
          "IGNORE strategy should match any value",
          () -> assertTrue(cell.matches(new CellValue(999)), "should match different number"),
          () -> assertTrue(cell.matches(CellValue.NULL), "should match null value"));
    }

    /** Verifies that REGEX strategy matches pattern. */
    @Test
    @Tag("normal")
    @DisplayName("REGEX strategy matches pattern")
    void shouldMatchPattern_whenRegexStrategy() {
      // Given
      final var column = Column.builder("uuid").regexPattern("[a-f0-9-]{36}").build();
      final var cell = Cell.of(column, new CellValue("ignored"));

      // When & Then
      assertAll(
          "REGEX strategy should match or not match based on pattern",
          () ->
              assertTrue(
                  cell.matches(new CellValue("a1b2c3d4-e5f6-7890-abcd-ef1234567890")),
                  "should match valid UUID pattern"),
          () ->
              assertFalse(
                  cell.matches(new CellValue("invalid-uuid")),
                  "should not match invalid UUID pattern"));
    }
  }

  /** Tests for equality and hashCode. */
  @Nested
  @DisplayName("equality")
  class Equality {

    /** Tests for equality and hashCode. */
    Equality() {}

    /** Verifies that cells with same column and value are equal. */
    @Test
    @Tag("normal")
    @DisplayName("cells with same column and value are equal")
    void shouldBeEqual_whenSameColumnAndValue() {
      // Given
      final var cell1 = Cell.of("name", new CellValue("John"));
      final var cell2 = Cell.of("name", new CellValue("John"));

      // When & Then
      assertAll(
          "cells with same column and value should be equal",
          () -> assertEquals(cell1, cell2, "cells should be equal"),
          () -> assertEquals(cell1.hashCode(), cell2.hashCode(), "hash codes should be equal"));
    }

    /** Verifies that cells with different columns are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("cells with different columns are not equal")
    void shouldNotBeEqual_whenDifferentColumns() {
      // Given
      final var cell1 = Cell.of("first_name", new CellValue("John"));
      final var cell2 = Cell.of("last_name", new CellValue("John"));

      // When & Then
      assertNotEquals(cell1, cell2, "cells with different columns should not be equal");
    }

    /** Verifies that cells with different values are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("cells with different values are not equal")
    void shouldNotBeEqual_whenDifferentValues() {
      // Given
      final var cell1 = Cell.of("name", new CellValue("John"));
      final var cell2 = Cell.of("name", new CellValue("Jane"));

      // When & Then
      assertNotEquals(cell1, cell2, "cells with different values should not be equal");
    }
  }

  /** Tests for toString method. */
  @Nested
  @DisplayName("toString")
  class ToStringTests {

    /** Tests for toString method. */
    ToStringTests() {}

    /** Verifies that toString returns a readable representation. */
    @Test
    @Tag("normal")
    @DisplayName("returns readable representation")
    void shouldReturnReadableRepresentation_whenToStringCalled() {
      // Given
      final var cell = Cell.of("name", new CellValue("John"));

      // When
      final var result = cell.toString();

      // Then
      assertEquals("Cell[name=John]", result, "toString should return readable representation");
    }

    /** Verifies that toString shows NULL for null values. */
    @Test
    @Tag("edge-case")
    @DisplayName("shows NULL for null values")
    void shouldShowNull_whenValueIsNull() {
      // Given
      final var cell = Cell.nullCell(Column.of("optional"));

      // When
      final var result = cell.toString();

      // Then
      assertEquals("Cell[optional=NULL]", result, "toString should show NULL for null values");
    }
  }
}
