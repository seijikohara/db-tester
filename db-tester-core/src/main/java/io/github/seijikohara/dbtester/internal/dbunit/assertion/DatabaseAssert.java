package io.github.seijikohara.dbtester.internal.dbunit.assertion;

import io.github.seijikohara.dbtester.api.exception.ValidationException;
import io.github.seijikohara.dbtester.internal.domain.ColumnName;
import io.github.seijikohara.dbtester.internal.domain.TableName;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.jspecify.annotations.Nullable;

/** Lightweight facade around {@link DbUnitAssert} used by the bridge when comparing datasets. */
public final class DatabaseAssert {

  /**
   * Internal delegate that exposes protected methods from DbUnitAssert.
   *
   * <p>This inner class exists to provide package-private access to DbUnitAssert's protected
   * methods without exposing them in DatabaseAssert's public API.
   */
  private static final class DbUnitAssertDelegate extends DbUnitAssert {
    /** Default constructor. */
    DbUnitAssertDelegate() {}

    @Override
    protected FailureHandler getDefaultFailureHandler() {
      return super.getDefaultFailureHandler();
    }

    @Override
    protected String[] getSortedTableNames(final IDataSet dataSet) throws DataSetException {
      return super.getSortedTableNames(dataSet);
    }

    @Override
    protected boolean skipCompare(
        final String columnName, final Object expectedValue, final Object actualValue) {
      // Convert to ColumnName for type safety validation
      final var unused = new ColumnName(columnName);
      return super.skipCompare(columnName, expectedValue, actualValue);
    }
  }

  /** Delegate instance for DbUnit assertion operations. */
  private final DbUnitAssertDelegate delegate;

  /** Comparator for dataset-level validation. */
  private final DataSetComparator dataSetComparator;

  /** Comparator for table-level validation. */
  private final TableComparator tableComparator;

  /** Public constructor for cross-package internal use. */
  public DatabaseAssert() {
    this.delegate = new DbUnitAssertDelegate();
    this.dataSetComparator = new DataSetComparator(this);
    this.tableComparator = new TableComparator(this);
  }

  /**
   * Asserts that two datasets are equal for a specific table, excluding specified columns.
   *
   * <p>This method extracts the named table from both datasets and delegates to the table-level
   * comparison method.
   *
   * @param expected the expected dataset containing the table to compare
   * @param actual the actual dataset from the database
   * @param tableName the name of the table to compare
   * @param ignoreColumnNames collection of column names to exclude from comparison (e.g.,
   *     auto-generated IDs, timestamps)
   * @throws ValidationException if the assertion fails or the table cannot be accessed
   */
  public void assertEqualsIgnoreColumns(
      final IDataSet expected,
      final IDataSet actual,
      final TableName tableName,
      final Collection<ColumnName> ignoreColumnNames) {
    try {
      final var expectedTable = expected.getTable(tableName.value());
      final var actualTable = actual.getTable(tableName.value());
      assertEqualsIgnoreColumns(expectedTable, actualTable, ignoreColumnNames);
    } catch (final DataSetException e) {
      throw new ValidationException(
          String.format("Failed to retrieve table '%s' for comparison", tableName), e);
    }
  }

  /**
   * Asserts that two tables are equal, excluding specified columns from comparison.
   *
   * <p>This method is useful when comparing tables that contain auto-generated or non-deterministic
   * columns (such as timestamps, auto-increment IDs, or UUIDs) that should not be part of the
   * assertion.
   *
   * @param expected the expected table data
   * @param actual the actual table data from the database
   * @param ignoreColumnNames collection of column names to exclude from comparison
   * @throws ValidationException if the assertion fails
   */
  public void assertEqualsIgnoreColumns(
      final ITable expected, final ITable actual, final Collection<ColumnName> ignoreColumnNames) {
    final var filteredExpected = ColumnFilter.excludeColumns(expected, ignoreColumnNames);
    final var filteredActual = ColumnFilter.excludeColumns(actual, ignoreColumnNames);
    assertEquals(filteredExpected, filteredActual);
  }

