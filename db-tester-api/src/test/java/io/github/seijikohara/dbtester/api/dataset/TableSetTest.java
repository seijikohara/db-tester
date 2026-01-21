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

/** Unit tests for {@link TableSet}. */
@DisplayName("TableSet")
class TableSetTest {

  /** Tests for the TableSet interface. */
  TableSetTest() {}

  /** Tests for of(List) factory method. */
  @Nested
  @DisplayName("of(List<Table>)")
  class OfListTest {

    /** Tests for of(List) factory method. */
    OfListTest() {}

    /** Verifies that of(List) creates TableSet with given tables. */
    @Test
    @Tag("normal")
    @DisplayName("creates TableSet with given tables")
    void createsTableSetWithGivenTables() {
      final var table1 =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id"), new ColumnName("name")),
              List.of());
      final var table2 =
          Table.of(
              new TableName("orders"),
              List.of(new ColumnName("id"), new ColumnName("user_id")),
              List.of());

      final var tableSet = TableSet.of(List.of(table1, table2));

      assertNotNull(tableSet);
      assertEquals(2, tableSet.getTables().size());
      assertEquals("users", tableSet.getTables().get(0).getName().value());
      assertEquals("orders", tableSet.getTables().get(1).getName().value());
    }

    /** Verifies that of(List) creates empty TableSet from empty list. */
    @Test
    @Tag("normal")
    @DisplayName("creates empty TableSet from empty list")
    void createsEmptyTableSetFromEmptyList() {
      final var tableSet = TableSet.of(List.of());

      assertNotNull(tableSet);
      assertTrue(tableSet.getTables().isEmpty());
    }
  }

  /** Tests for of(varargs) factory method. */
  @Nested
  @DisplayName("of(Table...)")
  class OfVarargsTest {

    /** Tests for of(varargs) factory method. */
    OfVarargsTest() {}

    /** Verifies that of(varargs) creates TableSet with varargs tables. */
    @Test
    @Tag("normal")
    @DisplayName("creates TableSet with varargs tables")
    void createsTableSetWithVarargsTables() {
      final var table1 =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id"), new ColumnName("name")),
              List.of());
      final var table2 =
          Table.of(
              new TableName("orders"),
              List.of(new ColumnName("id"), new ColumnName("user_id")),
              List.of());

      final var tableSet = TableSet.of(table1, table2);

      assertNotNull(tableSet);
      assertEquals(2, tableSet.getTables().size());
    }

    /** Verifies that of() creates empty TableSet with no arguments. */
    @Test
    @Tag("normal")
    @DisplayName("creates empty TableSet with no arguments")
    void createsEmptyTableSetWithNoArguments() {
      final var tableSet = TableSet.of();

      assertNotNull(tableSet);
      assertTrue(tableSet.getTables().isEmpty());
    }
  }

  /** Tests for SimpleTableSet record. */
  @Nested
  @DisplayName("SimpleTableSet")
  class SimpleTableSetTest {

    /** Tests for SimpleTableSet record. */
    SimpleTableSetTest() {}

    /** Verifies that getTables returns immutable list. */
    @Test
    @Tag("normal")
    @DisplayName("getTables returns immutable list")
    void getTablesReturnsImmutableList() {
      final var table =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id"), new ColumnName("name")),
              List.of());
      final var tableSet = TableSet.of(List.of(table));

      final var tables = tableSet.getTables();

      assertEquals(1, tables.size());
    }

    /** Verifies that getTable returns matching table. */
    @Test
    @Tag("normal")
    @DisplayName("getTable returns matching table")
    void getTableReturnsMatchingTable() {
      final var tableName = new TableName("users");
      final var table = Table.of(tableName, List.of(new ColumnName("id")), List.of());
      final var tableSet = TableSet.of(List.of(table));

      final var result = tableSet.getTable(tableName);

      assertTrue(result.isPresent());
      assertEquals(tableName, result.get().getName());
    }

    /** Verifies that getTable returns empty for non-existent table. */
    @Test
    @Tag("normal")
    @DisplayName("getTable returns empty for non-existent table")
    void getTableReturnsEmptyForNonExistentTable() {
      final var table = Table.of(new TableName("users"), List.of(new ColumnName("id")), List.of());
      final var tableSet = TableSet.of(List.of(table));

      final var result = tableSet.getTable(new TableName("orders"));

      assertTrue(result.isEmpty());
    }

    /** Verifies that getDataSource returns empty for SimpleTableSet. */
    @Test
    @Tag("normal")
    @DisplayName("getDataSource returns empty for SimpleTableSet")
    void getDataSourceReturnsEmptyForSimpleTableSet() {
      final var tableSet = TableSet.of(List.of());

      final var result = tableSet.getDataSource();

      assertTrue(result.isEmpty());
    }

    /** Verifies that getTable finds first matching table when multiple tables exist. */
    @Test
    @Tag("normal")
    @DisplayName("getTable finds first matching table when multiple tables exist")
    void getTableFindsFirstMatchingTable() {
      final var table1 =
          Table.of(
              new TableName("users"),
              List.of(new ColumnName("id")),
              List.of(Row.of(Map.of(new ColumnName("id"), new CellValue("1")))));
      final var table2 =
          Table.of(new TableName("orders"), List.of(new ColumnName("id")), List.of());
      final var tableSet = TableSet.of(List.of(table1, table2));

      final var result = tableSet.getTable(new TableName("users"));

      assertTrue(result.isPresent());
      assertEquals(1, result.get().getRows().size());
    }
  }
}
