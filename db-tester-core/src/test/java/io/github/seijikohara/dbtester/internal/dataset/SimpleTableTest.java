package io.github.seijikohara.dbtester.internal.dataset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.seijikohara.dbtester.api.dataset.Row;
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

/** Unit tests for {@link SimpleTable}. */
@DisplayName("SimpleTable")
class SimpleTableTest {

  /** Tests for the SimpleTable class. */
  SimpleTableTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates table with valid parameters. */
    @Test
    @Tag("normal")
    @DisplayName("should create table when valid parameters provided")
    void shouldCreateTable_whenValidParametersProvided() {
      // Given
      final var tableName = new TableName("USERS");
      final var columns = List.of(new ColumnName("ID"), new ColumnName("NAME"));
      final var row1 =
          new SimpleRow(
              Map.of(
                  new ColumnName("ID"), new CellValue(1),
                  new ColumnName("NAME"), new CellValue("Alice")));
      final var rows = List.<Row>of(row1);

      // When
      final var table = new SimpleTable(tableName, columns, rows);

      // Then
      assertAll(
          "table should contain all provided values",
          () -> assertEquals(tableName, table.getName(), "should have correct table name"),
          () -> assertEquals(columns, table.getColumns(), "should have correct columns"),
          () -> assertEquals(rows, table.getRows(), "should have correct rows"));
    }

    /** Verifies that constructor creates defensive copy of columns. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copy of columns")
    void shouldCreateDefensiveCopy_ofColumns() {
      // Given
      final var tableName = new TableName("USERS");
      final var mutableColumns = new ArrayList<ColumnName>();
      mutableColumns.add(new ColumnName("ID"));
      final List<Row> rows = List.of();

      // When
      final var table = new SimpleTable(tableName, mutableColumns, rows);
      mutableColumns.add(new ColumnName("NAME"));

      // Then
      assertEquals(1, table.getColumns().size(), "columns should not be affected by modification");
    }

    /** Verifies that constructor creates defensive copy of rows. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copy of rows")
    void shouldCreateDefensiveCopy_ofRows() {
      // Given
      final var tableName = new TableName("USERS");
      final var columns = List.of(new ColumnName("ID"));
      final var mutableRows = new ArrayList<Row>();
      mutableRows.add(new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(1))));

      // When
      final var table = new SimpleTable(tableName, columns, mutableRows);
      mutableRows.add(new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(2))));

      // Then
      assertEquals(1, table.getRows().size(), "rows should not be affected by modification");
    }

    /** Verifies that constructor handles empty columns and rows. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty table when empty columns and rows provided")
    void shouldCreateEmptyTable_whenEmptyColumnsAndRowsProvided() {
      // Given
      final var tableName = new TableName("EMPTY_TABLE");
      final List<ColumnName> columns = List.of();
      final List<Row> rows = List.of();

      // When
      final var table = new SimpleTable(tableName, columns, rows);

      // Then
      assertAll(
          "table should be empty",
          () -> assertEquals(tableName, table.getName(), "should have correct table name"),
          () -> assertEquals(0, table.getColumns().size(), "should have no columns"),
          () -> assertEquals(0, table.getRows().size(), "should have no rows"));
    }
  }

  /** Tests for the getName() method. */
  @Nested
  @DisplayName("getName() method")
  class GetNameMethod {

    /** Tests for the getName method. */
    GetNameMethod() {}

    /** Verifies that getName returns table name. */
    @Test
    @Tag("normal")
    @DisplayName("should return table name when called")
    void shouldReturnTableName_whenCalled() {
      // Given
      final var tableName = new TableName("PRODUCTS");
      final var table = new SimpleTable(tableName, List.of(), List.of());

      // When
      final var result = table.getName();

      // Then
      assertEquals(tableName, result, "should return correct table name");
    }
  }

  /** Tests for the getColumns() method. */
  @Nested
  @DisplayName("getColumns() method")
  class GetColumnsMethod {

    /** Tests for the getColumns method. */
    GetColumnsMethod() {}

    /** Verifies that getColumns returns all columns. */
    @Test
    @Tag("normal")
    @DisplayName("should return all columns when called")
    void shouldReturnAllColumns_whenCalled() {
      // Given
      final var columns =
          List.of(new ColumnName("ID"), new ColumnName("NAME"), new ColumnName("EMAIL"));
      final var table = new SimpleTable(new TableName("USERS"), columns, List.of());

      // When
      final var result = table.getColumns();

      // Then
      assertEquals(columns, result, "should return all columns");
    }

    /** Verifies that getColumns returns unmodifiable list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return unmodifiable list when called")
    void shouldReturnUnmodifiableList_whenCalled() {
      // Given
      final var columns = List.of(new ColumnName("ID"));
      final var table = new SimpleTable(new TableName("USERS"), columns, List.of());

      // When
      final var result = table.getColumns();

      // Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.add(new ColumnName("NAME")),
          "returned list should be unmodifiable");
    }
  }

  /** Tests for the getRows() method. */
  @Nested
  @DisplayName("getRows() method")
  class GetRowsMethod {

    /** Tests for the getRows method. */
    GetRowsMethod() {}

    /** Verifies that getRows returns all rows. */
    @Test
    @Tag("normal")
    @DisplayName("should return all rows when called")
    void shouldReturnAllRows_whenCalled() {
      // Given
      final var columns = List.of(new ColumnName("ID"));
      final var row1 = new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(1)));
      final var row2 = new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(2)));
      final var rows = List.<Row>of(row1, row2);
      final var table = new SimpleTable(new TableName("USERS"), columns, rows);

      // When
      final var result = table.getRows();

      // Then
      assertEquals(rows, result, "should return all rows");
    }

    /** Verifies that getRows returns unmodifiable list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return unmodifiable list when called")
    void shouldReturnUnmodifiableList_whenCalled() {
      // Given
      final var columns = List.of(new ColumnName("ID"));
      final var row1 = new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(1)));
      final var rows = List.<Row>of(row1);
      final var table = new SimpleTable(new TableName("USERS"), columns, rows);

      // When
      final var result = table.getRows();

      // Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.add(new SimpleRow(Map.of())),
          "returned list should be unmodifiable");
    }
  }

  /** Tests for the getRowCount() method. */
  @Nested
  @DisplayName("getRowCount() method")
  class GetRowCountMethod {

    /** Tests for the getRowCount method. */
    GetRowCountMethod() {}

    /** Verifies that getRowCount returns correct count. */
    @Test
    @Tag("normal")
    @DisplayName("should return correct count when called")
    void shouldReturnCorrectCount_whenCalled() {
      // Given
      final var columns = List.of(new ColumnName("ID"));
      final var row1 = new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(1)));
      final var row2 = new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(2)));
      final var row3 = new SimpleRow(Map.of(new ColumnName("ID"), new CellValue(3)));
      final var rows = List.<Row>of(row1, row2, row3);
      final var table = new SimpleTable(new TableName("USERS"), columns, rows);

      // When
      final var result = table.getRowCount();

      // Then
      assertEquals(3, result, "should return correct row count");
    }

    /** Verifies that getRowCount returns zero for empty table. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return zero when table is empty")
    void shouldReturnZero_whenTableIsEmpty() {
      // Given
      final var table = new SimpleTable(new TableName("EMPTY"), List.of(), List.of());

      // When
      final var result = table.getRowCount();

      // Then
      assertEquals(0, result, "should return zero for empty table");
    }
  }
}
