package io.github.seijikohara.dbtester.api.assertion;

import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.spi.DatabaseBridgeProvider;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Static facade for programmatic database assertions.
 *
 * <p>While the extension primarily relies on annotation-driven expectations, there are situations
 * where tests need direct control over intermediate validation. {@code DatabaseAssertion} supports
 * those scenarios by exposing assertion helpers that operate on framework abstractions ({@link
 * DataSet}, {@link Table}, {@link DataSource}) and internally delegate to the database bridge
 * implementation loaded via {@link ServiceLoader}. Typical use cases include verifying state
 * mid-test, asserting ad-hoc SQL queries, or comparing tables while ignoring volatile columns.
 *
 * @see io.github.seijikohara.dbtester.api.annotation.Expectation
 * @see DatabaseBridgeProvider
 */
public final class DatabaseAssertion {

  /** Lazy holder for the DatabaseBridgeProvider instance loaded via SPI. */
  private static final class BridgeHolder {
    /** The singleton DatabaseBridgeProvider instance. */
    private static final DatabaseBridgeProvider INSTANCE = loadProvider();

    /** Private constructor to prevent instantiation. */
    private BridgeHolder() {}

    /**
     * Loads the DatabaseBridgeProvider implementation via ServiceLoader.
     *
     * @return the DatabaseBridgeProvider instance
     */
    private static DatabaseBridgeProvider loadProvider() {
      return ServiceLoader.load(DatabaseBridgeProvider.class)
          .findFirst()
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "No DatabaseBridgeProvider implementation found. "
                          + "Add db-tester-core to your classpath."));
    }
  }

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * @throws UnsupportedOperationException always, as this class is not instantiable
   */
  private DatabaseAssertion() {
    throw new UnsupportedOperationException("This class has only static methods");
  }

  /**
   * Returns the bridge for delegating operations to the underlying database implementation.
   *
   * <p>This method retrieves the singleton DatabaseBridgeProvider instance loaded via SPI. The
   * bridge is lazily initialized on first access through the BridgeHolder inner class.
   *
   * @return the database bridge provider instance loaded via ServiceLoader
   */
  private static DatabaseBridgeProvider getBridge() {
    return BridgeHolder.INSTANCE;
  }

  /**
   * Asserts that a specific table in the actual dataset matches the expected dataset, excluding
   * specified columns from comparison.
   *
   * <p>This method performs a row-by-row, column-by-column comparison of the specified table, but
   * ignores the columns listed in {@code ignoreColumnNames}. This is useful for excluding
   * auto-generated columns (timestamps, IDs) or non-deterministic values from validation.
   *
   * @param expected the expected dataset containing the table to compare (must not be null)
   * @param actual the actual dataset containing the table to validate (must not be null)
   * @param tableName the name of the table to compare (must not be null or empty)
   * @param ignoreColumnNames collection of column names to exclude from comparison (must not be
   *     null, may be empty)
   * @throws AssertionError if the table data does not match the expected data
   */
  public static void assertEqualsIgnoreColumns(
      final DataSet expected,
      final DataSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    getBridge().assertEqualsIgnoreColumns(expected, actual, tableName, ignoreColumnNames);
  }

  /**
   * Asserts that a specific table in the actual dataset matches the expected dataset, excluding
   * specified columns from comparison.
   *
   * <p>This is a convenience overload that accepts column names as varargs instead of a collection.
   *
   * @param expected the expected dataset containing the table to compare (must not be null)
   * @param actual the actual dataset containing the table to validate (must not be null)
   * @param tableName the name of the table to compare (must not be null or empty)
   * @param ignoreColumnNames varargs of column names to exclude from comparison (must not be null,
   *     may be empty)
   * @throws AssertionError if the table data does not match the expected data
   */
  public static void assertEqualsIgnoreColumns(
      final DataSet expected,
      final DataSet actual,
      final String tableName,
      final String... ignoreColumnNames) {
    assertEqualsIgnoreColumns(expected, actual, tableName, List.of(ignoreColumnNames));
  }

  /**
   * Asserts that the actual table matches the expected table, excluding specified columns from
   * comparison.
   *
   * <p>This method performs a row-by-row, column-by-column comparison, but ignores the columns
   * listed in {@code ignoreColumnNames}. This is useful for excluding auto-generated columns or
   * non-deterministic values from validation.
   *
   * @param expected the expected table data (must not be null)
   * @param actual the actual table data to validate (must not be null)
   * @param ignoreColumnNames collection of column names to exclude from comparison (must not be
   *     null, may be empty)
   * @throws AssertionError if the table data does not match the expected data
   */
  public static void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final Collection<String> ignoreColumnNames) {
    getBridge().assertEqualsIgnoreColumns(expected, actual, ignoreColumnNames);
  }

  /**
   * Asserts that the actual table matches the expected table, excluding specified columns from
   * comparison.
   *
   * <p>This is a convenience overload that accepts column names as varargs instead of a collection.
   *
   * @param expected the expected table data (must not be null)
   * @param actual the actual table data to validate (must not be null)
   * @param ignoreColumnNames varargs of column names to exclude from comparison (must not be null,
   *     may be empty)
   * @throws AssertionError if the table data does not match the expected data
   */
  public static void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final String... ignoreColumnNames) {
    assertEqualsIgnoreColumns(expected, actual, List.of(ignoreColumnNames));
  }

  /**
   * Asserts that the results of a SQL query match the expected dataset, excluding specified columns
   * from comparison.
   *
   * <p>This method executes the provided SQL query against the specified data source and compares
   * the results with the expected dataset. This is useful for validating complex queries,
   * aggregations, or views where direct table comparison is not appropriate.
   *
   * @param expected the expected dataset containing the table to compare (must not be null)
   * @param dataSource the data source for executing the SQL query (must not be null)
   * @param sqlQuery the SQL query to execute; results will be compared against the expected data
   *     (must not be null or empty)
   * @param tableName the name of the table in the expected dataset to compare (must not be null or
   *     empty)
   * @param ignoreColumnNames collection of column names to exclude from comparison (must not be
   *     null, may be empty)
   * @throws AssertionError if the query results do not match the expected data
   */
  public static void assertEqualsByQuery(
      final DataSet expected,
      final DataSource dataSource,
      final String sqlQuery,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    getBridge().assertEqualsByQuery(expected, dataSource, sqlQuery, tableName, ignoreColumnNames);
  }

  /**
   * Asserts that the results of a SQL query match the expected dataset, excluding specified columns
   * from comparison.
   *
   * <p>This is a convenience overload that accepts column names as varargs instead of a collection.
   * The method executes the provided SQL query against the specified data source and compares the
   * results with the expected dataset.
   *
   * @param expected the expected dataset containing the table to compare (must not be null)
   * @param dataSource the data source for executing the SQL query (must not be null)
   * @param sqlQuery the SQL query to execute; results will be compared against the expected data
   *     (must not be null or empty)
   * @param tableName the name of the table in the expected dataset to compare (must not be null or
   *     empty)
   * @param ignoreColumnNames varargs of column names to exclude from comparison (must not be null,
   *     may be empty)
   * @throws AssertionError if the query results do not match the expected data
   */
  public static void assertEqualsByQuery(
      final DataSet expected,
      final DataSource dataSource,
      final String sqlQuery,
      final String tableName,
      final String... ignoreColumnNames) {
    assertEqualsByQuery(expected, dataSource, sqlQuery, tableName, List.of(ignoreColumnNames));
  }

  /**
   * Asserts equality by comparing expected table against SQL query results, excluding specified
   * columns from comparison.
   *
   * @param expected the expected table data (must not be null)
   * @param dataSource database connection source for executing the query (must not be null)
   * @param tableName the name to assign to the query results (must not be null or empty)
   * @param sqlQuery SQL query to retrieve actual data (must not be null or empty)
   * @param ignoreColumnNames collection of column names to exclude from comparison (must not be
   *     null, may be empty)
   * @throws AssertionError if the query results do not match the expected table
   */
  public static void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    getBridge().assertEqualsByQuery(expected, dataSource, tableName, sqlQuery, ignoreColumnNames);
  }

  /**
   * Asserts equality by comparing expected table against SQL query results, excluding specified
   * columns from comparison.
   *
   * @param expected the expected table data (must not be null)
   * @param dataSource database connection source for executing the query (must not be null)
   * @param tableName the name to assign to the query results (must not be null or empty)
   * @param sqlQuery SQL query to retrieve actual data (must not be null or empty)
   * @param ignoreColumnNames varargs of column names to exclude from comparison (must not be null,
   *     may be empty)
   * @throws AssertionError if the query results do not match the expected table
   */
  public static void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final String... ignoreColumnNames) {
    assertEqualsByQuery(expected, dataSource, tableName, sqlQuery, List.of(ignoreColumnNames));
  }

  /**
   * Asserts that the actual dataset matches the expected dataset.
   *
   * <p>This method performs a complete comparison of all tables, rows, and columns in both
   * datasets. The comparison is strict: all table names, column names, row counts, and values must
   * match exactly.
   *
   * @param expected the expected dataset (must not be null)
   * @param actual the actual dataset to validate (must not be null)
   * @throws AssertionError if the datasets do not match
   */
  public static void assertEquals(final DataSet expected, final DataSet actual) {
    getBridge().assertEquals(expected, actual);
  }

  /**
   * Asserts that the actual dataset matches the expected dataset, using a custom failure handler.
   *
   * <p>This method performs a complete comparison of all tables, rows, and columns. When mismatches
   * are detected, the provided failure handler is invoked instead of the default assertion
   * behavior. This allows for custom failure reporting, failure collection, or integration with
   * specialized assertion libraries.
   *
   * @param expected the expected dataset (must not be null)
   * @param actual the actual dataset to validate (must not be null)
   * @param failureHandler the custom failure handler, or {@code null} for default
   * @throws AssertionError if the datasets do not match (default behavior when {@code
   *     failureHandler} is {@code null})
   */
  public static void assertEquals(
      final DataSet expected,
      final DataSet actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    getBridge().assertEquals(expected, actual, failureHandler);
  }

  /**
   * Asserts that the actual table matches the expected table.
   *
   * <p>This method performs a complete row-by-row, column-by-column comparison. The comparison is
   * strict: all column names, row counts, and values must match exactly.
   *
   * @param expected the expected table data (must not be null)
   * @param actual the actual table data to validate (must not be null)
   * @throws AssertionError if the tables do not match
   */
  public static void assertEquals(final Table expected, final Table actual) {
    getBridge().assertEquals(expected, actual);
  }

  /**
   * Asserts that the actual table matches the expected table, including additional columns in the
   * comparison.
   *
   * <p>This method supplements the table schema with additional column names before performing the
   * comparison. This is useful when the expected table metadata does not include all columns that
   * should be validated. Data types for the additional columns are inferred from the actual table
   * data.
   *
   * @param expected the expected table data (must not be null)
   * @param actual the actual table data to validate (must not be null)
   * @param additionalColumnNames collection of additional columns to include in the comparison
   *     (must not be null, may be empty)
   * @throws AssertionError if the tables do not match
   */
  public static void assertEquals(
      final Table expected, final Table actual, final Collection<String> additionalColumnNames) {
    getBridge().assertEquals(expected, actual, additionalColumnNames);
  }

  /**
   * Asserts that the actual table matches the expected table, including additional columns in the
   * comparison.
   *
   * <p>This is a convenience overload that accepts column names as varargs instead of a collection.
   * The method supplements the table schema with additional column names before performing the
   * comparison. Data types for the additional columns are inferred from the actual table data.
   *
   * @param expected the expected table data (must not be null)
   * @param actual the actual table data to validate (must not be null)
   * @param additionalColumnNames varargs of additional columns to include in the comparison (must
   *     not be null, may be empty)
   * @throws AssertionError if the tables do not match
   */
  public static void assertEquals(
      final Table expected, final Table actual, final String... additionalColumnNames) {
    assertEquals(expected, actual, List.of(additionalColumnNames));
  }

  /**
   * Asserts that the actual table matches the expected table, using a custom failure handler.
   *
   * <p>This method performs a complete row-by-row, column-by-column comparison. When mismatches are
   * detected, the provided failure handler is invoked instead of the default assertion behavior.
   * This allows for custom failure reporting, failure collection, or integration with specialized
   * assertion libraries.
   *
   * @param expected the expected table data (must not be null)
   * @param actual the actual table data to validate (must not be null)
   * @param failureHandler the custom failure handler, or {@code null} for default
   * @throws AssertionError if the tables do not match (default behavior when {@code failureHandler}
   *     is {@code null})
   */
  public static void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    getBridge().assertEquals(expected, actual, failureHandler);
  }
}
