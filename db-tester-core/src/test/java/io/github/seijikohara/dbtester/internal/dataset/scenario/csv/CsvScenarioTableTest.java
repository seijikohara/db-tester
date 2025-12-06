package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CsvScenarioTable}. */
@DisplayName("CsvScenarioTable")
class CsvScenarioTableTest {

  /** Tests for the CsvScenarioTable class. */
  CsvScenarioTableTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /**
     * Verifies that constructor creates table without scenario column when first column is not
     * scenario marker.
     */
    @Test
    @Tag("normal")
    @DisplayName(
        "should create table without scenario column when first column is not scenario marker")
    void shouldCreateTableWithoutScenarioColumn_whenFirstColumnIsNotScenarioMarker() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var col1 = new ColumnName("COL1");
      final var col2 = new ColumnName("COL2");
      final var col3 = new ColumnName("COL3");
      final var columns = List.of(col1, col2, col3);

      final var row1 = createRow(Map.of(col1, "A", col2, "B", col3, "C"));
      final var row2 = createRow(Map.of(col1, "D", col2, "E", col3, "F"));
      final var rows = List.of(row1, row2);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var table = new CsvScenarioTable(sourceTable, List.of(), scenarioMarker);

      // Then
      assertAll(
          "table should contain all columns and rows",
          () -> assertEquals(tableName, table.getName(), "should return table name"),
          () -> assertEquals(columns, table.getColumns(), "should return all columns"),
          () -> assertEquals(2, table.getRows().size(), "should return all rows"));
    }

    /** Verifies that constructor excludes scenario column when first column is scenario marker. */
    @Test
    @Tag("normal")
    @DisplayName("should exclude scenario column when first column is scenario marker")
    void shouldExcludeScenarioColumn_whenFirstColumnIsScenarioMarker() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var col2 = new ColumnName("COL2");
      final var columns = List.of(scenarioCol, col1, col2);

      final var row1 = createRow(Map.of(scenarioCol, "scenario1", col1, "A", col2, "B"));
      final var row2 = createRow(Map.of(scenarioCol, "scenario2", col1, "C", col2, "D"));
      final var rows = List.of(row1, row2);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var table = new CsvScenarioTable(sourceTable, List.of(), scenarioMarker);

      // Then
      assertAll(
          "scenario column should be excluded",
          () ->
              assertEquals(
                  List.of(col1, col2), table.getColumns(), "should exclude scenario column"),
          () -> assertEquals(2, table.getRows().size(), "should return all rows"),
          () -> {
            final var firstRow = table.getRows().get(0);
            assertEquals(new DataValue("A"), firstRow.getValue(col1), "should have COL1 value");
            assertEquals(new DataValue("B"), firstRow.getValue(col2), "should have COL2 value");
          });
    }

    /** Verifies that constructor includes all rows when scenario names is empty. */
    @Test
    @Tag("normal")
    @DisplayName("should include all rows when scenario names is empty")
    void shouldIncludeAllRows_whenScenarioNamesIsEmpty() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(scenarioCol, col1);

      final var row1 = createRow(Map.of(scenarioCol, "scenario1", col1, "A"));
      final var row2 = createRow(Map.of(scenarioCol, "scenario2", col1, "B"));
      final var rows = List.of(row1, row2);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var table = new CsvScenarioTable(sourceTable, List.of(), scenarioMarker);

      // Then
      assertEquals(
          2, table.getRows().size(), "should include all rows when scenario names is empty");
    }

    /** Verifies that constructor filters rows by scenario names when provided. */
    @Test
    @Tag("normal")
    @DisplayName("should filter rows by scenario names when scenario names provided")
    void shouldFilterRowsByScenarioNames_whenScenarioNamesProvided() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(scenarioCol, col1);

      final var row1 = createRow(Map.of(scenarioCol, "scenario1", col1, "A"));
      final var row2 = createRow(Map.of(scenarioCol, "scenario2", col1, "B"));
      final var row3 = createRow(Map.of(scenarioCol, "scenario1", col1, "C"));
      final var rows = List.of(row1, row2, row3);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));

      // When
      final var table = new CsvScenarioTable(sourceTable, scenarioNames, scenarioMarker);

      // Then
      assertAll(
          "should filter rows by scenario names",
          () -> assertEquals(2, table.getRows().size(), "should include only matching rows"),
          () -> {
            final var firstRow = table.getRows().get(0);
            assertEquals(
                new DataValue("A"), firstRow.getValue(col1), "first row should have value A");
          },
          () -> {
            final var secondRow = table.getRows().get(1);
            assertEquals(
                new DataValue("C"), secondRow.getValue(col1), "second row should have value C");
          });
    }

    /** Verifies that constructor trims scenario values when comparing with scenario names. */
    @Test
    @Tag("edge-case")
    @DisplayName("should trim scenario values when comparing with scenario names")
    void shouldTrimScenarioValues_whenComparingWithScenarioNames() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(scenarioCol, col1);

      final var row1 = createRow(Map.of(scenarioCol, " scenario1 ", col1, "A"));
      final var row2 = createRow(Map.of(scenarioCol, "scenario2", col1, "B"));
      final var rows = List.of(row1, row2);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));

      // When
      final var table = new CsvScenarioTable(sourceTable, scenarioNames, scenarioMarker);

      // Then
      assertAll(
          "should trim scenario values before comparison",
          () ->
              assertEquals(
                  1, table.getRows().size(), "should include row with trimmed scenario value"),
          () -> {
            final var firstRow = table.getRows().get(0);
            assertEquals(new DataValue("A"), firstRow.getValue(col1), "should have value A");
          });
    }

    /**
     * Verifies that constructor includes rows with null scenario value when scenario names
     * provided.
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should include rows with null scenario value when scenario names provided")
    void shouldIncludeRowsWithNullScenarioValue_whenScenarioNamesProvided() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(scenarioCol, col1);

      final var row1 = createRowWithNullValue(scenarioCol, col1, "A");
      final var rows = List.of(row1);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));

      // When
      final var table = new CsvScenarioTable(sourceTable, scenarioNames, scenarioMarker);

      // Then
      assertEquals(1, table.getRows().size(), "should include rows with null scenario value");
    }

    /**
     * Verifies that constructor includes rows with blank scenario value when scenario names
     * provided.
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should include rows with blank scenario value when scenario names provided")
    void shouldIncludeRowsWithBlankScenarioValue_whenScenarioNamesProvided() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(scenarioCol, col1);

      final var row1 = createRow(Map.of(scenarioCol, "", col1, "A"));
      final var row2 = createRow(Map.of(scenarioCol, "  ", col1, "B"));
      final var rows = List.of(row1, row2);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));

      // When
      final var table = new CsvScenarioTable(sourceTable, scenarioNames, scenarioMarker);

      // Then
      assertEquals(2, table.getRows().size(), "should include rows with blank scenario value");
    }

    /** Verifies that constructor normalizes empty strings to null when creating rows. */
    @Test
    @Tag("normal")
    @DisplayName("should normalize empty strings to null when creating rows")
    void shouldNormalizeEmptyStringsToNull_whenCreatingRows() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var col1 = new ColumnName("COL1");
      final var col2 = new ColumnName("COL2");
      final var col3 = new ColumnName("COL3");
      final var columns = List.of(col1, col2, col3);

      final var row1 = createRow(Map.of(col1, "", col2, "value", col3, ""));
      final var rows = List.of(row1);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var table = new CsvScenarioTable(sourceTable, List.of(), scenarioMarker);

      // Then
      assertAll(
          "empty strings should be normalized to null",
          () -> assertEquals(1, table.getRows().size(), "should have one row"),
          () -> {
            final var firstRow = table.getRows().get(0);
            assertTrue(firstRow.getValue(col1).isNull(), "COL1 should be null");
            assertEquals(new DataValue("value"), firstRow.getValue(col2), "COL2 should have value");
            assertTrue(firstRow.getValue(col3).isNull(), "COL3 should be null");
          });
    }

    /** Verifies that constructor creates empty table when source table has no rows. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create empty table when source table has no rows")
    void shouldCreateEmptyTable_whenSourceTableHasNoRows() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(col1);
      final var rows = List.<Row>of();

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var table = new CsvScenarioTable(sourceTable, List.of(), scenarioMarker);

      // Then
      assertEquals(0, table.getRows().size(), "should create table with no rows");
    }

    /** Verifies that constructor creates table with no rows when all rows filtered out. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create table with no rows when all rows filtered out")
    void shouldCreateTableWithNoRows_whenAllRowsFilteredOut() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(scenarioCol, col1);

      final var row1 = createRow(Map.of(scenarioCol, "scenario2", col1, "A"));
      final var rows = List.of(row1);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));

      // When
      final var table = new CsvScenarioTable(sourceTable, scenarioNames, scenarioMarker);

      // Then
      assertEquals(
          0, table.getRows().size(), "should create table with no rows when all filtered out");
    }
  }

  /** Tests for the getName() method. */
  @Nested
  @DisplayName("getName() method")
  class GetNameMethod {

    /** Tests for the getName method. */
    GetNameMethod() {}

    /** Verifies that getName returns table name when called. */
    @Test
    @Tag("normal")
    @DisplayName("should return table name when called")
    void shouldReturnTableName_whenCalled() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(col1);
      final var rows = List.<Row>of();

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var table = new CsvScenarioTable(sourceTable, List.of(), scenarioMarker);

      // When
      final var result = table.getName();

      // Then
      assertEquals(tableName, result, "should return table name");
    }
  }

  /** Tests for the getColumns() method. */
  @Nested
  @DisplayName("getColumns() method")
  class GetColumnsMethod {

    /** Tests for the getColumns method. */
    GetColumnsMethod() {}

    /** Verifies that getColumns returns data columns when called. */
    @Test
    @Tag("normal")
    @DisplayName("should return data columns when called")
    void shouldReturnDataColumns_whenCalled() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var col2 = new ColumnName("COL2");
      final var columns = List.of(scenarioCol, col1, col2);
      final var rows = List.<Row>of();

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var table = new CsvScenarioTable(sourceTable, List.of(), scenarioMarker);

      // When
      final var result = table.getColumns();

      // Then
      assertEquals(
          List.of(col1, col2), result, "should return data columns excluding scenario column");
    }
  }

  /** Tests for the getRows() method. */
  @Nested
  @DisplayName("getRows() method")
  class GetRowsMethod {

    /** Tests for the getRows method. */
    GetRowsMethod() {}

    /** Verifies that getRows returns filtered rows when called. */
    @Test
    @Tag("normal")
    @DisplayName("should return filtered rows when called")
    void shouldReturnFilteredRows_whenCalled() {
      // Given
      final var tableName = new TableName("TEST_TABLE");
      final var scenarioCol = new ColumnName("SCENARIO");
      final var col1 = new ColumnName("COL1");
      final var columns = List.of(scenarioCol, col1);

      final var row1 = createRow(Map.of(scenarioCol, "scenario1", col1, "A"));
      final var row2 = createRow(Map.of(scenarioCol, "scenario2", col1, "B"));
      final var rows = List.of(row1, row2);

      final var sourceTable = createMockTable(tableName, columns, rows);
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));
      final var table = new CsvScenarioTable(sourceTable, scenarioNames, scenarioMarker);

      // When
      final var result = table.getRows();

      // Then
      assertAll(
          "should return filtered rows",
          () -> assertEquals(1, result.size(), "should return only matching rows"),
          () ->
              assertEquals(
                  new DataValue("A"), result.get(0).getValue(col1), "should have correct value"));
    }
  }

  /**
   * Creates a mock Table with the specified properties.
   *
   * @param tableName the table name
   * @param columns the columns
   * @param rows the rows
   * @return mock Table instance
   */
  private static Table createMockTable(
      final TableName tableName, final List<ColumnName> columns, final List<Row> rows) {
    final var mockTable = mock(Table.class);
    when(mockTable.getName()).thenReturn(tableName);
    when(mockTable.getColumns()).thenReturn(columns);
    when(mockTable.getRows()).thenReturn(rows);
    return mockTable;
  }

  /**
   * Creates a Row with the specified column values.
   *
   * @param values map of column names to values
   * @return Row instance
   */
  private static Row createRow(final Map<ColumnName, Object> values) {
    final var mockRow = mock(Row.class);
    values.forEach(
        (column, value) -> {
          final var dataValue = new DataValue(value);
          when(mockRow.getValue(column)).thenReturn(dataValue);
        });
    return mockRow;
  }

  /**
   * Creates a Row with a null scenario value and a non-null data value.
   *
   * @param scenarioColumn the scenario column name
   * @param dataColumn the data column name
   * @param dataValue the data value
   * @return Row instance
   */
  private static Row createRowWithNullValue(
      final ColumnName scenarioColumn, final ColumnName dataColumn, final Object dataValue) {
    final var mockRow = mock(Row.class);
    when(mockRow.getValue(scenarioColumn)).thenReturn(new DataValue(null));
    when(mockRow.getValue(dataColumn)).thenReturn(new DataValue(dataValue));
    return mockRow;
  }
}
