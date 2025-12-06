package io.github.seijikohara.dbtester.internal.assertion;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dataset.Row;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.DataValue;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;

/**
 * Comparator for DataSet and Table objects.
 *
 * <p>This class provides methods to compare datasets and tables, optionally ignoring specific
 * columns or using custom failure handlers.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * var comparator = new DataSetComparator();
 * comparator.assertEquals(expectedDataSet, actualDataSet, failureHandler);
 * }</pre>
 */
public class DataSetComparator {

  /** Creates a new dataset comparator. */
  public DataSetComparator() {
    // Default constructor
  }

  /**
   * Asserts that two datasets are equal.
   *
   * <p>Compares table counts and individual tables between expected and actual datasets.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param failureHandler optional custom failure handler for assertion failures
   */
  public void assertEquals(
      final DataSet expected,
      final DataSet actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    final var expectedTables = expected.getTables();
    final var actualTables = actual.getTables();

    if (expectedTables.size() != actualTables.size()) {
      fail(
          String.format(
              "Table count mismatch: expected %d tables, but got %d",
              expectedTables.size(), actualTables.size()),
          expectedTables.size(),
          actualTables.size(),
          failureHandler);
      return;
    }

    expectedTables.forEach(
        expectedTable -> {
          final var tableName = expectedTable.getName();
          actual
              .getTable(tableName)
              .ifPresentOrElse(
                  actualTable -> assertEquals(expectedTable, actualTable, failureHandler),
                  () ->
                      fail(
                          "Table not found: " + tableName.value(),
                          tableName.value(),
                          null,
                          failureHandler));
        });
  }

  /**
   * Asserts that two tables are equal.
   *
   * <p>Compares row counts and individual rows between expected and actual tables.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param failureHandler optional custom failure handler for assertion failures
   */
  public void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    if (expectedRows.size() != actualRows.size()) {
      fail(
          String.format(
              "Row count mismatch in table '%s': expected %d rows, but got %d",
              expected.getName().value(), expectedRows.size(), actualRows.size()),
          expectedRows.size(),
          actualRows.size(),
          failureHandler);
      return;
    }

