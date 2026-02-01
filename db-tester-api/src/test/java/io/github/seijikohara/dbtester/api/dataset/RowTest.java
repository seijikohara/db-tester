package io.github.seijikohara.dbtester.api.dataset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Row}. */
@DisplayName("Row")
class RowTest {

  /** Tests for the Row interface. */
  RowTest() {}

  /** Tests for of(Map) factory method. */
  @Nested
  @DisplayName("of(Map<ColumnName, CellValue>)")
  class OfTest {

    /** Tests for of(Map) factory method. */
    OfTest() {}

    /** Verifies that of creates Row with given values. */
    @Test
    @Tag("normal")
    @DisplayName("creates Row with given values")
    void createsRowWithGivenValues() {
      final var col1 = new ColumnName("id");
      final var col2 = new ColumnName("name");
      final var values = Map.of(col1, new CellValue("1"), col2, new CellValue("John"));

      final var row = Row.of(values);

      assertNotNull(row);
      assertEquals(2, row.getValues().size());
    }

    /** Verifies that of creates Row with empty values. */
    @Test
    @Tag("normal")
    @DisplayName("creates Row with empty values")
    void createsRowWithEmptyValues() {
      final var row = Row.of(Map.of());

      assertNotNull(row);
      assertTrue(row.getValues().isEmpty());
    }
  }

  /** Tests for of(List, List) factory method. */
  @Nested
  @DisplayName("of(List<String>, List<?>)")
  class OfColumnsAndValuesTest {

    /** Tests for of(List, List) factory method. */
    OfColumnsAndValuesTest() {}

    /** Verifies that of creates Row when columns and values match. */
    @Test
    @Tag("normal")
    @DisplayName("should create row when columns and values match")
    void shouldCreateRow_whenColumnsAndValuesMatch() {
      // Given
      final var columns = List.of("ID", "NAME", "AGE");
      final List<? /* @Nullable */> values = List.of(1, "Alice", 30);

      // When
      final var row = Row.of(columns, values);

      // Then
      assertAll(
          "row should contain all column-value pairs",
          () -> assertEquals(3, row.getValues().size(), "should have 3 entries"),
          () ->
              assertEquals(
                  new CellValue(1),
                  row.getValue(new ColumnName("ID")),
                  "should have correct ID value"),
          () ->
              assertEquals(
                  new CellValue("Alice"),
                  row.getValue(new ColumnName("NAME")),
                  "should have correct NAME value"),
          () ->
              assertEquals(
                  new CellValue(30),
                  row.getValue(new ColumnName("AGE")),
                  "should have correct AGE value"));
    }

    /** Verifies that of maps null values to CellValue.NULL. */
    @Test
    @Tag("edge-case")
    @DisplayName("should map null to CellValue.NULL when null provided")
    void shouldMapNullToNullCellValue_whenNullProvided() {
      // Given
      final var columns = List.of("ID", "NAME");
      final var values = new ArrayList<>();
      values.add(1);
      values.add(null);

      // When
      final var row = Row.of(columns, values);

      // Then
      assertAll(
          "row should handle null value",
          () ->
              assertEquals(
                  new CellValue(1), row.getValue(new ColumnName("ID")), "should have ID value"),
          () ->
              assertEquals(
                  CellValue.NULL,
                  row.getValue(new ColumnName("NAME")),
                  "should have NULL for NAME"));
    }

    /** Verifies that of throws exception when sizes mismatch. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when sizes mismatch")
    void shouldThrowException_whenSizeMismatch() {
      // Given
      final var columns = List.of("ID", "NAME");
      final List<? /* @Nullable */> values = List.of(1);

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> Row.of(columns, values),
              "should throw IllegalArgumentException");
      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("does not match"),
          "exception should mention size mismatch");
    }

    /** Verifies that of creates empty row when both lists are empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty row when both lists are empty")
    void shouldCreateEmptyRow_whenBothListsAreEmpty() {
      // When
      final var row = Row.of(List.of(), List.of());

      // Then
      assertTrue(row.getValues().isEmpty(), "row should have no values");
    }
  }

  /** Tests for SimpleRow record. */
  @Nested
  @DisplayName("SimpleRow")
  class SimpleRowTest {

    /** Tests for SimpleRow record. */
    SimpleRowTest() {}

    /** Verifies that getValues returns immutable map. */
    @Test
    @Tag("normal")
    @DisplayName("getValues returns immutable map")
    void getValuesReturnsImmutableMap() {
      final var col = new ColumnName("id");
      final var row = Row.of(Map.of(col, new CellValue("1")));

      final var values = row.getValues();

      assertEquals(1, values.size());
      assertEquals(new CellValue("1"), values.get(col));
    }

    /** Verifies that getValue returns value for existing column. */
    @Test
    @Tag("normal")
    @DisplayName("getValue returns value for existing column")
    void getValueReturnsValueForExistingColumn() {
      final var col = new ColumnName("name");
      final var value = new CellValue("Alice");
      final var row = Row.of(Map.of(col, value));

      final var result = row.getValue(col);

      assertEquals(value, result);
    }

    /** Verifies that getValue returns NULL for non-existent column. */
    @Test
    @Tag("normal")
    @DisplayName("getValue returns NULL for non-existent column")
    void getValueReturnsNullForNonExistentColumn() {
      final var col = new ColumnName("id");
      final var row = Row.of(Map.of(col, new CellValue("1")));

      final var result = row.getValue(new ColumnName("non_existent"));

      assertEquals(CellValue.NULL, result);
    }

    /** Verifies that getValue returns NULL for empty row. */
    @Test
    @Tag("normal")
    @DisplayName("getValue returns NULL for empty row")
    void getValueReturnsNullForEmptyRow() {
      final var row = Row.of(Map.of());

      final var result = row.getValue(new ColumnName("any"));

      assertEquals(CellValue.NULL, result);
    }

    /** Verifies that getValues preserves all column-value pairs. */
    @Test
    @Tag("normal")
    @DisplayName("getValues preserves all column-value pairs")
    void getValuesPreservesAllColumnValuePairs() {
      final var col1 = new ColumnName("id");
      final var col2 = new ColumnName("name");
      final var col3 = new ColumnName("email");
      final var values =
          Map.of(
              col1, new CellValue("1"),
              col2, new CellValue("John"),
              col3, new CellValue("john@example.com"));

      final var row = Row.of(values);

      assertEquals(3, row.getValues().size());
      assertEquals(new CellValue("1"), row.getValue(col1));
      assertEquals(new CellValue("John"), row.getValue(col2));
      assertEquals(new CellValue("john@example.com"), row.getValue(col3));
    }
  }
}
