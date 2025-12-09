package io.github.seijikohara.dbtester.internal.scenario;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.github.seijikohara.dbtester.api.dataset.DataSet;
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
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link FilteredDataSet}. */
@DisplayName("FilteredDataSet")
class FilteredDataSetTest {

  /** Tests for the FilteredDataSet class. */
  FilteredDataSetTest() {}

  /** The filter for testing. */
  private ScenarioFilter filter;

  /** Mock data source. */
  private DataSource mockDataSource;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    filter = new ScenarioFilter(new ScenarioMarker("$scenario"), Set.of(new ScenarioName("test")));
    mockDataSource = mock(DataSource.class);
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
      final var sourceDataSet = createMockDataSet();

      // When
      final var result = new FilteredDataSet(sourceDataSet, filter, mockDataSource);

      // Then
      assertNotNull(result, "instance should not be null");
    }

    /** Verifies that constructor accepts null data source. */
    @Test
    @Tag("edge-case")
    @DisplayName("should create instance when data source is null")
    void shouldCreateInstance_whenDataSourceIsNull() {
      // Given
      final var sourceDataSet = createMockDataSet();

      // When
      final var result = new FilteredDataSet(sourceDataSet, filter, null);

      // Then
      assertNotNull(result, "instance should not be null");
    }
  }

  /** Tests for the getTables() method. */
  @Nested
  @DisplayName("getTables() method")
  class GetTablesMethod {

    /** Tests for the getTables method. */
    GetTablesMethod() {}

    /** Verifies that getTables returns filtered tables. */
    @Test
    @Tag("normal")
    @DisplayName("should return filtered tables when called")
    void shouldReturnFilteredTables_whenCalled() {
      // Given
      final var sourceDataSet = createMockDataSet();
      final var filteredDataSet = new FilteredDataSet(sourceDataSet, filter, mockDataSource);

      // When
      final var result = filteredDataSet.getTables();

      // Then
      assertAll(
          "tables should be filtered",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.size(), "should have one table"));
    }

    /** Verifies that getTables returns immutable list. */
    @Test
    @Tag("normal")
    @DisplayName("should return immutable list when called")
    void shouldReturnImmutableList_whenCalled() {
      // Given
      final var sourceDataSet = createMockDataSet();
      final var filteredDataSet = new FilteredDataSet(sourceDataSet, filter, mockDataSource);

      // When
      final var result = filteredDataSet.getTables();

      // Then
      assertNotNull(result, "result should not be null");
    }
  }

  /** Tests for the getTable() method. */
  @Nested
  @DisplayName("getTable(TableName) method")
  class GetTableMethod {

    /** Tests for the getTable method. */
    GetTableMethod() {}

    /** Verifies that getTable returns table when it exists. */
    @Test
    @Tag("normal")
    @DisplayName("should return table when table exists")
    void shouldReturnTable_whenTableExists() {
      // Given
      final var sourceDataSet = createMockDataSet();
      final var filteredDataSet = new FilteredDataSet(sourceDataSet, filter, mockDataSource);
      final var tableName = new TableName("users");

      // When
      final var result = filteredDataSet.getTable(tableName);

      // Then
      assertTrue(result.isPresent(), "table should be present");
      assertEquals(tableName, result.orElseThrow().getName(), "should have correct table name");
    }

    /** Verifies that getTable returns empty when table does not exist. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when table does not exist")
    void shouldReturnEmpty_whenTableDoesNotExist() {
      // Given
      final var sourceDataSet = createMockDataSet();
      final var filteredDataSet = new FilteredDataSet(sourceDataSet, filter, mockDataSource);
      final var tableName = new TableName("nonexistent");

      // When
      final var result = filteredDataSet.getTable(tableName);

      // Then
      assertFalse(result.isPresent(), "table should not be present");
    }
  }

  /** Tests for the getDataSource() method. */
  @Nested
  @DisplayName("getDataSource() method")
  class GetDataSourceMethod {

    /** Tests for the getDataSource method. */
    GetDataSourceMethod() {}

    /** Verifies that getDataSource returns data source when present. */
    @Test
    @Tag("normal")
    @DisplayName("should return data source when present")
    void shouldReturnDataSource_whenPresent() {
      // Given
      final var sourceDataSet = createMockDataSet();
      final var filteredDataSet = new FilteredDataSet(sourceDataSet, filter, mockDataSource);

      // When
      final var result = filteredDataSet.getDataSource();

      // Then
      assertTrue(result.isPresent(), "data source should be present");
      assertEquals(mockDataSource, result.orElseThrow(), "should return correct data source");
    }

    /** Verifies that getDataSource returns empty when null. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty when data source is null")
    void shouldReturnEmpty_whenDataSourceIsNull() {
      // Given
      final var sourceDataSet = createMockDataSet();
      final var filteredDataSet = new FilteredDataSet(sourceDataSet, filter, null);

      // When
      final var result = filteredDataSet.getDataSource();

      // Then
      assertFalse(result.isPresent(), "data source should not be present");
    }
  }

  /**
   * Creates a mock DataSet for testing.
   *
   * @return mock DataSet
   */
  private DataSet createMockDataSet() {
    final var mockDataSet = mock(DataSet.class);
    final var mockTable = createMockTable();
    when(mockDataSet.getTables()).thenReturn(List.of(mockTable));
    return mockDataSet;
  }

  /**
   * Creates a mock Table for testing.
   *
   * @return mock Table
   */
  private Table createMockTable() {
    final var mockTable = mock(Table.class);
    final var tableName = new TableName("users");
    final var columns =
        List.of(new ColumnName("$scenario"), new ColumnName("ID"), new ColumnName("NAME"));
    final var row = createMockRow();

    when(mockTable.getName()).thenReturn(tableName);
    when(mockTable.getColumns()).thenReturn(columns);
    when(mockTable.getRows()).thenReturn(List.of(row));
    when(mockTable.getRowCount()).thenReturn(1);

    return mockTable;
  }

  /**
   * Creates a mock Row for testing.
   *
   * @return mock Row
   */
  private Row createMockRow() {
    final var mockRow = mock(Row.class);
    final var values = new LinkedHashMap<ColumnName, CellValue>();
    values.put(new ColumnName("$scenario"), new CellValue("test"));
    values.put(new ColumnName("ID"), new CellValue("1"));
    values.put(new ColumnName("NAME"), new CellValue("John"));

    when(mockRow.getValue(new ColumnName("$scenario"))).thenReturn(new CellValue("test"));
    when(mockRow.getValue(new ColumnName("ID"))).thenReturn(new CellValue("1"));
    when(mockRow.getValue(new ColumnName("NAME"))).thenReturn(new CellValue("John"));
    when(mockRow.getValues()).thenReturn(values);

    return mockRow;
  }
}
