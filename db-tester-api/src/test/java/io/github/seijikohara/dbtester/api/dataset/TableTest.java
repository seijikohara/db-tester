package io.github.seijikohara.dbtester.api.dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
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
      assertEquals(tableName, table.getName());
      assertEquals(2, table.getColumns().size());
      assertEquals(1, table.getRows().size());
      assertEquals(1, table.getRowCount());
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
      assertTrue(table.getRows().isEmpty());
      assertEquals(0, table.getRowCount());
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
      assertEquals("users", table.getName().value());
      assertEquals(2, table.getColumns().size());
      assertEquals("id", table.getColumns().get(0).value());
      assertEquals("name", table.getColumns().get(1).value());
    }

    /** Verifies that of creates Table with empty column list. */
    @Test
    @Tag("normal")
    @DisplayName("creates Table with empty column list")
    void createsTableWithEmptyColumnList() {
      final var table = Table.of("empty_columns", List.of(), List.of());

      assertNotNull(table);
      assertTrue(table.getColumns().isEmpty());
    }
  }

  /** Tests for SimpleTable record. */
  @Nested
  @DisplayName("SimpleTable")
  class SimpleTableTest {

    /** Tests for SimpleTable record. */
    SimpleTableTest() {}

    /** Verifies that getName returns table name. */
    @Test
    @Tag("normal")
    @DisplayName("getName returns table name")
    void getNameReturnsTableName() {
      final var tableName = new TableName("test_table");
      final var table = Table.of(tableName, List.of(new ColumnName("col1")), List.of());

      assertEquals(tableName, table.getName());
    }

    /** Verifies that getColumns returns immutable list. */
    @Test
    @Tag("normal")
    @DisplayName("getColumns returns immutable list")
    void getColumnsReturnsImmutableList() {
      final var columns = List.of(new ColumnName("id"), new ColumnName("name"));
      final var table = Table.of(new TableName("test"), columns, List.of());

      final var result = table.getColumns();

      assertEquals(2, result.size());
      assertEquals("id", result.get(0).value());
      assertEquals("name", result.get(1).value());
    }

    /** Verifies that getRows returns immutable list. */
    @Test
    @Tag("normal")
    @DisplayName("getRows returns immutable list")
    void getRowsReturnsImmutableList() {
      final var row1 = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));
      final var row2 = Row.of(Map.of(new ColumnName("id"), new CellValue("2")));
      final var table =
          Table.of(new TableName("test"), List.of(new ColumnName("id")), List.of(row1, row2));

      final var result = table.getRows();

      assertEquals(2, result.size());
    }

    /** Verifies that getRowCount returns correct count. */
    @Test
    @Tag("normal")
    @DisplayName("getRowCount returns correct count")
    void getRowCountReturnsCorrectCount() {
      final var row1 = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));
      final var row2 = Row.of(Map.of(new ColumnName("id"), new CellValue("2")));
      final var row3 = Row.of(Map.of(new ColumnName("id"), new CellValue("3")));
      final var table =
          Table.of(new TableName("test"), List.of(new ColumnName("id")), List.of(row1, row2, row3));

      assertEquals(3, table.getRowCount());
    }

    /** Verifies that getRowCount equals getRows().size(). */
    @Test
    @Tag("normal")
    @DisplayName("getRowCount equals getRows().size()")
    void getRowCountEqualsGetRowsSize() {
      final var row = Row.of(Map.of(new ColumnName("id"), new CellValue("1")));
      final var table =
          Table.of(new TableName("test"), List.of(new ColumnName("id")), List.of(row));

      assertEquals(table.getRows().size(), table.getRowCount());
    }
  }
}