  /**
   * Asserts that expected dataset matches database state retrieved by a custom SQL query.
   *
   * <p>This method is useful for complex validation scenarios where the expected data cannot be
   * represented by a single table comparison, such as validating aggregated results or data from
   * multiple joined tables.
   *
   * @param expected the expected dataset containing the table to compare
   * @param connection the database connection to execute the query
   * @param sqlQuery the SQL query to retrieve actual data
   * @param tableName the logical table name for the comparison
   * @param ignoreColumnNames collection of column names to exclude from comparison
   * @throws ValidationException if the assertion fails or the SQL query execution fails
   */
  public void assertEqualsByQuery(
      final IDataSet expected,
      final IDatabaseConnection connection,
      final String sqlQuery,
      final TableName tableName,
      final Collection<ColumnName> ignoreColumnNames) {
    try {
      final var expectedTable = expected.getTable(tableName.value());
      assertEqualsByQuery(expectedTable, connection, tableName, sqlQuery, ignoreColumnNames);
    } catch (final DataSetException e) {
      throw new ValidationException(
          String.format("Failed to retrieve table '%s' from dataset", tableName), e);
    }
  }

  /**
   * Asserts that expected table matches database state retrieved by a custom SQL query.
   *
   * <p>This method executes the provided SQL query to retrieve actual data and compares it with the
   * expected table, excluding the specified columns.
   *
   * @param expected the expected table data
   * @param connection the database connection to execute the query
   * @param tableName the logical table name for the comparison
   * @param sqlQuery the SQL query to retrieve actual data
   * @param ignoreColumnNames collection of column names to exclude from comparison
   * @throws ValidationException if the assertion fails or the SQL query execution fails
   */
  public void assertEqualsByQuery(
      final ITable expected,
      final IDatabaseConnection connection,
      final TableName tableName,
      final String sqlQuery,
      final Collection<ColumnName> ignoreColumnNames) {
    try {
      final var actualTable = connection.createQueryTable(tableName.value(), sqlQuery);
      assertEqualsIgnoreColumns(expected, actualTable, ignoreColumnNames);
    } catch (final DataSetException | SQLException e) {
      throw new ValidationException(
          String.format("Failed to execute query for table '%s': %s", tableName, sqlQuery), e);
    }
  }

  /**
   * Asserts that two datasets are equal with default failure handling.
   *
   * <p>This method performs comprehensive dataset comparison including both structural validation
   * (table names and counts) and content validation (row-by-row comparison) using the default
   * failure handler.
   *
   * <p>The actual comparison logic is delegated to {@link DataSetComparator} for better testability
   * and maintainability.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset from the database
   * @throws ValidationException if the assertion fails
   */
  public void assertEquals(final IDataSet expected, final IDataSet actual) {
    dataSetComparator.compare(expected, actual, null);
  }

  /**
   * Asserts that two datasets are equal with custom failure handling.
   *
   * <p>This method performs comprehensive dataset comparison including both structural validation
   * (table names and counts) and content validation (row-by-row comparison). The provided failure
   * handler controls how comparison errors are reported.
   *
   * <p>The actual comparison logic is delegated to {@link DataSetComparator} for better testability
   * and maintainability.
   *
   * @param expected the expected dataset
   * @param actual the actual dataset from the database
   * @param failureHandler the handler for processing comparison failures, or {@code null} to use
   *     the default handler
   * @throws ValidationException if the assertion fails
   */
  public void assertEquals(
      final IDataSet expected,
      final IDataSet actual,
      final @Nullable FailureHandler failureHandler) {
    dataSetComparator.compare(expected, actual, failureHandler);
  }

