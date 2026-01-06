package io.github.seijikohara.dbtester.internal.loader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTableSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DataSetMerger}. */
@DisplayName("DataSetMerger")
class DataSetMergerTest {

  /** Tests for the DataSetMerger class. */
  DataSetMergerTest() {}

  /** The merger under test. */
  private DataSetMerger merger;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    merger = new DataSetMerger();
  }

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var instance = new DataSetMerger();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the merge() method with empty input. */
  @Nested
  @DisplayName("merge() with empty input")
  class MergeEmptyInput {

    /** Tests for the merge method with empty input. */
    MergeEmptyInput() {}

    /** Verifies that merge returns empty dataset when input is empty. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return empty dataset when input is empty")
    void shouldReturnEmptyDataSet_whenInputIsEmpty() {
      // When
      final var result = merger.merge(List.of(), TableMergeStrategy.UNION_ALL);

      // Then
      assertAll(
          "result should be empty",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertTrue(result.getTables().isEmpty(), "should have no tables"));
    }
  }

  /** Tests for the merge() method with single dataset. */
  @Nested
  @DisplayName("merge() with single dataset")
  class MergeSingleDataSet {

    /** Tests for the merge method with single dataset. */
    MergeSingleDataSet() {}

    /** Verifies that merge returns same dataset when single input. */
    @Test
    @Tag("normal")
    @DisplayName("should return same dataset when single input")
    void shouldReturnSameDataSet_whenSingleInput() {
      // Given
      final var dataSet = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));

      // When
      final var result = merger.merge(List.of(dataSet), TableMergeStrategy.UNION_ALL);

      // Then
      assertAll(
          "result should match input",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.getTables().size(), "should have one table"),
          () ->
              assertEquals(
                  "TABLE1", result.getTables().get(0).getName().value(), "table name should match"),
          () -> assertEquals(1, result.getTables().get(0).getRows().size(), "should have one row"));
    }
  }

  /** Tests for FIRST merge strategy. */
  @Nested
  @DisplayName("merge() with FIRST strategy")
  class MergeFirstStrategy {

    /** Tests for FIRST strategy. */
    MergeFirstStrategy() {}

    /** Verifies that merge uses first table when same table name. */
    @Test
    @Tag("normal")
    @DisplayName("should use first table when same table name")
    void shouldUseFirstTable_whenSameTableName() {
      // Given
      final var dataSet1 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));
      final var dataSet2 = createTableSet("TABLE1", List.of("id", "name"), List.of("2", "Bob"));

      // When
      final var result = merger.merge(List.of(dataSet1, dataSet2), TableMergeStrategy.FIRST);

      // Then
      final var table = result.getTables().get(0);
      final var rows = table.getRows();
      assertAll(
          "should use first table",
          () -> assertEquals(1, result.getTables().size(), "should have one table"),
          () -> assertEquals(1, rows.size(), "should have one row from first table"),
          () ->
              assertEquals(
                  "1",
                  rows.get(0).getValue(new ColumnName("id")).value(),
                  "id should be from first table"));
    }
  }

  /** Tests for LAST merge strategy. */
  @Nested
  @DisplayName("merge() with LAST strategy")
  class MergeLastStrategy {

    /** Tests for LAST strategy. */
    MergeLastStrategy() {}

    /** Verifies that merge uses last table when same table name. */
    @Test
    @Tag("normal")
    @DisplayName("should use last table when same table name")
    void shouldUseLastTable_whenSameTableName() {
      // Given
      final var dataSet1 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));
      final var dataSet2 = createTableSet("TABLE1", List.of("id", "name"), List.of("2", "Bob"));

      // When
      final var result = merger.merge(List.of(dataSet1, dataSet2), TableMergeStrategy.LAST);

      // Then
      final var table = result.getTables().get(0);
      final var rows = table.getRows();
      assertAll(
          "should use last table",
          () -> assertEquals(1, result.getTables().size(), "should have one table"),
          () -> assertEquals(1, rows.size(), "should have one row from last table"),
          () ->
              assertEquals(
                  "2",
                  rows.get(0).getValue(new ColumnName("id")).value(),
                  "id should be from last table"));
    }
  }

  /** Tests for UNION merge strategy. */
  @Nested
  @DisplayName("merge() with UNION strategy")
  class MergeUnionStrategy {

    /** Tests for UNION strategy. */
    MergeUnionStrategy() {}

    /** Verifies that merge removes duplicate rows. */
    @Test
    @Tag("normal")
    @DisplayName("should remove duplicate rows when same data")
    void shouldRemoveDuplicateRows_whenSameData() {
      // Given
      final var dataSet1 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));
      final var dataSet2 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));

      // When
      final var result = merger.merge(List.of(dataSet1, dataSet2), TableMergeStrategy.UNION);

      // Then
      final var table = result.getTables().get(0);
      assertAll(
          "should have unique rows",
          () -> assertEquals(1, result.getTables().size(), "should have one table"),
          () -> assertEquals(1, table.getRows().size(), "should have one row after deduplication"));
    }

    /** Verifies that merge keeps distinct rows. */
    @Test
    @Tag("normal")
    @DisplayName("should keep distinct rows when different data")
    void shouldKeepDistinctRows_whenDifferentData() {
      // Given
      final var dataSet1 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));
      final var dataSet2 = createTableSet("TABLE1", List.of("id", "name"), List.of("2", "Bob"));

      // When
      final var result = merger.merge(List.of(dataSet1, dataSet2), TableMergeStrategy.UNION);

      // Then
      final var table = result.getTables().get(0);
      assertAll(
          "should have all distinct rows",
          () -> assertEquals(1, result.getTables().size(), "should have one table"),
          () -> assertEquals(2, table.getRows().size(), "should have two distinct rows"));
    }
  }

  /** Tests for UNION_ALL merge strategy. */
  @Nested
  @DisplayName("merge() with UNION_ALL strategy")
  class MergeUnionAllStrategy {

    /** Tests for UNION_ALL strategy. */
    MergeUnionAllStrategy() {}

    /** Verifies that merge keeps all rows including duplicates. */
    @Test
    @Tag("normal")
    @DisplayName("should keep all rows including duplicates")
    void shouldKeepAllRows_includingDuplicates() {
      // Given
      final var dataSet1 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));
      final var dataSet2 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));

      // When
      final var result = merger.merge(List.of(dataSet1, dataSet2), TableMergeStrategy.UNION_ALL);

      // Then
      final var table = result.getTables().get(0);
      assertAll(
          "should have all rows",
          () -> assertEquals(1, result.getTables().size(), "should have one table"),
          () ->
              assertEquals(
                  2, table.getRows().size(), "should have two rows (duplicates preserved)"));
    }

    /** Verifies that merge combines rows from different datasets. */
    @Test
    @Tag("normal")
    @DisplayName("should combine rows from different datasets")
    void shouldCombineRows_fromDifferentDataSets() {
      // Given
      final var dataSet1 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));
      final var dataSet2 = createTableSet("TABLE1", List.of("id", "name"), List.of("2", "Bob"));

      // When
      final var result = merger.merge(List.of(dataSet1, dataSet2), TableMergeStrategy.UNION_ALL);

      // Then
      final var table = result.getTables().get(0);
      assertAll(
          "should combine all rows",
          () -> assertEquals(1, result.getTables().size(), "should have one table"),
          () -> assertEquals(2, table.getRows().size(), "should have two rows combined"));
    }
  }

  /** Tests for merging datasets with different tables. */
  @Nested
  @DisplayName("merge() with different tables")
  class MergeDifferentTables {

    /** Tests for merging different tables. */
    MergeDifferentTables() {}

    /** Verifies that merge preserves different tables. */
    @Test
    @Tag("normal")
    @DisplayName("should preserve different tables")
    void shouldPreserveDifferentTables() {
      // Given
      final var dataSet1 = createTableSet("TABLE1", List.of("id", "name"), List.of("1", "Alice"));
      final var dataSet2 = createTableSet("TABLE2", List.of("id", "value"), List.of("1", "100"));

      // When
      final var result = merger.merge(List.of(dataSet1, dataSet2), TableMergeStrategy.UNION_ALL);

      // Then
      assertAll(
          "should have both tables",
          () -> assertEquals(2, result.getTables().size(), "should have two tables"),
          () ->
              assertEquals(
                  "TABLE1",
                  result.getTables().get(0).getName().value(),
                  "first table should be TABLE1"),
          () ->
              assertEquals(
                  "TABLE2",
                  result.getTables().get(1).getName().value(),
                  "second table should be TABLE2"));
    }
  }

  /**
   * Creates a TableSet for testing.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param values the row values
   * @return the created TableSet
   */
  private TableSet createTableSet(
      final String tableName, final List<String> columnNames, final List<String> values) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final Map<ColumnName, CellValue> rowValues = new LinkedHashMap<>();
    for (var i = 0; i < columns.size(); i++) {
      rowValues.put(columns.get(i), new CellValue(values.get(i)));
    }
    final Row row = new SimpleRow(rowValues);
    final Table table = new SimpleTable(new TableName(tableName), columns, List.of(row));
    return new SimpleTableSet(List.of(table));
  }
}
