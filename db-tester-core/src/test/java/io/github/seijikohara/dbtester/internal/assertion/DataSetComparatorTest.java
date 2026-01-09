package io.github.seijikohara.dbtester.internal.assertion;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.dataset.Row;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.internal.dataset.SimpleRow;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTable;
import io.github.seijikohara.dbtester.internal.dataset.SimpleTableSet;
import java.util.HashMap;
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

  /**
   * Creates a Table with null value in the second column.
   *
   * @param tableName the table name
   * @param columnNames the column names (must have at least 2 columns)
   * @param firstColumnValue the value for the first column
   * @return the created Table with null in second column
   */
  private static Table createTableWithNullValue(
      final String tableName, final List<String> columnNames, final String firstColumnValue) {
    final var columns = columnNames.stream().map(ColumnName::new).toList();
    final var rowValues = new java.util.LinkedHashMap<ColumnName, CellValue>();
    rowValues.put(columns.get(0), new CellValue(firstColumnValue));
    rowValues.put(columns.get(1), CellValue.NULL);
    final var row = new SimpleRow(rowValues);
    return new SimpleTable(new TableName(tableName), columns, List.of(row));
  }

  /** Tests for the assertEqualsWithStrategies(Table, Table, Collection, Map) method. */
  @Nested
  @DisplayName("assertEqualsWithStrategies(Table, Table, Collection, Map) method")
  class AssertEqualsWithStrategiesMethod {

    /** Tests for the assertEqualsWithStrategies method. */
    AssertEqualsWithStrategiesMethod() {}

    /** Verifies that assertEqualsWithStrategies passes with empty strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when strategies are empty and values match")
    void shouldPass_whenStrategiesAreEmptyAndValuesMatch() {
      // Given
      final var expected = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));
      final var actual = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), new HashMap<>()),
          "should not throw when values match with empty strategies");
    }

    /** Verifies that assertEqualsWithStrategies ignores columns with IGNORE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when IGNORE strategy is applied to differing column")
    void shouldPass_whenIgnoreStrategyApplied() {
      // Given
      final var expected =
          createTable(
              "USERS", List.of("ID", "NAME", "CREATED_AT"), List.of("1", "Alice", "2024-01-01"));
      final var actual =
          createTable(
              "USERS", List.of("ID", "NAME", "CREATED_AT"), List.of("1", "Alice", "2025-12-31"));

      // When & Then - CREATED_AT is in ignoreColumns set
      assertDoesNotThrow(
          () ->
              comparator.assertEqualsWithStrategies(
                  expected, actual, Set.of("CREATED_AT"), new HashMap<>()),
          "should not throw when ignored column differs");
    }

    /** Verifies that assertEqualsWithStrategies uses CASE_INSENSITIVE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when CASE_INSENSITIVE strategy matches case-different values")
    void shouldPass_whenCaseInsensitiveStrategyMatches() {
      // Given
      final var expected =
          createTable("USERS", List.of("ID", "EMAIL"), List.of("1", "Alice@TEST.com"));
      final var actual =
          createTable("USERS", List.of("ID", "EMAIL"), List.of("1", "alice@test.COM"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies),
          "should not throw when case-insensitive comparison matches");
    }

    /** Verifies that assertEqualsWithStrategies throws when CASE_INSENSITIVE does not match. */
    @Test
    @Tag("error")
    @DisplayName("should throw when CASE_INSENSITIVE strategy does not match")
    void shouldThrow_whenCaseInsensitiveStrategyDoesNotMatch() {
      // Given
      final var expected =
          createTable("USERS", List.of("ID", "EMAIL"), List.of("1", "alice@test.com"));
      final var actual = createTable("USERS", List.of("ID", "EMAIL"), List.of("1", "bob@test.com"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("EMAIL"),
          "exception message should mention the differing column");
    }

    /** Verifies that assertEqualsWithStrategies uses REGEX strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when REGEX strategy matches pattern")
    void shouldPass_whenRegexStrategyMatches() {
      // Given
      final var expected =
          createTable("USERS", List.of("ID", "UUID"), List.of("1", "[a-f0-9-]{36}"));
      final var actual =
          createTable(
              "USERS", List.of("ID", "UUID"), List.of("1", "550e8400-e29b-41d4-a716-446655440000"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("UUID", ColumnStrategyMapping.regex("UUID", "[a-f0-9-]{36}"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies),
          "should not throw when regex pattern matches");
    }

    /** Verifies that assertEqualsWithStrategies throws when REGEX does not match. */
    @Test
    @Tag("error")
    @DisplayName("should throw when REGEX strategy does not match")
    void shouldThrow_whenRegexStrategyDoesNotMatch() {
      // Given
      final var expected =
          createTable("USERS", List.of("ID", "UUID"), List.of("1", "[a-f0-9-]{36}"));
      final var actual = createTable("USERS", List.of("ID", "UUID"), List.of("1", "not-a-uuid"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("UUID", ColumnStrategyMapping.regex("UUID", "[a-f0-9-]{36}"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("UUID"),
          "exception message should mention the differing column");
    }

    /** Verifies that assertEqualsWithStrategies uses NOT_NULL strategy. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when NOT_NULL strategy finds non-null value")
    void shouldPass_whenNotNullStrategyFindsValue() {
      // Given
      final var expected = createTable("USERS", List.of("ID", "TOKEN"), List.of("1", "any-value"));
      final var actual =
          createTable("USERS", List.of("ID", "TOKEN"), List.of("1", "different-value"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("TOKEN", ColumnStrategyMapping.notNull("TOKEN"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies),
          "should not throw when NOT_NULL strategy finds non-null value");
    }

    /** Verifies that assertEqualsWithStrategies throws when NOT_NULL strategy finds null. */
    @Test
    @Tag("error")
    @DisplayName("should throw when NOT_NULL strategy finds null value")
    void shouldThrow_whenNotNullStrategyFindsNullValue() {
      // Given
      final var expected = createTable("USERS", List.of("ID", "TOKEN"), List.of("1", "any-value"));
      final var actual = createTableWithNullValue("USERS", List.of("ID", "TOKEN"), "1");
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("TOKEN", ColumnStrategyMapping.notNull("TOKEN"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("TOKEN"),
          "exception message should mention the null column");
    }

    /** Verifies that assertEqualsWithStrategies uses NUMERIC strategy for numeric comparison. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when NUMERIC strategy matches equivalent numeric values")
    void shouldPass_whenNumericStrategyMatchesEquivalentValues() {
      // Given
      final var expected = createTable("PRODUCTS", List.of("ID", "PRICE"), List.of("1", "99.99"));
      final var actual = createTable("PRODUCTS", List.of("ID", "PRICE"), List.of("1", "99.990"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("PRICE", ColumnStrategyMapping.numeric("PRICE"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies),
          "should not throw when NUMERIC strategy matches equivalent numeric values");
    }

    /** Verifies that assertEqualsWithStrategies throws when NUMERIC values differ. */
    @Test
    @Tag("error")
    @DisplayName("should throw when NUMERIC strategy detects different values")
    void shouldThrow_whenNumericStrategyDetectsDifferentValues() {
      // Given
      final var expected = createTable("PRODUCTS", List.of("ID", "PRICE"), List.of("1", "99.99"));
      final var actual = createTable("PRODUCTS", List.of("ID", "PRICE"), List.of("1", "100.00"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("PRICE", ColumnStrategyMapping.numeric("PRICE"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("PRICE"),
          "exception message should mention the differing column");
    }

    /** Verifies that assertEqualsWithStrategies uses TIMESTAMP_FLEXIBLE strategy. */
    @Test
    @Tag("normal")
    @DisplayName(
        "should pass when TIMESTAMP_FLEXIBLE strategy matches timestamps with different precision")
    void shouldPass_whenTimestampFlexibleStrategyMatches() {
      // Given
      final var expected =
          createTable("EVENTS", List.of("ID", "CREATED_AT"), List.of("1", "2024-01-15 10:30:00"));
      final var actual =
          createTable(
              "EVENTS", List.of("ID", "CREATED_AT"), List.of("1", "2024-01-15 10:30:00.000"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("CREATED_AT", ColumnStrategyMapping.timestampFlexible("CREATED_AT"));

      // When & Then
      assertDoesNotThrow(
          () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies),
          "should not throw when TIMESTAMP_FLEXIBLE matches timestamps with different precision");
    }

    /** Verifies that assertEqualsWithStrategies throws when TIMESTAMP_FLEXIBLE detects mismatch. */
    @Test
    @Tag("error")
    @DisplayName("should throw when TIMESTAMP_FLEXIBLE strategy detects different timestamps")
    void shouldThrow_whenTimestampFlexibleStrategyDetectsDifferentValues() {
      // Given
      final var expected =
          createTable("EVENTS", List.of("ID", "CREATED_AT"), List.of("1", "2024-01-15 10:30:00"));
      final var actual =
          createTable("EVENTS", List.of("ID", "CREATED_AT"), List.of("1", "2024-01-15 11:00:00"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("CREATED_AT", ColumnStrategyMapping.timestampFlexible("CREATED_AT"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () -> comparator.assertEqualsWithStrategies(expected, actual, Set.of(), strategies));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("CREATED_AT"),
          "exception message should mention the differing column");
    }

    /** Verifies that assertEqualsWithStrategies handles multiple strategies. */
    @Test
    @Tag("normal")
    @DisplayName("should pass when multiple strategies are applied correctly")
    void shouldPass_whenMultipleStrategiesApplied() {
      // Given
      final var expected =
          createTable(
              "USERS",
              List.of("ID", "EMAIL", "TOKEN", "CREATED_AT"),
              List.of("1", "Alice@Test.COM", "expected-token", "2024-01-01"));
      final var actual =
          createTable(
              "USERS",
              List.of("ID", "EMAIL", "TOKEN", "CREATED_AT"),
              List.of("1", "alice@test.com", "actual-token", "2025-12-31"));
      final var strategies = new HashMap<String, ColumnStrategyMapping>();
      strategies.put("EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL"));
      strategies.put("TOKEN", ColumnStrategyMapping.notNull("TOKEN"));

      // When & Then - CREATED_AT is ignored, EMAIL is case-insensitive, TOKEN is not-null
      assertDoesNotThrow(
          () ->
              comparator.assertEqualsWithStrategies(
                  expected, actual, Set.of("CREATED_AT"), strategies),
          "should not throw when multiple strategies pass");
    }

    /** Verifies that assertEqualsWithStrategies throws when row count differs. */
    @Test
    @Tag("error")
    @DisplayName("should throw when row count differs")
    void shouldThrow_whenRowCountDiffers() {
      // Given
      final var expected = createTableWithMultipleRows("USERS", List.of("ID"), List.of("1", "2"));
      final var actual = createTableWithMultipleRows("USERS", List.of("ID"), List.of("1"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () ->
                  comparator.assertEqualsWithStrategies(
                      expected, actual, Set.of(), new HashMap<>()));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("row_count"),
          "exception message should mention row count mismatch");
    }

    /** Verifies that assertEqualsWithStrategies throws when STRICT strategy fails. */
    @Test
    @Tag("error")
    @DisplayName("should throw when values differ with default STRICT strategy")
    void shouldThrow_whenValuesDifferWithStrictStrategy() {
      // Given
      final var expected = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Alice"));
      final var actual = createTable("USERS", List.of("ID", "NAME"), List.of("1", "Bob"));

      // When & Then
      final var exception =
          assertThrows(
              AssertionError.class,
              () ->
                  comparator.assertEqualsWithStrategies(
                      expected, actual, Set.of(), new HashMap<>()));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("NAME"),
          "exception message should mention the differing column");
    }
  }
}
