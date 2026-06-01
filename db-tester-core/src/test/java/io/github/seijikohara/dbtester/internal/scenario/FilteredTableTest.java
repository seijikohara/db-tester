package io.github.seijikohara.dbtester.internal.scenario;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.scenario.ScenarioName;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link FilteredTable}. */
@DisplayName("FilteredTable")
class FilteredTableTest {

  /** Tests for the FilteredTable class. */
  FilteredTableTest() {}

  /** The filter for testing. */
  private ScenarioFilter filter;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    filter = new ScenarioFilter(new ScenarioMarker("$scenario"), Set.of(new ScenarioName("test")));
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when called. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // Given
      final var sourceTable = createMockTableWithScenario();

      // When
      final var result = new FilteredTable(sourceTable, filter);

      // Then
      assertNotNull(result, "instance should not be null");
    }
  }

  /** Tests for the name() method. */
  @Nested
  @DisplayName("name() method")
  class NameMethod {

    /** Tests for the name method. */
    NameMethod() {}

    /** Verifies that name returns table name. */
    @Test
    @Tag("normal")
    @DisplayName("should return table name when called")
    void shouldReturnTableName_whenCalled() {
      // Given
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.name();

      // Then
      assertEquals(new TableName("users"), result, "should return correct table name");
    }
  }

  /** Tests for the columns() method. */
  @Nested
  @DisplayName("columns() method")
  class ColumnsMethod {

    /** Tests for the columns method. */
    ColumnsMethod() {}

    /** Verifies that columns excludes scenario column. */
    @Test
    @Tag("normal")
    @DisplayName("should exclude scenario column when scenario column exists")
    void shouldExcludeScenarioColumn_whenScenarioColumnExists() {
      // Given
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.columns();

      // Then
      assertAll(
          "columns should exclude scenario column",
          () -> assertEquals(2, result.size(), "should have 2 columns"),
          () ->
              assertFalse(
                  result.contains(new ColumnName("$scenario")),
                  "should not contain scenario column"),
          () -> assertEquals(new ColumnName("ID"), result.get(0), "first column should be ID"),
          () ->
              assertEquals(new ColumnName("NAME"), result.get(1), "second column should be NAME"));
    }

    /** Verifies that columns returns all columns when no scenario column. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return all columns when no scenario column exists")
    void shouldReturnAllColumns_whenNoScenarioColumnExists() {
      // Given
      final var sourceTable = createMockTableWithoutScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.columns();

      // Then
      assertEquals(2, result.size(), "should have all 2 columns");
    }
  }

  /** Tests for the rows() method. */
  @Nested
  @DisplayName("rows() method")
  class RowsMethod {

    /** Tests for the rows method. */
    RowsMethod() {}

    /** Verifies that rows returns filtered rows. */
    @Test
    @Tag("normal")
    @DisplayName("should return filtered rows when scenario column exists")
    void shouldReturnFilteredRows_whenScenarioColumnExists() {
      // Given
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.rows();

      // Then
      assertNotNull(result, "result should not be null");
      assertEquals(1, result.size(), "should have 1 filtered row");
    }

    /** Verifies that rows returns all rows when filter is not active. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return all rows when filter is not active")
    void shouldReturnAllRows_whenFilterIsNotActive() {
      // Given
      final var inactiveFilter = new ScenarioFilter(new ScenarioMarker("$scenario"), Set.of());
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, inactiveFilter);

      // When
      final var result = filteredTable.rows();

      // Then
      assertNotNull(result, "result should not be null");
    }
  }

  /** Tests for the rowCount() method. */
  @Nested
  @DisplayName("rowCount() method")
  class RowCountMethod {

    /** Tests for the rowCount method. */
    RowCountMethod() {}

    /** Verifies that rowCount returns filtered row count. */
    @Test
    @Tag("normal")
    @DisplayName("should return filtered row count when called")
    void shouldReturnFilteredRowCount_whenCalled() {
      // Given
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.rowCount();

      // Then
      assertEquals(1, result, "should return correct row count");
    }
  }

  /** Tests for cell value preservation. */
  @Nested
  @DisplayName("cell value preservation")
  class CellValuePreservation {

    /** Tests for cell value preservation. */
    CellValuePreservation() {}

    /**
     * Verifies that an empty-string cell is preserved rather than converted to NULL.
     *
     * <p>FilteredTable projects each cell value as parsed. It does not collapse an empty string to
     * NULL. An empty CSV or TSV cell already parses to NULL upstream, while a JSON or YAML empty
     * string parses to a non-null empty string. Preserving the value keeps the NULL versus
     * empty-string distinction expressed in JSON and YAML.
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should preserve empty string as non-null when empty values exist")
    void shouldPreserveEmptyString_whenEmptyValuesExist() {
      // Given
      final var sourceTable = createMockTableWithEmptyString();
      final var noScenarioFilter = new ScenarioFilter(new ScenarioMarker("$nonexistent"), Set.of());
      final var filteredTable = new FilteredTable(sourceTable, noScenarioFilter);

      // When
      final var result = filteredTable.rows();

      // Then
      final var nameValue = result.get(0).value(new ColumnName("NAME"));
      assertAll(
          "empty string is preserved",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have 1 row"),
          () -> assertFalse(nameValue.isNull(), "empty-string NAME cell should not be NULL"),
          () ->
              assertEquals(
                  "", nameValue.value(), "empty-string NAME cell should remain an empty string"));
    }
  }

  /**
   * Creates a mock Table with scenario column for testing.
   *
   * @return mock Table
   */
  private Table createMockTableWithScenario() {
    final var mockTable = mock(Table.class);
    final var tableName = new TableName("users");
    final var columns =
        List.of(new ColumnName("$scenario"), new ColumnName("ID"), new ColumnName("NAME"));
    final var row = createMockRowWithScenario("test");

    when(mockTable.name()).thenReturn(tableName);
    when(mockTable.columns()).thenReturn(columns);
    when(mockTable.rows()).thenReturn(List.of(row));
    when(mockTable.rowCount()).thenReturn(1);

    return mockTable;
  }

  /**
   * Creates a mock Table without scenario column for testing.
   *
   * @return mock Table
   */
  private Table createMockTableWithoutScenario() {
    final var mockTable = mock(Table.class);
    final var tableName = new TableName("users");
    final var columns = List.of(new ColumnName("ID"), new ColumnName("NAME"));
    final var row = createMockRowWithoutScenario();

    when(mockTable.name()).thenReturn(tableName);
    when(mockTable.columns()).thenReturn(columns);
    when(mockTable.rows()).thenReturn(List.of(row));
    when(mockTable.rowCount()).thenReturn(1);

    return mockTable;
  }

  /**
   * Creates a mock Table with empty string value.
   *
   * @return mock Table
   */
  private Table createMockTableWithEmptyString() {
    final var mockTable = mock(Table.class);
    final var tableName = new TableName("users");
    final var columns = List.of(new ColumnName("ID"), new ColumnName("NAME"));
    final var row = createMockRowWithEmptyString();

    when(mockTable.name()).thenReturn(tableName);
    when(mockTable.columns()).thenReturn(columns);
    when(mockTable.rows()).thenReturn(List.of(row));
    when(mockTable.rowCount()).thenReturn(1);

    return mockTable;
  }

  /**
   * Creates a mock Row with scenario value for testing.
   *
   * @param scenarioValue the scenario value
   * @return mock Row
   */
  private Row createMockRowWithScenario(final String scenarioValue) {
    final var mockRow = mock(Row.class);
    final var values = new LinkedHashMap<ColumnName, CellValue>();
    values.put(new ColumnName("$scenario"), new CellValue(scenarioValue));
    values.put(new ColumnName("ID"), new CellValue("1"));
    values.put(new ColumnName("NAME"), new CellValue("John"));

    when(mockRow.value(new ColumnName("$scenario"))).thenReturn(new CellValue(scenarioValue));
    when(mockRow.value(new ColumnName("ID"))).thenReturn(new CellValue("1"));
    when(mockRow.value(new ColumnName("NAME"))).thenReturn(new CellValue("John"));
    when(mockRow.values()).thenReturn(values);

    return mockRow;
  }

  /**
   * Creates a mock Row without scenario column for testing.
   *
   * @return mock Row
   */
  private Row createMockRowWithoutScenario() {
    final var mockRow = mock(Row.class);
    final var values = new LinkedHashMap<ColumnName, CellValue>();
    values.put(new ColumnName("ID"), new CellValue("1"));
    values.put(new ColumnName("NAME"), new CellValue("John"));

    when(mockRow.value(new ColumnName("ID"))).thenReturn(new CellValue("1"));
    when(mockRow.value(new ColumnName("NAME"))).thenReturn(new CellValue("John"));
    when(mockRow.values()).thenReturn(values);

    return mockRow;
  }

  /**
   * Creates a mock Row with empty string value for testing.
   *
   * @return mock Row
   */
  private Row createMockRowWithEmptyString() {
    final var mockRow = mock(Row.class);
    final var values = new LinkedHashMap<ColumnName, CellValue>();
    values.put(new ColumnName("ID"), new CellValue("1"));
    values.put(new ColumnName("NAME"), new CellValue(""));

    when(mockRow.value(new ColumnName("ID"))).thenReturn(new CellValue("1"));
    when(mockRow.value(new ColumnName("NAME"))).thenReturn(new CellValue(""));
    when(mockRow.values()).thenReturn(values);

    return mockRow;
  }
}
