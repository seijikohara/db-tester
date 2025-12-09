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
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.getName();

      // Then
      assertEquals(new TableName("users"), result, "should return correct table name");
    }
  }

  /** Tests for the getColumns() method. */
  @Nested
  @DisplayName("getColumns() method")
  class GetColumnsMethod {

    /** Tests for the getColumns method. */
    GetColumnsMethod() {}

    /** Verifies that getColumns excludes scenario column. */
    @Test
    @Tag("normal")
    @DisplayName("should exclude scenario column when scenario column exists")
    void shouldExcludeScenarioColumn_whenScenarioColumnExists() {
      // Given
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.getColumns();

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

    /** Verifies that getColumns returns all columns when no scenario column. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return all columns when no scenario column exists")
    void shouldReturnAllColumns_whenNoScenarioColumnExists() {
      // Given
      final var sourceTable = createMockTableWithoutScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.getColumns();

      // Then
      assertEquals(2, result.size(), "should have all 2 columns");
    }
  }

  /** Tests for the getRows() method. */
  @Nested
  @DisplayName("getRows() method")
  class GetRowsMethod {

    /** Tests for the getRows method. */
    GetRowsMethod() {}

    /** Verifies that getRows returns filtered rows. */
    @Test
    @Tag("normal")
    @DisplayName("should return filtered rows when scenario column exists")
    void shouldReturnFilteredRows_whenScenarioColumnExists() {
      // Given
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.getRows();

      // Then
      assertNotNull(result, "result should not be null");
      assertEquals(1, result.size(), "should have 1 filtered row");
    }

    /** Verifies that getRows returns all rows when filter is not active. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return all rows when filter is not active")
    void shouldReturnAllRows_whenFilterIsNotActive() {
      // Given
      final var inactiveFilter = new ScenarioFilter(new ScenarioMarker("$scenario"), Set.of());
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, inactiveFilter);

      // When
      final var result = filteredTable.getRows();

      // Then
      assertNotNull(result, "result should not be null");
    }
  }

  /** Tests for the getRowCount() method. */
  @Nested
  @DisplayName("getRowCount() method")
  class GetRowCountMethod {

    /** Tests for the getRowCount method. */
    GetRowCountMethod() {}

    /** Verifies that getRowCount returns filtered row count. */
    @Test
    @Tag("normal")
    @DisplayName("should return filtered row count when called")
    void shouldReturnFilteredRowCount_whenCalled() {
      // Given
      final var sourceTable = createMockTableWithScenario();
      final var filteredTable = new FilteredTable(sourceTable, filter);

      // When
      final var result = filteredTable.getRowCount();

      // Then
      assertEquals(1, result, "should return correct row count");
    }
  }

  /** Tests for empty string normalization. */
  @Nested
  @DisplayName("empty string normalization")
  class EmptyStringNormalization {

    /** Tests for empty string normalization. */
    EmptyStringNormalization() {}

    /** Verifies that empty strings are converted to null. */
    @Test
    @Tag("edge-case")
    @DisplayName("should convert empty strings to null when empty values exist")
    void shouldConvertEmptyStringsToNull_whenEmptyValuesExist() {
      // Given
      final var sourceTable = createMockTableWithEmptyString();
      final var noScenarioFilter = new ScenarioFilter(new ScenarioMarker("$nonexistent"), Set.of());
      final var filteredTable = new FilteredTable(sourceTable, noScenarioFilter);

      // When
      final var result = filteredTable.getRows();

      // Then
      assertNotNull(result, "result should not be null");
      assertEquals(1, result.size(), "should have 1 row");
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

    when(mockTable.getName()).thenReturn(tableName);
    when(mockTable.getColumns()).thenReturn(columns);
    when(mockTable.getRows()).thenReturn(List.of(row));
    when(mockTable.getRowCount()).thenReturn(1);

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

    when(mockTable.getName()).thenReturn(tableName);
    when(mockTable.getColumns()).thenReturn(columns);
    when(mockTable.getRows()).thenReturn(List.of(row));
    when(mockTable.getRowCount()).thenReturn(1);

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

    when(mockTable.getName()).thenReturn(tableName);
    when(mockTable.getColumns()).thenReturn(columns);
    when(mockTable.getRows()).thenReturn(List.of(row));
    when(mockTable.getRowCount()).thenReturn(1);

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

    when(mockRow.getValue(new ColumnName("$scenario"))).thenReturn(new CellValue(scenarioValue));
    when(mockRow.getValue(new ColumnName("ID"))).thenReturn(new CellValue("1"));
    when(mockRow.getValue(new ColumnName("NAME"))).thenReturn(new CellValue("John"));
    when(mockRow.getValues()).thenReturn(values);

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

    when(mockRow.getValue(new ColumnName("ID"))).thenReturn(new CellValue("1"));
    when(mockRow.getValue(new ColumnName("NAME"))).thenReturn(new CellValue("John"));
    when(mockRow.getValues()).thenReturn(values);

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

    when(mockRow.getValue(new ColumnName("ID"))).thenReturn(new CellValue("1"));
    when(mockRow.getValue(new ColumnName("NAME"))).thenReturn(new CellValue(""));
    when(mockRow.getValues()).thenReturn(values);

    return mockRow;
  }
}