  /**
   * Asserts that two tables are equal with default failure handling.
   *
   * <p>This method performs table comparison using the default failure handler.
   *
   * @param expected the expected table data
   * @param actual the actual table data from the database
   * @throws ValidationException if the assertion fails
   */
  public void assertEquals(final ITable expected, final ITable actual) {
    try {
      delegate.assertEquals(expected, actual);
    } catch (final DatabaseUnitException e) {
      throw new ValidationException("Table comparison failed", e);
    }
  }

  /**
   * Asserts that two tables are equal with custom failure handling.
   *
   * <p>This method performs table comparison and delegates to DbUnit's assertion logic.
   *
   * @param expected the expected table data
   * @param actual the actual table data from the database
   * @param failureHandler the handler for processing comparison failures
   * @throws ValidationException if the assertion fails
   */
  public void assertEquals(
      final ITable expected, final ITable actual, final @Nullable FailureHandler failureHandler) {
    try {
      delegate.assertEquals(expected, actual, failureHandler);
    } catch (final DatabaseUnitException e) {
      throw new ValidationException("Table comparison failed", e);
    }
  }

  /**
   * Asserts that two tables are equal with additional column information.
   *
   * <p>This method allows providing additional column metadata for comparison when the table
   * metadata is incomplete or needs to be supplemented.
   *
   * @param expected the expected table data
   * @param actual the actual table data from the database
   * @param additionalColumns collection of additional columns with type information for comparison
   * @throws ValidationException if the assertion fails
   */
  public void assertEquals(
      final ITable expected, final ITable actual, final Collection<Column> additionalColumns) {
    try {
      delegate.assertEquals(expected, actual, additionalColumns.toArray(Column[]::new));
    } catch (final DatabaseUnitException e) {
      throw new ValidationException("Table comparison with additional column info failed", e);
    }
  }

  /**
   * Compares table data using type-aware column comparison.
   *
   * <p>This method provides enhanced error reporting and comprehensive validation. It delegates to
   * {@link TableComparator} to perform cell-by-cell comparison with proper type handling.
   *
   * @param expected the expected table data
   * @param actual the actual table data from the database
   * @param comparisonColumns the columns to compare with their associated data types
   * @param failureHandler the handler for processing comparison failures
   */
  void compareData(
      final ITable expected,
      final ITable actual,
      final Collection<DbUnitAssert.ComparisonColumn> comparisonColumns,
      final FailureHandler failureHandler) {
    tableComparator.compare(expected, actual, comparisonColumns, failureHandler);
  }

  /**
   * Package-private accessor for the default failure handler.
   *
   * <p>This method provides access to DbUnit's default failure handler for use by extracted
   * comparison classes.
   *
   * @return the default failure handler
   */
  FailureHandler getFailureHandler() {
    return delegate.getDefaultFailureHandler();
  }

  /**
   * Package-private accessor for sorted table names.
   *
   * <p>This method provides access to DbUnit's table name sorting logic for use by extracted
   * comparison classes.
   *
   * @param dataSet the dataset to get table names from
   * @return list of sorted table names
   * @throws ValidationException if table names cannot be retrieved
   */
  List<TableName> getTableNames(final IDataSet dataSet) {
    try {
      return Stream.of(delegate.getSortedTableNames(dataSet)).map(TableName::new).toList();
    } catch (final DataSetException e) {
      throw new ValidationException("Failed to retrieve table names from dataset", e);
    }
  }

  /**
   * Package-private accessor for skip comparison logic.
   *
   * <p>This method provides access to DbUnit's skip comparison logic for use by extracted
   * comparison classes.
   *
   * @param columnName the column name
   * @param expectedValue the expected value
   * @param actualValue the actual value
   * @return true if comparison should be skipped
   */
  boolean shouldSkipCompare(
      final ColumnName columnName, final Object expectedValue, final Object actualValue) {
    return delegate.skipCompare(columnName.value(), expectedValue, actualValue);
  }
}