    final var tableName = expected.getName();
    IntStream.range(0, expectedRows.size())
        .forEach(
            i -> compareRows(tableName, i, expectedRows.get(i), actualRows.get(i), failureHandler));
  }

  /**
   * Asserts that two tables are equal, including additional columns in comparison.
   *
   * <p>This method compares all columns from the expected table plus any additional columns
   * specified. This is useful when the actual table may contain columns not present in the expected
   * table that should also be compared.
   *
   * <p>The comparison includes:
   *
   * <ul>
   *   <li>All columns present in the expected table
   *   <li>Any additional columns specified in {@code additionalColumnNames}
   * </ul>
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param additionalColumnNames additional column names to include in comparison
   * @throws AssertionError if row counts differ or if any column values differ
   */
  public void assertEqualsWithAdditionalColumns(
      final Table expected, final Table actual, final Collection<String> additionalColumnNames) {
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    if (expectedRows.size() != actualRows.size()) {
      throw new AssertionError(
          String.format(
              "Row count mismatch in table '%s': expected %d rows, but got %d",
              expected.getName().value(), expectedRows.size(), actualRows.size()));
    }

    // Combine expected columns with additional columns
    final Set<ColumnName> columnsToCompare = new HashSet<>(expected.getColumns());
    additionalColumnNames.stream().map(ColumnName::new).forEach(columnsToCompare::add);

    final var tableName = expected.getName();
    IntStream.range(0, expectedRows.size())
        .forEach(
            i ->
                compareRowsWithColumns(
                    tableName, i, expectedRows.get(i), actualRows.get(i), columnsToCompare));
  }

  /**
   * Compares two rows using only the specified columns.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param columnsToCompare the set of columns to compare
   * @throws AssertionError if any column values differ
   */
  private void compareRowsWithColumns(
      final TableName tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final Set<ColumnName> columnsToCompare) {
    columnsToCompare.forEach(
        columnName -> {
          final var expectedValue = expected.getValue(columnName);
          final var actualValue = actual.getValue(columnName);

          if (!Objects.equals(expectedValue, actualValue)) {
            throw new AssertionError(
                String.format(
                    "Value mismatch in table '%s', row %d, column '%s': expected '%s', but got '%s'",
                    tableName.value(),
                    rowIndex,
                    columnName.value(),
                    extractValueString(expectedValue),
                    extractValueString(actualValue)));
          }
        });
  }

  /**
   * Asserts that two datasets are equal for a specific table, ignoring specified columns.
   *
   * <p>Extracts the specified table from both datasets and compares them while ignoring the
   * specified columns.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param tableName the table name to compare
   * @param ignoreColumnNames columns to ignore during comparison
   * @throws AssertionError if the table is not found in either dataset or if values differ
   */
  public void assertEqualsIgnoreColumns(
      final DataSet expected,
      final DataSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    final var tableNameObj = new TableName(tableName);
    final var expectedTable =
        expected
            .getTable(tableNameObj)
            .orElseThrow(() -> new AssertionError("Expected table not found: " + tableName));
    final var actualTable =
        actual
            .getTable(tableNameObj)
            .orElseThrow(() -> new AssertionError("Actual table not found: " + tableName));

    assertEqualsIgnoreColumns(expectedTable, actualTable, ignoreColumnNames);
  }

  /**
   * Asserts that two tables are equal, ignoring specified columns.
   *
   * <p>Compares all columns except those specified in the ignore set.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param ignoreColumnNames columns to ignore during comparison
   * @throws AssertionError if row counts differ or if any non-ignored column values differ
   */
  public void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final Collection<String> ignoreColumnNames) {
    final var ignoreSet = Set.copyOf(ignoreColumnNames);
    final var expectedRows = expected.getRows();
    final var actualRows = actual.getRows();

    if (expectedRows.size() != actualRows.size()) {
      throw new AssertionError(
          String.format(
              "Row count mismatch in table '%s': expected %d rows, but got %d",
              expected.getName().value(), expectedRows.size(), actualRows.size()));
    }

    final var tableName = expected.getName();
    IntStream.range(0, expectedRows.size())
        .forEach(
            i ->
                compareRowsIgnoreColumns(
                    tableName, i, expectedRows.get(i), actualRows.get(i), ignoreSet));
  }

  /**
   * Compares two rows and reports mismatches via failure handler.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param failureHandler optional custom failure handler
   */
  private void compareRows(
      final TableName tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    expected
        .getValues()
        .keySet()
        .forEach(
            columnName -> {
              final var expectedValue = expected.getValue(columnName);
              final var actualValue = actual.getValue(columnName);

              if (!Objects.equals(expectedValue, actualValue)) {
                fail(
                    String.format(
                        "Value mismatch in table '%s', row %d, column '%s': expected '%s', but got '%s'",
                        tableName.value(),
                        rowIndex,
                        columnName.value(),
                        extractValueString(expectedValue),
                        extractValueString(actualValue)),
                    extractValueOrNull(expectedValue),
                    extractValueOrNull(actualValue),
                    failureHandler);
              }
            });
  }

  /**
   * Compares two rows while ignoring specified columns.
   *
   * @param tableName the table name for error reporting
   * @param rowIndex the row index for error reporting
   * @param expected the expected row
   * @param actual the actual row
   * @param ignoreSet set of column names to ignore
   * @throws AssertionError if any non-ignored column values differ
   */
  private void compareRowsIgnoreColumns(
      final TableName tableName,
      final int rowIndex,
      final Row expected,
      final Row actual,
      final Set<String> ignoreSet) {
    expected.getValues().keySet().stream()
        .filter(columnName -> !ignoreSet.contains(columnName.value()))
        .forEach(
            columnName -> {
              final var expectedValue = expected.getValue(columnName);
              final var actualValue = actual.getValue(columnName);

              if (!Objects.equals(expectedValue, actualValue)) {
                throw new AssertionError(
                    String.format(
                        "Value mismatch in table '%s', row %d, column '%s': expected '%s', but got '%s'",
                        tableName.value(),
                        rowIndex,
                        columnName.value(),
                        extractValueString(expectedValue),
                        extractValueString(actualValue)));
              }
            });
  }

  /**
   * Extracts the string representation of a data value, or "null" if null.
   *
   * @param dataValue the data value to extract
   * @return the value string or "null"
   */
  private String extractValueString(final @Nullable DataValue dataValue) {
    return Optional.ofNullable(dataValue)
        .map(DataValue::value)
        .map(Object::toString)
        .orElse("null");
  }

  /**
   * Extracts the underlying value from a data value, or null if the data value is null.
   *
   * @param dataValue the data value to extract
   * @return the underlying value or null
   */
  private @Nullable Object extractValueOrNull(final @Nullable DataValue dataValue) {
    return Optional.ofNullable(dataValue).map(DataValue::value).orElse(null);
  }

  /**
   * Reports an assertion failure via the handler or throws an AssertionError.
   *
   * @param message the failure message
   * @param expected the expected value
   * @param actual the actual value
   * @param failureHandler optional custom failure handler
   * @throws AssertionError if no failure handler is provided
   */
  private void fail(
      final String message,
      final @Nullable Object expected,
      final @Nullable Object actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    Optional.ofNullable(failureHandler)
        .ifPresentOrElse(
            handler -> handler.handleFailure(message, expected, actual),
            () -> {
              throw new AssertionError(message);
            });
  }
}
