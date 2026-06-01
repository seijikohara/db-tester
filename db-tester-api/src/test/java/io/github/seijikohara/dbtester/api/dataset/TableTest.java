package io.github.seijikohara.dbtester.api.dataset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Table}. */
@DisplayName("Table")
class TableTest {

  /** Tests for the Table interface. */
  TableTest() {}

  /** Tests for of(TableName, List, List) factory method. */
  @Nested
  @DisplayName("of(TableName, List<ColumnName>, List<Row>)")
  class OfWithTableNameTest {

    /** Tests for of(TableName, List, List) factory method. */
    OfWithTableNameTest() {}

    /** Verifies that of creates Table with given parameters. */
    @Test
    @Tag("normal")
    @DisplayName("creates Table with given parameters")
    void createsTableWithGivenParameters() {
      final var tableName = new TableName("users");
      final var columns = List.of(new ColumnName("id"), new ColumnName("name"));
      final var row = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));
      final var rows = List.of(row);

      final var table = Table.of(tableName, columns, rows);

      assertNotNull(table);
      assertEquals(tableName, table.name());
      assertEquals(2, table.columns().size());
      assertEquals(1, table.rows().size());
      assertEquals(1, table.rowCount());
    }

    /** Verifies that of creates Table with empty rows. */
    @Test
    @Tag("normal")
    @DisplayName("creates Table with empty rows")
    void createsTableWithEmptyRows() {
      final var tableName = new TableName("empty_table");
      final var columns = List.of(new ColumnName("id"));

      final var table = Table.of(tableName, columns, List.of());

      assertNotNull(table);
      assertTrue(table.rows().isEmpty());
      assertEquals(0, table.rowCount());
    }
  }

  /** Tests for of(String, List, List) factory method. */
  @Nested
  @DisplayName("of(String, List<String>, List<Row>)")
  class OfWithStringTest {

    /** Tests for of(String, List, List) factory method. */
    OfWithStringTest() {}

    /** Verifies that of creates Table with string parameters. */
    @Test
    @Tag("normal")
    @DisplayName("creates Table with string parameters")
    void createsTableWithStringParameters() {
      final var row = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));

      final var table = Table.of("users", List.of("id", "name"), List.of(row));

      assertNotNull(table);
      assertEquals("users", table.name().value());
      assertEquals(2, table.columns().size());
      assertEquals("id", table.columns().get(0).value());
      assertEquals("name", table.columns().get(1).value());
    }

    /** Verifies that of creates Table with empty column list. */
    @Test
    @Tag("normal")
    @DisplayName("creates Table with empty column list")
    void createsTableWithEmptyColumnList() {
      final var table = Table.of("empty_columns", List.of(), List.of());

      assertNotNull(table);
      assertTrue(table.columns().isEmpty());
    }
  }

  /** Tests for ofValues(String, List, List) factory method. */
  @Nested
  @DisplayName("ofValues(String, List<String>, List<List<?>>)")
  class OfValuesTest {

    /** Tests for ofValues factory method. */
    OfValuesTest() {}

    /** Verifies that ofValues creates Table with valid input. */
    @Test
    @Tag("normal")
    @DisplayName("should create table when valid input provided")
    void shouldCreateTable_whenValidInputProvided() {
      // Given
      final var columns = List.of("ID", "NAME", "AGE");
      final List<List<? /* @Nullable */>> rows =
          List.of(List.of(1, "Alice", 30), List.of(2, "Bob", 25));

      // When
      final var table = Table.ofValues("USERS", columns, rows);

      // Then
      assertAll(
          "table should contain correct structure and data",
          () -> assertEquals("USERS", table.name().value(), "should have correct table name"),
          () -> assertEquals(3, table.columns().size(), "should have 3 columns"),
          () -> assertEquals("ID", table.columns().get(0).value(), "first column should be ID"),
          () ->
              assertEquals("NAME", table.columns().get(1).value(), "second column should be NAME"),
          () -> assertEquals("AGE", table.columns().get(2).value(), "third column should be AGE"),
          () -> assertEquals(2, table.rowCount(), "should have 2 rows"));
    }

    /** Verifies that ofValues handles null values in rows. */
    @Test
    @Tag("edge-case")
    @DisplayName("should map null values to CellValue.NULL")
    void shouldHandleNullValues_whenNullInRow() {
      // Given
      final var columns = List.of("ID", "NAME");
      final var rowWithNull = new ArrayList<>();
      rowWithNull.add(1);
      rowWithNull.add(null);
      final List<List<? /* @Nullable */>> rows = List.of(rowWithNull);

      // When
      final var table = Table.ofValues("USERS", columns, rows);

      // Then
      final var row = table.rows().get(0);
      assertAll(
          "row should handle null value correctly",
          () ->
              assertEquals(
                  new CellValue(1), row.value(new ColumnName("ID")), "should have ID value"),
          () ->
              assertEquals(
                  CellValue.NULL, row.value(new ColumnName("NAME")), "should have NULL for NAME"));
    }

    /** Verifies that ofValues throws exception when row size mismatches column count. */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when row size mismatches column count")
    void shouldThrowException_whenRowSizeMismatch() {
      // Given
      final var columns = List.of("ID", "NAME");
      final List<List<? /* @Nullable */>> rows = List.of(List.of(1)); // Only 1 value for 2 columns

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> Table.ofValues("USERS", columns, rows),
              "should throw IllegalArgumentException");
      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("does not match"),
          "exception should mention size mismatch");
    }

    /** Verifies that ofValues creates empty table when no rows provided. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty table when no rows provided")
    void shouldCreateEmptyTable_whenNoRowsProvided() {
      // When
      final var table = Table.ofValues("EMPTY", List.of("ID"), List.of());

      // Then
      assertAll(
          "empty table should have correct structure",
          () -> assertEquals("EMPTY", table.name().value(), "should have correct table name"),
          () -> assertEquals(1, table.columns().size(), "should have 1 column"),
          () -> assertEquals(0, table.rowCount(), "should have 0 rows"),
          () -> assertTrue(table.rows().isEmpty(), "rows should be empty"));
    }
  }

  /** Tests for SimpleTable record. */
  @Nested
  @DisplayName("SimpleTable")
  class SimpleTableTest {

    /** Tests for SimpleTable record. */
    SimpleTableTest() {}

    /** Verifies that name returns table name. */
    @Test
    @Tag("normal")
    @DisplayName("name returns table name")
    void nameReturnsTableName() {
      final var tableName = new TableName("test_table");
      final var table = Table.of(tableName, List.of(new ColumnName("col1")), List.of());

      assertEquals(tableName, table.name());
    }

    /** Verifies that columns returns immutable list. */
    @Test
    @Tag("normal")
    @DisplayName("columns returns immutable list")
    void columnsReturnsImmutableList() {
      final var columns = List.of(new ColumnName("id"), new ColumnName("name"));
      final var table = Table.of(new TableName("test"), columns, List.of());

      final var result = table.columns();

      assertEquals(2, result.size());
      assertEquals("id", result.get(0).value());
      assertEquals("name", result.get(1).value());
    }

    /** Verifies that rows returns immutable list. */
    @Test
    @Tag("normal")
    @DisplayName("rows returns immutable list")
    void rowsReturnsImmutableList() {
      final var row1 = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));
      final var row2 = Row.of(Map.of(new ColumnName("id"), new CellValue("2")));
      final var table =
          Table.of(new TableName("test"), List.of(new ColumnName("id")), List.of(row1, row2));

      final var result = table.rows();

      assertEquals(2, result.size());
    }

    /** Verifies that rowCount returns correct count. */
    @Test
    @Tag("normal")
    @DisplayName("rowCount returns correct count")
    void rowCountReturnsCorrectCount() {
      final var row1 = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));
      final var row2 = Row.of(Map.of(new ColumnName("id"), new CellValue("2")));
      final var row3 = Row.of(Map.of(new ColumnName("id"), new CellValue("3")));
      final var table =
          Table.of(new TableName("test"), List.of(new ColumnName("id")), List.of(row1, row2, row3));

      assertEquals(3, table.rowCount());
    }

    /** Verifies that rowCount equals rows().size(). */
    @Test
    @Tag("normal")
    @DisplayName("rowCount equals rows().size()")
    void rowCountEqualsRowsSize() {
      final var row = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));
      final var table =
          Table.of(new TableName("test"), List.of(new ColumnName("id")), List.of(row));

      assertEquals(table.rows().size(), table.rowCount());
    }
  }
}
