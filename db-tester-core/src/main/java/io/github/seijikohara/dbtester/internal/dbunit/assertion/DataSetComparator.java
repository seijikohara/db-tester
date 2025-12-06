package io.github.seijikohara.dbtester.internal.dbunit.assertion;

import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.dataset.IDataSet;
import org.jspecify.annotations.Nullable;

/** Performs structural and content comparisons between two DbUnit datasets. */
final class DataSetComparator {
  /** The parent DatabaseAssert instance for accessing utility methods. */
  private final DatabaseAssert databaseAssert;

  /**
   * Creates a dataset comparator.
   *
   * @param databaseAssert the parent DatabaseAssert instance
   */
  DataSetComparator(final DatabaseAssert databaseAssert) {
    this.databaseAssert = databaseAssert;
  }

  /**
   * Compares two datasets for equality.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset from the database
   * @param failureHandler the handler for processing comparison failures, or null to use default
   * @throws ValidationException if the datasets do not match
   */
  void compare(
      final IDataSet expected,
      final IDataSet actual,
      final @Nullable FailureHandler failureHandler) {
    if (isSameInstance(expected, actual)) {
      return;
    }

    final var handler =
        Optional.ofNullable(failureHandler).orElse(databaseAssert.getFailureHandler());

    validateTableStructure(expected, actual, handler);
    validateTableContents(expected, actual, handler);
  }

  /**
   * Checks if the expected and actual datasets are the same instance.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @return true if both datasets are the same instance, false otherwise
   */
  private boolean isSameInstance(final IDataSet expected, final IDataSet actual) {
    return expected == actual;
  }

  /**
   * Validates that both datasets have the same table structure.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param failureHandler the failure handler
   * @throws ValidationException if the table structures do not match
   */
  private void validateTableStructure(
      final IDataSet expected, final IDataSet actual, final FailureHandler failureHandler) {
    final var expectedTableNames = databaseAssert.getTableNames(expected);
    final var actualTableNames = databaseAssert.getTableNames(actual);

    validateTableCount(expectedTableNames, actualTableNames, failureHandler);
    validateTableNames(expectedTableNames, actualTableNames, failureHandler);
  }

  /**
   * Validates that both datasets contain the same number of tables.
   *
   * @param expectedTableNames list of expected table names
   * @param actualTableNames list of actual table names
   * @param failureHandler the failure handler
   */
  private void validateTableCount(
      final List<TableName> expectedTableNames,
      final List<TableName> actualTableNames,
      final FailureHandler failureHandler) {
    if (expectedTableNames.size() != actualTableNames.size()) {
      throw failureHandler.createFailure(
          "table count",
          String.valueOf(expectedTableNames.size()),
          String.valueOf(actualTableNames.size()));
    }
  }

  /**
   * Validates that both datasets have matching table names in the same order.
   *
   * @param expectedTableNames list of expected table names
   * @param actualTableNames list of actual table names
   * @param failureHandler the failure handler
   * @throws ValidationException if the table names do not match
   */
  private void validateTableNames(
      final List<TableName> expectedTableNames,
      final List<TableName> actualTableNames,
      final FailureHandler failureHandler) {
    IntStream.range(0, expectedTableNames.size())
        .filter(index -> !expectedTableNames.get(index).equals(actualTableNames.get(index)))
        .findFirst()
        .ifPresent(
            mismatchIndex -> {
              throw new ValidationException(
                  failureHandler.createFailure(
                      "tables", expectedTableNames.toString(), actualTableNames.toString()));
            });
  }

  /**
   * Validates the contents of all tables in both datasets.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param failureHandler the failure handler
   * @throws ValidationException if the table contents do not match
   */
  private void validateTableContents(
      final IDataSet expected, final IDataSet actual, final FailureHandler failureHandler) {
    final var expectedTableNames = databaseAssert.getTableNames(expected);
    final var errorMessages =
        collectTableErrors(expected, actual, expectedTableNames, failureHandler);

    if (!errorMessages.isEmpty()) {
      throw failureHandler.createFailure(
          String.format(
              "Comparison failure%s%s",
              System.lineSeparator(), String.join(System.lineSeparator(), errorMessages)));
    }
  }

  /**
   * Collects validation errors from all tables.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param expectedTableNames collection of table names to validate
   * @param failureHandler the failure handler
   * @return list of error messages for failed validations
   */
  private List<String> collectTableErrors(
      final IDataSet expected,
      final IDataSet actual,
      final Collection<TableName> expectedTableNames,
      final FailureHandler failureHandler) {
    return expectedTableNames.stream()
        .map(tableName -> validateSingleTable(expected, actual, tableName, failureHandler))
        .flatMap(Optional::stream)
        .toList();
  }

  /**
   * Validates a single table and returns an error message if validation fails.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset
   * @param tableName the name of the table to validate
   * @param failureHandler the failure handler
   * @return an Optional containing an error message if validation fails, or empty if successful
   */
  private Optional<String> validateSingleTable(
      final IDataSet expected,
      final IDataSet actual,
      final TableName tableName,
      final FailureHandler failureHandler) {
    try {
      databaseAssert.assertEquals(
          expected.getTable(tableName.value()), actual.getTable(tableName.value()), failureHandler);
      return Optional.empty();
    } catch (final AssertionError error) {
      return Optional.ofNullable(error.getMessage())
          .or(() -> Optional.of(error.getClass().getSimpleName()));
    } catch (final DatabaseUnitException e) {
      return Optional.of(
          String.format("DataSet error for table %s: %s", tableName, e.getMessage()));
    }
  }
}
