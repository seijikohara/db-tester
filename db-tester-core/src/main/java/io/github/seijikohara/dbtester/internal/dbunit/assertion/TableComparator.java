package io.github.seijikohara.dbtester.internal.dbunit.assertion;

import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dbunit.assertion.DbUnitAssert.ComparisonColumn;
import org.dbunit.assertion.Difference;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;

/** Performs per-table comparisons using DbUnit {@link ComparisonColumn} metadata. */
final class TableComparator {
  /** The parent DatabaseAssert instance for accessing utility methods. */
  private final DatabaseAssert databaseAssert;

  /**
   * Creates a table comparator.
   *
   * @param databaseAssert the parent DatabaseAssert instance
   */
  TableComparator(final DatabaseAssert databaseAssert) {
    this.databaseAssert = databaseAssert;
  }

  /**
   * Compares table data using type-aware column comparison.
   *
   * @param expected the expected table data
   * @param actual the actual table data from the database
   * @param comparisonColumns the columns to compare with their associated data types
   * @param failureHandler the handler for processing comparison failures
   */
  void compare(
      final ITable expected,
      final ITable actual,
      final Collection<ComparisonColumn> comparisonColumns,
      final FailureHandler failureHandler) {
    validateNotNull(expected, "expected");
    validateNotNull(actual, "actual");
    validateNotNull(comparisonColumns, "comparisonColumns");
    validateNotNull(failureHandler, "failureHandler");

    final var errorMessages = collectAllErrors(expected, actual, comparisonColumns, failureHandler);
    if (!errorMessages.isEmpty()) {
      throw failureHandler.createFailure(String.join(System.lineSeparator(), errorMessages));
    }
  }

  /**
   * Validates that a parameter is not null.
   *
   * @param parameter the parameter value to validate
   * @param parameterName the name of the parameter for error reporting
   * @param <T> the type of the parameter being validated
   * @return the validated non-null parameter
   * @throws NullPointerException if the parameter is null
   */
  private static <T> T validateNotNull(final T parameter, final String parameterName) {
    return Optional.ofNullable(parameter)
        .orElseThrow(
            () ->
                new NullPointerException(
                    String.format("The parameter '%s' must not be null", parameterName)));
  }

  /**
   * Collects all validation errors from comparing all rows.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param comparisonColumns the columns to compare
   * @param failureHandler the failure handler
   * @return list of error messages for all detected differences
   */
  private List<String> collectAllErrors(
      final ITable expected,
      final ITable actual,
      final Collection<ComparisonColumn> comparisonColumns,
      final FailureHandler failureHandler) {
    final var rowCount = expected.getRowCount();
    return IntStream.range(0, rowCount)
        .boxed()
        .flatMap(
            rowIndex -> compareRow(expected, actual, rowIndex, comparisonColumns, failureHandler))
        .flatMap(Optional::stream)
        .toList();
  }

  /**
   * Compares all columns in a single row.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param rowIndex the zero-based row index to compare
   * @param comparisonColumns the columns to compare
   * @param failureHandler the failure handler
   * @return stream of error messages wrapped in Optional (empty if comparison succeeds)
   */
  private Stream<Optional<String>> compareRow(
      final ITable expected,
      final ITable actual,
      final int rowIndex,
      final Collection<ComparisonColumn> comparisonColumns,
      final FailureHandler failureHandler) {
    return comparisonColumns.stream()
        .map(
            comparisonColumn ->
                compareColumn(expected, actual, rowIndex, comparisonColumn, failureHandler));
  }

  /**
   * Compares a single column value between expected and actual tables.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param rowIndex the zero-based row index
   * @param column the comparison column specification with data type information
   * @param failureHandler the failure handler
   * @return an Optional containing an error message if comparison fails, or empty if values match
   */
  private Optional<String> compareColumn(
      final ITable expected,
      final ITable actual,
      final int rowIndex,
      final ComparisonColumn column,
      final FailureHandler failureHandler) {
    final var columnName = new ColumnName(column.getColumnName());
    final var dataType = column.getDataType();

    try {
      final var expectedValue = expected.getValue(rowIndex, columnName.value());
      final var actualValue = actual.getValue(rowIndex, columnName.value());

      if (databaseAssert.shouldSkipCompare(columnName, expectedValue, actualValue)) {
        return Optional.empty();
      }

      if (dataType.compare(expectedValue, actualValue) != 0) {
        return handleDifference(
            expected, actual, rowIndex, columnName, expectedValue, actualValue, failureHandler);
      }

      return Optional.empty();
    } catch (final DataSetException e) {
      return Optional.of(createDataSetErrorMessage(rowIndex, columnName, e));
    }
  }

  /**
   * Handles a detected difference between expected and actual values.
   *
   * @param expected the expected table
   * @param actual the actual table
   * @param rowIndex the zero-based row index where the difference was found
   * @param columnName the name of the column containing the difference
   * @param expectedValue the expected value
   * @param actualValue the actual value from the database
   * @param failureHandler the failure handler
   * @return an Optional containing an error message, or empty if the handler does not throw
   */
  private Optional<String> handleDifference(
      final ITable expected,
      final ITable actual,
      final int rowIndex,
      final ColumnName columnName,
      final Object expectedValue,
      final Object actualValue,
      final FailureHandler failureHandler) {
    final var diff =
        new Difference(expected, actual, rowIndex, columnName.value(), expectedValue, actualValue);

    try {
      failureHandler.handle(diff);
      return Optional.empty();
    } catch (final AssertionError error) {
      return Optional.ofNullable(error.getMessage())
          .or(() -> Optional.of(error.getClass().getSimpleName()));
    }
  }

  /**
   * Creates a formatted error message for dataset access exceptions.
   *
   * @param rowIndex the zero-based row index where the error occurred
   * @param columnName the name of the column being accessed when the error occurred
   * @param e the exception that was thrown
   * @return a formatted error message with row and column context
   */
  private String createDataSetErrorMessage(
      final int rowIndex, final ColumnName columnName, final DataSetException e) {
    return String.format(
        "DataSet error at row %d, column %s: %s", rowIndex, columnName, e.getMessage());
  }
}
