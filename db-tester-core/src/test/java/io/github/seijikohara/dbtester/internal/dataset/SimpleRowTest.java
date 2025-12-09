package io.github.seijikohara.dbtester.internal.dataset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SimpleRow}. */
@DisplayName("SimpleRow")
class SimpleRowTest {

  /** Tests for the SimpleRow class. */
  SimpleRowTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor stores values correctly. */
    @Test
    @Tag("normal")
    @DisplayName("should store values when valid map provided")
    void shouldStoreValues_whenValidMapProvided() {
      // Given
      final var column1 = new ColumnName("COL1");
      final var column2 = new ColumnName("COL2");
      final var value1 = new CellValue("value1");
      final var value2 = new CellValue("value2");
      final var values = Map.of(column1, value1, column2, value2);

      // When
      final var row = new SimpleRow(values);

      // Then
      final var result = row.getValues();
      assertAll(
          "row should contain all provided values",
          () -> assertEquals(2, result.size(), "should have 2 entries"),
          () -> assertEquals(value1, result.get(column1), "should have value1 for column1"),
          () -> assertEquals(value2, result.get(column2), "should have value2 for column2"));
    }

    /** Verifies that constructor creates defensive copy. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copy when map is modified after construction")
    void shouldCreateDefensiveCopy_whenMapIsModifiedAfterConstruction() {
      // Given
      final var column1 = new ColumnName("COL1");
      final var value1 = new CellValue("value1");
      final var mutableMap = new HashMap<ColumnName, CellValue>();
      mutableMap.put(column1, value1);

      // When
      final var row = new SimpleRow(mutableMap);
      mutableMap.put(new ColumnName("COL2"), new CellValue("value2"));

      // Then
      final var result = row.getValues();
      assertAll(
          "row values should not be affected by changes to original map",
          () -> assertEquals(1, result.size(), "should still have only 1 entry"),
          () -> assertEquals(value1, result.get(column1), "should still have original value"));
    }

    /** Verifies that constructor handles empty map. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty row when empty map provided")
    void shouldCreateEmptyRow_whenEmptyMapProvided() {
      // Given
      final Map<ColumnName, CellValue> emptyMap = Map.of();

      // When
      final var row = new SimpleRow(emptyMap);

      // Then
      assertEquals(0, row.getValues().size(), "should have no entries");
    }
  }

  /** Tests for the getValues() method. */
  @Nested
  @DisplayName("getValues() method")
  class GetValuesMethod {

    /** Tests for the getValues method. */
    GetValuesMethod() {}

    /** Verifies that getValues returns all values. */
    @Test
    @Tag("normal")
    @DisplayName("should return all values when called")
    void shouldReturnAllValues_whenCalled() {
      // Given
      final var column1 = new ColumnName("COL1");
      final var column2 = new ColumnName("COL2");
      final var column3 = new ColumnName("COL3");
      final var value1 = new CellValue("value1");
      final var value2 = new CellValue("value2");
      final var value3 = new CellValue(null);
      final var values = Map.of(column1, value1, column2, value2, column3, value3);
      final var row = new SimpleRow(values);

      // When
      final var result = row.getValues();

      // Then
      assertAll(
          "should return all values",
          () -> assertEquals(3, result.size(), "should have 3 entries"),
          () -> assertEquals(value1, result.get(column1), "should have value1"),
          () -> assertEquals(value2, result.get(column2), "should have value2"),
          () -> assertEquals(value3, result.get(column3), "should have null value"));
    }

    /** Verifies that getValues returns unmodifiable map. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return unmodifiable map when called")
    void shouldReturnUnmodifiableMap_whenCalled() {
      // Given
      final var column1 = new ColumnName("COL1");
      final var value1 = new CellValue("value1");
      final var values = Map.of(column1, value1);
      final var row = new SimpleRow(values);

      // When
      final var result = row.getValues();

      // Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.put(new ColumnName("COL2"), new CellValue("value2")),
          "returned map should be unmodifiable");
    }
  }

  /** Tests for the getValue(ColumnName) method. */
  @Nested
  @DisplayName("getValue(ColumnName) method")
  class GetValueMethod {

    /** Tests for the getValue method. */
    GetValueMethod() {}

    /** Verifies that getValue returns value when column exists. */
    @Test
    @Tag("normal")
    @DisplayName("should return value when column exists")
    void shouldReturnValue_whenColumnExists() {
      // Given
      final var column1 = new ColumnName("COL1");
      final var column2 = new ColumnName("COL2");
      final var value1 = new CellValue("value1");
      final var value2 = new CellValue(123);
      final var values = Map.of(column1, value1, column2, value2);
      final var row = new SimpleRow(values);

      // When
      final var result1 = row.getValue(column1);
      final var result2 = row.getValue(column2);

      // Then
      assertAll(
          "should return correct values for existing columns",
          () -> assertEquals(value1, result1, "should return value1 for column1"),
          () -> assertEquals(value2, result2, "should return value2 for column2"));
    }

    /** Verifies that getValue returns null CellValue when column does not exist. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return null CellValue when column does not exist")
    void shouldReturnNullCellValue_whenColumnDoesNotExist() {
      // Given
      final var column1 = new ColumnName("COL1");
      final var value1 = new CellValue("value1");
      final var values = Map.of(column1, value1);
      final var row = new SimpleRow(values);
      final var nonExistentColumn = new ColumnName("NON_EXISTENT");

      // When
      final var result = row.getValue(nonExistentColumn);

      // Then
      assertAll(
          "should return CellValue wrapping null for non-existent column",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertTrue(result.isNull(), "result should wrap null value"));
    }
  }
}
