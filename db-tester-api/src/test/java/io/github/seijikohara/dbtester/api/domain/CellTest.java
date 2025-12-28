package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    @DisplayName("of(Column, CellValue) creates cell")
    void ofColumnAndCellValueCreatesCell() {
      final var column = Column.of("name");
      final var value = new CellValue("John");
      final var cell = Cell.of(column, value);

      assertSame(column, cell.getColumn());
      assertEquals(value, cell.getValue());
      assertEquals("name", cell.getColumnName().value());
    }

    /** Verifies that of(String, CellValue) creates a cell with a column. */
    @Test
    @DisplayName("of(String, CellValue) creates cell with a column")
    void ofStringAndCellValueCreatesCell() {
      final var cell = Cell.of("email", new CellValue("test@example.com"));

      assertEquals("email", cell.getColumnName().value());
      assertEquals("test@example.com", cell.getRawValue());
    }

    /** Verifies that of(String, Object) creates a cell with raw value. */
    @Test
    @DisplayName("of(String, Object) creates cell with raw value")
    void ofStringAndObjectCreatesCell() {
      final var cell = Cell.of("age", 30);

      assertEquals("age", cell.getColumnName().value());
      assertEquals(30, cell.getRawValue());
    }

    /** Verifies that nullCell creates a NULL cell with column name. */
    @Test
    @DisplayName("nullCell creates NULL cell with column name")
    void nullCellCreatesNullCellWithColumnName() {
      final var cell = Cell.nullCell(Column.of("optional"));

      assertTrue(cell.isNull());
      assertNull(cell.getRawValue());
      assertEquals("optional", cell.getColumnName().value());
    }

    /** Verifies that nullCell creates a cell with NULL value. */
    @Test
    @DisplayName("nullCell creates cell with NULL value")
    void nullCellCreatesNullValue() {
      final var column = Column.of("notes");
      final var cell = Cell.nullCell(column);

      assertTrue(cell.isNull());
      assertSame(column, cell.getColumn());
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
    @DisplayName("getRawValue returns underlying value")
    void getRawValueReturnsUnderlyingValue() {
      final var cell = Cell.of("price", new BigDecimal("99.99"));

      assertEquals(new BigDecimal("99.99"), cell.getRawValue());
    }

    /** Verifies that getValueAsString returns a string representation. */
    @Test
    @DisplayName("getValueAsString returns string representation")
    void getValueAsStringReturnsString() {
      final var cell = Cell.of("count", 42);

      assertEquals("42", cell.getValueAsString());
    }

    /** Verifies that getValueAsString returns null for a NULL cell. */
    @Test
    @DisplayName("getValueAsString returns null for NULL cell")
    void getValueAsStringReturnsNullForNullCell() {
      final var cell = Cell.nullCell(Column.of("optional"));

      assertNull(cell.getValueAsString());
    }

    /** Verifies that getValueAsNumber returns a number for numeric value. */
    @Test
    @DisplayName("getValueAsNumber returns number for numeric value")
    void getValueAsNumberReturnsNumber() {
      final var cell = Cell.of("amount", new BigDecimal("123.45"));

      assertEquals(new BigDecimal("123.45"), cell.getValueAsNumber());
    }

    /** Verifies that getValueAsNumber parses a string to number. */
    @Test
    @DisplayName("getValueAsNumber parses string to number")
    void getValueAsNumberParsesString() {
      final var cell = Cell.of("amount", new CellValue("123.45"));

      assertNotNull(cell.getValueAsNumber());
      assertEquals(0, new BigDecimal("123.45").compareTo((BigDecimal) cell.getValueAsNumber()));
    }

    /** Verifies that getValueAsNumber returns null for non-numeric string. */
    @Test
    @DisplayName("getValueAsNumber returns null for non-numeric string")
    void getValueAsNumberReturnsNullForNonNumericString() {
      final var cell = Cell.of("name", new CellValue("John"));

      assertNull(cell.getValueAsNumber());
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
    @DisplayName("returns true for IGNORE strategy")
    void returnsTrueForIgnoreStrategy() {
      final var column = Column.builder("id").comparisonStrategy(ComparisonStrategy.IGNORE).build();
      final var cell = Cell.of(column, new CellValue(1));

      assertTrue(cell.shouldIgnore());
    }

    /** Verifies that shouldIgnore returns false for non-IGNORE strategy. */
    @Test
    @DisplayName("returns false for non-IGNORE strategy")
    void returnsFalseForNonIgnoreStrategy() {
      final var column = Column.of("name");
      final var cell = Cell.of(column, new CellValue("John"));

      assertFalse(cell.shouldIgnore());
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
    @DisplayName("uses column's comparison strategy")
    void usesColumnComparisonStrategy() {
      final var numericColumn =
          Column.builder("price").comparisonStrategy(ComparisonStrategy.NUMERIC).build();
      final var cell = Cell.of(numericColumn, new CellValue("99.99"));

      assertTrue(cell.matches(new CellValue(new BigDecimal("99.99"))));
    }

    /** Verifies that matches another cell using this column's strategy. */
    @Test
    @DisplayName("matches another cell using this column's strategy")
    void matchesAnotherCell() {
      final var column =
          Column.builder("name").comparisonStrategy(ComparisonStrategy.CASE_INSENSITIVE).build();
      final var cell1 = Cell.of(column, new CellValue("HELLO"));
      final var cell2 = Cell.of(column, new CellValue("hello"));

      assertTrue(cell1.matches(cell2));
    }

    /** Verifies that IGNORE strategy always matches. */
    @Test
    @DisplayName("IGNORE strategy always matches")
    void ignoreStrategyAlwaysMatches() {
      final var column =
          Column.builder("auto_id").comparisonStrategy(ComparisonStrategy.IGNORE).build();
      final var cell = Cell.of(column, new CellValue(1));

      assertTrue(cell.matches(new CellValue(999)));
      assertTrue(cell.matches(CellValue.NULL));
    }

    /** Verifies that REGEX strategy matches pattern. */
    @Test
    @DisplayName("REGEX strategy matches pattern")
    void regexStrategyMatchesPattern() {
      final var column = Column.builder("uuid").regexPattern("[a-f0-9-]{36}").build();
      final var cell = Cell.of(column, new CellValue("ignored"));

      assertTrue(cell.matches(new CellValue("a1b2c3d4-e5f6-7890-abcd-ef1234567890")));
      assertFalse(cell.matches(new CellValue("invalid-uuid")));
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
    @DisplayName("cells with same column and value are equal")
    void cellsWithSameColumnAndValueAreEqual() {
      final var cell1 = Cell.of("name", new CellValue("John"));
      final var cell2 = Cell.of("name", new CellValue("John"));

      assertEquals(cell1, cell2);
      assertEquals(cell1.hashCode(), cell2.hashCode());
    }

    /** Verifies that cells with different columns are not equal. */
    @Test
    @DisplayName("cells with different columns are not equal")
    void cellsWithDifferentColumnsAreNotEqual() {
      final var cell1 = Cell.of("first_name", new CellValue("John"));
      final var cell2 = Cell.of("last_name", new CellValue("John"));

      assertNotEquals(cell1, cell2);
    }

    /** Verifies that cells with different values are not equal. */
    @Test
    @DisplayName("cells with different values are not equal")
    void cellsWithDifferentValuesAreNotEqual() {
      final var cell1 = Cell.of("name", new CellValue("John"));
      final var cell2 = Cell.of("name", new CellValue("Jane"));

      assertNotEquals(cell1, cell2);
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
    @DisplayName("returns readable representation")
    void returnsReadableRepresentation() {
      final var cell = Cell.of("name", new CellValue("John"));

      assertEquals("Cell[name=John]", cell.toString());
    }

    /** Verifies that toString shows NULL for null values. */
    @Test
    @DisplayName("shows NULL for null values")
    void showsNullForNullValues() {
      final var cell = Cell.nullCell(Column.of("optional"));

      assertEquals("Cell[optional=NULL]", cell.toString());
    }
  }
}
