package io.github.seijikohara.dbtester.internal.assertion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTableSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DataSetComparator}. */
@DisplayName("DataSetComparator")
class DataSetComparatorTest {

  /** Tests for the DataSetComparator class. */
  DataSetComparatorTest() {}

  /** The comparator instance under test. */
  private DataSetComparator comparator;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    comparator = new DataSetComparator();
  }

  /** Tests for the assertEquals(TableSet, TableSet, AssertionFailureHandler) method. */
  @Nested
  @DisplayName("assertEquals(TableSet, TableSet, AssertionFailureHandler) method")
  class AssertEqualsDataSetMethod {

    /** Tests for the assertEquals method for TableSet. */
    AssertEqualsDataSetMethod() {}

    /** Verifies that assertEquals passes when datasets are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when datasets are equal")
    void shouldPass_whenDataSetsAreEqual() {
      // Given
      final var expected = createTableSet("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));
      final var actual = createTableSet("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEquals(expected, actual, null),
          "should not throw when datasets are equal");
    }

    /** Verifies that assertEquals throws when table count differs. */
    @Test
    @Tag("error")
    @DisplayName("should throw when table count differs")
    void shouldThrow_whenTableCountDiffers() {
      // Given
      final var table1 = createTable("USERS", List.of("ID"), List.of("1"));
      final var table2 = createTable("PRODUCTS", List.of("ID"), List.of("1"));
      final var expected = new SimpleTableSet(List.of(table1, table2));
      final var actual = new SimpleTableSet(List.of(table1));

      // When & Then
      final var exception =
          assertThrows(AssertionError.class, () -> comparator.assertEquals(expected, actual, null));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("table_count"),
          "exception message should mention table count mismatch");
    }

    /** Verifies that assertEquals throws when table is missing. */
    @Test
    @Tag("error")
    @DisplayName("should throw when table is missing")
    void shouldThrow_whenTableIsMissing() {
      // Given
      final var expected = createTableSet("USERS", List.of("ID"), List.of("1"));
      final var actual = createTableSet("PRODUCTS", List.of("ID"), List.of("1"));

      // When & Then
      final var exception =
          assertThrows(AssertionError.class, () -> comparator.assertEquals(expected, actual, null));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("not found"),
          "exception message should mention table not found");
    }

    /** Verifies that assertEquals calls failure handler when provided. */
    @Test
    @Tag("normal")
    @DisplayName("should call failure handler when provided and tables differ")
    void shouldCallFailureHandler_whenProvidedAndTablesDiffer() {
      // Given
      final var expected = createTableSet("USERS", List.of("ID"), List.of("1"));
      final var actual = createTableSet("USERS", List.of("ID"), List.of("2"));
      final var mockHandler = mock(AssertionFailureHandler.class);

      // When
      comparator.assertEquals(expected, actual, mockHandler);

      // Then - now passes structured message with null expected/actual (values are in message)
      verify(mockHandler)
          .handleFailure(
              org.mockito.ArgumentMatchers.contains("row[0].ID"),
              org.mockito.ArgumentMatchers.isNull(),
              org.mockito.ArgumentMatchers.isNull());
    }
  }

  /** Tests for the assertEquals(Table, Table, AssertionFailureHandler) method. */
  @Nested
  @DisplayName("assertEquals(Table, Table, AssertionFailureHandler) method")
  class AssertEqualsTableMethod {

    /** Tests for the assertEquals method for Table. */
    AssertEqualsTableMethod() {}

    /** Verifies that assertEquals passes when tables are equal. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when tables are equal")
    void shouldPass_whenTablesAreEqual() {
      // Given
      final var expected = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));
      final var actual = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEquals(expected, actual, null),
          "should not throw when tables are equal");
    }

    /** Verifies that assertEquals throws when row count differs. */
    @Test
    @Tag("error")
    @DisplayName("should throw when row count differs")
    void shouldThrow_whenRowCountDiffers() {
      // Given
      final var expected = createTableWithMultipleRows("USERS", List.of("ID"), List.of("1", "2"));
      final var actual = createTableWithMultipleRows("USERS", List.of("ID"), List.of("1"));

      // When & Then
      final var exception =
          assertThrows(AssertionError.class, () -> comparator.assertEquals(expected, actual, null));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("row_count"),
          "exception message should mention row count mismatch");
    }

    /** Verifies that assertEquals throws when values differ. */
    @Test
    @Tag("error")
    @DisplayName("should throw when values differ")
    void shouldThrow_whenValuesDiffer() {
      // Given
      final var expected = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));
      final var actual = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Bob"));

      // When & Then
      final var exception =
          assertThrows(AssertionError.class, () -> comparator.assertEquals(expected, actual, null));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      final var message = exception.getMessage();
      assertAll(
          "exception message should describe the value mismatch",
          () -> assertTrue(message.contains("row[0].NAME"), "should mention row and column"),
          () -> assertTrue(message.contains("Alice"), "should mention expected value"),
          () -> assertTrue(message.contains("Bob"), "should mention actual value"));
    }

    /** Verifies that assertEquals collects all differences. */
    @Test
    @Tag("normal")
    @DisplayName("should collect all differences instead of failing on first")
    void shouldCollectAllDifferences() {
      // Given - multiple differences in the same table
      final var expectedColumns = List.of("ID", "NAME", "STATUS");
      final var actualColumns = List.of("ID", "NAME", "STATUS");
      final var expected = createTable("USERS", expectedColumns, List.of("1", "Alice", "ACTIVE"));
      final var actual = createTable("USERS", actualColumns, List.of("2", "Bob", "INACTIVE"));

      // When & Then
      final var exception =
          assertThrows(AssertionError.class, () -> comparator.assertEquals(expected, actual, null));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      final String message = exception.getMessage();
      assertTrue(message.contains("total_differences: 3"), "should count all differences");
      assertTrue(message.contains("row[0].ID"), "should include ID mismatch");
      assertTrue(message.contains("row[0].NAME"), "should include NAME mismatch");
      assertTrue(message.contains("row[0].STATUS"), "should include STATUS mismatch");
    }
  }

  /** Tests for the assertEqualsIgnoreColumns(TableSet, TableSet, String, Collection) method. */
  @Nested
  @DisplayName("assertEqualsIgnoreColumns(TableSet, TableSet, String, Collection) method")
  class AssertEqualsIgnoreColumnsDataSetMethod {

    /** Tests for the assertEqualsIgnoreColumns method for TableSet. */
    AssertEqualsIgnoreColumnsDataSetMethod() {}

    /** Verifies that assertEqualsIgnoreColumns passes when ignoring differing columns. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when ignoring differing columns")
    void shouldPass_whenIgnoringDifferingColumns() {
      // Given
      final var expected =
          createTableSet(
              "USERS", List.of("ID", "NAME", "TIMESTAMP"), List.of("1", "Alice", "2024"));
      final var actual =
          createTableSet(
              "USERS", List.of("ID", "NAME", "TIMESTAMP"), List.of("1", "Alice", "2025"));

      // When & Then
      assertDoesNotThrow(
          () ->
              comparator.assertEqualsIgnoreColumns(expected, actual, "USERS", Set.of("TIMESTAMP")),
          "should not throw when ignored column differs");
    }

    /** Verifies that assertEqualsIgnoreColumns throws when expected table not found. */
    @Test
    @Tag("error")
    @DisplayName("should throw when expected table not found")
    void shouldThrow_whenExpectedTableNotFound() {
      // Given
      final var expected = createTableSet("PRODUCTS", List.of("ID"), List.of("1"));
      final var actual = createTableSet("USERS", List.of("ID"), List.of("1"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsIgnoreColumns(expected, actual, "USERS", Set.of()));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("Expected table not found"),
          "exception message should mention expected table not found");
    }

    /** Verifies that assertEqualsIgnoreColumns throws when actual table not found. */
    @Test
    @Tag("error")
    @DisplayName("should throw when actual table not found")
    void shouldThrow_whenActualTableNotFound() {
      // Given
      final var expected = createTableSet("USERS", List.of("ID"), List.of("1"));
      final var actual = createTableSet("PRODUCTS", List.of("ID"), List.of("1"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsIgnoreColumns(expected, actual, "USERS", Set.of()));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("Actual table not found"),
          "exception message should mention actual table not found");
    }
  }

  /** Tests for the assertEqualsIgnoreColumns(Table, Table, Collection) method. */
  @Nested
  @DisplayName("assertEqualsIgnoreColumns(Table, Table, Collection) method")
  class AssertEqualsIgnoreColumnsTableMethod {

    /** Tests for the assertEqualsIgnoreColumns method for Table. */
    AssertEqualsIgnoreColumnsTableMethod() {}

    /** Verifies that assertEqualsIgnoreColumns passes when ignoring columns. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when ignoring differing columns")
    void shouldPass_whenIgnoringDifferingColumns() {
      // Given
      final var expected =
          createTable("USERS", List.of("ID", "NAME", "UPDATED_AT"), List.of("1", "Alice", "old"));
      final var actual =
          createTable("USERS", List.of("ID", "NAME", "UPDATED_AT"), List.of("1", "Alice", "new"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEqualsIgnoreColumns(expected, actual, Set.of("UPDATED_AT")),
          "should not throw when ignored column differs");
    }

    /** Verifies that assertEqualsIgnoreColumns throws when non-ignored values differ. */
    @Test
    @Tag("error")
    @DisplayName("should throw when non-ignored values differ")
    void shouldThrow_whenNonIgnoredValuesDiffer() {
      // Given
      final var expected =
          createTable("USERS", List.of("ID", "NAME", "UPDATED_AT"), List.of("1", "Alice", "old"));
      final var actual =
          createTable("USERS", List.of("ID", "NAME", "UPDATED_AT"), List.of("1", "Bob", "new"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsIgnoreColumns(expected, actual, Set.of("UPDATED_AT")));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("NAME"),
          "exception message should mention the differing column");
    }
  }

  /**
   * Creates a TableSet with one table.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param values the row values
   * @return the created TableSet
   */
  private static TableSet createTableSet(
      final String tableName, final List<String> columnNames, final List<String> values) {
    return new SimpleTableSet(List.of(createTable(tableName, columnNames, values)));
  }

  /**
   * Creates a Table.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param values the row values
   * @return the created Table
   */
  private static Table createTable(
      final String tableName, final List<String> columnNames, final List<String> values) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final var rowValues =
        columns.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    column -> column,
                    column -> new CellValue(values.get(columns.indexOf(column))),
                    (v1, v2) -> v1,
                    java.util.LinkedHashMap::new));
    final var row = new SimpleRow(rowValues);
    return new SimpleTable(new TableName(tableName), columns, List.of(row));
  }

  /**
   * Creates a Table with multiple rows.
   *
   * @param tableName the table name
   * @param columnNames the column names
   * @param rowValues the values for each row
   * @return the created Table
   */
  private static Table createTableWithMultipleRows(
      final String tableName, final List<String> columnNames, final List<String> rowValues) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final var rows =
        rowValues.stream()
            .map(value -> new SimpleRow(Map.of(columns.getFirst(), new CellValue(value))))
            .map(Row.class::cast)
            .toList();
    return new SimpleTable(new TableName(tableName), columns, rows);
  }
}
