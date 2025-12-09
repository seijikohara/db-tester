package io.github.seijikohara.dbtester.internal.dataset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
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

/** Unit tests for {@link SimpleDataSet}. */
@DisplayName("SimpleDataSet")
class SimpleDataSetTest {

  /** Tests for the SimpleDataSet class. */
  SimpleDataSetTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates dataset with valid tables. */
    @Test
    @Tag("normal")
    @DisplayName("should create dataset when valid tables provided")
    void shouldCreateDataSet_whenValidTablesProvided() {
      // Given
      final var table1 = createTable("USERS", List.of("ID", "NAME"));
      final var table2 = createTable("PRODUCTS", List.of("ID", "TITLE"));
      final var tables = List.<Table>of(table1, table2);

      // When
      final var dataSet = new SimpleDataSet(tables);

      // Then
      assertEquals(2, dataSet.getTables().size(), "should have 2 tables");
    }

    /** Verifies that constructor creates defensive copy of tables. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create defensive copy of tables")
    void shouldCreateDefensiveCopy_ofTables() {
      // Given
      final var mutableTables = new ArrayList<Table>();
      mutableTables.add(createTable("USERS", List.of("ID")));

      // When
      final var dataSet = new SimpleDataSet(mutableTables);
      mutableTables.add(createTable("PRODUCTS", List.of("ID")));

      // Then
      assertEquals(1, dataSet.getTables().size(), "tables should not be affected by modification");
    }

    /** Verifies that constructor handles empty table list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty dataset when empty list provided")
    void shouldCreateEmptyDataSet_whenEmptyListProvided() {
      // Given
      final List<Table> emptyTables = List.of();

      // When
      final var dataSet = new SimpleDataSet(emptyTables);

      // Then
      assertEquals(0, dataSet.getTables().size(), "should have no tables");
    }
  }

  /** Tests for the getTables() method. */
  @Nested
  @DisplayName("getTables() method")
  class GetTablesMethod {

    /** Tests for the getTables method. */
    GetTablesMethod() {}

    /** Verifies that getTables returns all tables. */
    @Test
    @Tag("normal")
    @DisplayName("should return all tables when called")
    void shouldReturnAllTables_whenCalled() {
      // Given
      final var table1 = createTable("USERS", List.of("ID"));
      final var table2 = createTable("PRODUCTS", List.of("ID"));
      final var table3 = createTable("ORDERS", List.of("ID"));
      final var tables = List.<Table>of(table1, table2, table3);
      final var dataSet = new SimpleDataSet(tables);

      // When
      final var result = dataSet.getTables();

      // Then
      assertAll(
          "should return all tables",
          () -> assertEquals(3, result.size(), "should have 3 tables"),
          () -> assertTrue(result.contains(table1), "should contain USERS table"),
          () -> assertTrue(result.contains(table2), "should contain PRODUCTS table"),
          () -> assertTrue(result.contains(table3), "should contain ORDERS table"));
    }

    /** Verifies that getTables returns unmodifiable list. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return unmodifiable list when called")
    void shouldReturnUnmodifiableList_whenCalled() {
      // Given
      final var table1 = createTable("USERS", List.of("ID"));
      final var dataSet = new SimpleDataSet(List.of(table1));

      // When
      final var result = dataSet.getTables();

      // Then
      assertThrows(
          UnsupportedOperationException.class,
          () -> result.add(createTable("NEW", List.of("ID"))),
          "returned list should be unmodifiable");
    }
  }

  /** Tests for the getTable(TableName) method. */
  @Nested
  @DisplayName("getTable(TableName) method")
  class GetTableMethod {

    /** Tests for the getTable method. */
    GetTableMethod() {}

    /** Verifies that getTable returns table when it exists. */
    @Test
    @Tag("normal")
    @DisplayName("should return table when it exists")
    void shouldReturnTable_whenItExists() {
      // Given
      final var usersTable = createTable("USERS", List.of("ID", "NAME"));
      final var productsTable = createTable("PRODUCTS", List.of("ID", "TITLE"));
      final var dataSet = new SimpleDataSet(List.of(usersTable, productsTable));

      // When
      final var result = dataSet.getTable(new TableName("USERS"));

      // Then
      assertAll(
          "should return the correct table",
          () -> assertTrue(result.isPresent(), "should be present"),
          () -> assertEquals(usersTable, result.orElseThrow(), "should return USERS table"));
    }

    /** Verifies that getTable returns empty when table does not exist. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when table does not exist")
    void shouldReturnEmpty_whenTableDoesNotExist() {
      // Given
      final var usersTable = createTable("USERS", List.of("ID"));
      final var dataSet = new SimpleDataSet(List.of(usersTable));

      // When
      final var result = dataSet.getTable(new TableName("NON_EXISTENT"));

      // Then
      assertFalse(result.isPresent(), "should return empty for non-existent table");
    }

    /** Verifies that getTable returns empty when dataset is empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when dataset is empty")
    void shouldReturnEmpty_whenDataSetIsEmpty() {
      // Given
      final var dataSet = new SimpleDataSet(List.of());

      // When
      final var result = dataSet.getTable(new TableName("USERS"));

      // Then
      assertFalse(result.isPresent(), "should return empty for empty dataset");
    }

    /** Verifies that getTable returns first match when multiple tables with same name exist. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return first match when multiple tables have same name")
    void shouldReturnFirstMatch_whenMultipleTablesHaveSameName() {
      // Given
      final var usersTable1 = createTableWithRows("USERS", List.of("ID"), 1);
      final var usersTable2 = createTableWithRows("USERS", List.of("ID"), 2);
      final var dataSet = new SimpleDataSet(List.of(usersTable1, usersTable2));

      // When
      final var result = dataSet.getTable(new TableName("USERS"));

      // Then
      assertAll(
          "should return first matching table",
          () -> assertTrue(result.isPresent(), "should be present"),
          () -> assertEquals(usersTable1, result.orElseThrow(), "should return first USERS table"));
    }
  }

  /** Tests for the getDataSource() method. */
  @Nested
  @DisplayName("getDataSource() method")
  class GetDataSourceMethod {

    /** Tests for the getDataSource method. */
    GetDataSourceMethod() {}

    /** Verifies that getDataSource returns empty. */
    @Test
    @Tag("normal")
    @DisplayName("should return empty when called")
    void shouldReturnEmpty_whenCalled() {
      // Given
      final var dataSet = new SimpleDataSet(List.of());

      // When
      final var result = dataSet.getDataSource();

      // Then
      assertFalse(result.isPresent(), "SimpleDataSet should always return empty DataSource");
    }
  }

  /**
   * Creates a simple table with the specified name and columns.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @return the created table
   */
  private static Table createTable(final String tableName, final List<String> columnNames) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    return new SimpleTable(new TableName(tableName), columns, List.of());
  }

  /**
   * Creates a table with rows.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param rowCount the number of rows to create
   * @return the created table
   */
  private static Table createTableWithRows(
      final String tableName, final List<String> columnNames, final int rowCount) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final var rows = new ArrayList<Row>();
    for (int i = 0; i < rowCount; i++) {
      rows.add(new SimpleRow(Map.of(columns.getFirst(), new CellValue(i))));
    }
    return new SimpleTable(new TableName(tableName), columns, rows);
  }
}
