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
          () -> assertEquals(tableName, table.name(), "should have correct table name"),
          () -> assertEquals(columns, table.columns(), "should have correct columns"),
          () -> assertEquals(rows, table.rows(), "should have correct rows"));
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
      assertEquals(1, table.columns().size(), "columns should not be affected by modification");
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
      assertEquals(1, table.rows().size(), "rows should not be affected by modification");
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
          () -> assertEquals(tableName, table.name(), "should have correct table name"),
          () -> assertEquals(0, table.columns().size(), "should have no columns"),
          () -> assertEquals(0, table.rows().size(), "should have no rows"));
    }
  }

  /** Tests for the name() method. */
  @Nested
  @DisplayName("name() method")
  class NameMethod {

    /** Tests for the name method. */
    NameMethod() {}

    /** Verifies that getName returns table name. */
    @Test
    @Tag("normal")
    @DisplayName("should return table name when called")
    void shouldReturnTableName_whenCalled() {
      // Given
      final var tableName = new TableName("PRODUCTS");
      final var table = new SimpleTable(tableName, List.of(), List.of());

      // When
      final var result = table.name();

      // Then
      assertEquals(tableName, result, "should return correct table name");
    }
  }

  /** Tests for the columns() method. */
  @Nested
  @DisplayName("columns() method")
  class ColumnsMethod {

    /** Tests for the columns method. */
    ColumnsMethod() {}

    /** Verifies that columns returns all columns. */
    @Test
    @Tag("normal")
    @DisplayName("should return all columns when called")
    void shouldReturnAllColumns_whenCalled() {
      // Given
      final var columns =
          List.of(new ColumnName("ID"), new ColumnName("NAME"), new ColumnName("EMAIL"));
      final var table = new SimpleTable(new TableName("USERS"), columns, List.of());

      // When
      final var result = table.columns();

      // Then
      assertEquals(columns, result, "should return all columns");
    }

    /** Verifies that columns returns unmodifiable list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return unmodifiable list when called")
    void shouldReturnUnmodifiableList_whenCalled() {
      // Given
      final var columns = List.of(new ColumnName("ID"));
      final var table = new SimpleTable(new TableName("USERS"), columns, List.of());

      // When
      final var result = table.columns();

      // Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.add(new ColumnName("NAME")),
          "returned list should be unmodifiable");
    }
  }

  /** Tests for the rows() method. */
  @Nested
  @DisplayName("rows() method")
  class RowsMethod {

    /** Tests for the rows method. */
    RowsMethod() {}

    /** Verifies that rows returns all rows. */
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
      final var result = table.rows();

      // Then
      assertEquals(rows, result, "should return all rows");
    }

    /** Verifies that rows returns unmodifiable list. */
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
      final var result = table.rows();

      // Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.add(new SimpleRow(Map.of())),
          "returned list should be unmodifiable");
    }
  }

  /** Tests for the rowCount() method. */
  @Nested
  @DisplayName("rowCount() method")
  class RowCountMethod {

    /** Tests for the rowCount method. */
    RowCountMethod() {}

    /** Verifies that rowCount returns correct count. */
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
      final var result = table.rowCount();

      // Then
      assertEquals(3, result, "should return correct row count");
    }

    /** Verifies that rowCount returns zero for empty table. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return zero when table is empty")
    void shouldReturnZero_whenTableIsEmpty() {
      // Given
      final var table = new SimpleTable(new TableName("EMPTY"), List.of(), List.of());

      // When
      final var result = table.rowCount();

      // Then
      assertEquals(0, result, "should return zero for empty table");
    }
  }
}
