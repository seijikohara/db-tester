package io.github.seijikohara.dbtester.api.assertion;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import javax.sql.DataSource;

/**
 * Static facade for query-based database assertions.
 *
 * <p>This facade provides assertion methods that execute SQL queries against a {@link DataSource}
 * and compare the results with expected datasets. For pure data comparison without query execution,
 * use {@link DatabaseAssertion}.
 *
 * <p>The separation follows the Single Responsibility Principle: {@link DatabaseAssertion} handles
 * in-memory data comparison, while this facade handles SQL query execution and comparison.
 *
 * @see DatabaseAssertion
 * @see QueryAssertionProvider
 */
public final class DatabaseQueryAssertion {

  /** Lazy holder for the QueryAssertionProvider instance loaded via SPI. */
  private static final class ProviderHolder {
    /** The singleton QueryAssertionProvider instance. */
    private static final QueryAssertionProvider INSTANCE = loadProvider();

    /** Private constructor to prevent instantiation. */
    private ProviderHolder() {}

    /**
     * Loads the QueryAssertionProvider implementation via ServiceLoader.
     *
     * @return the QueryAssertionProvider instance
     */
    private static QueryAssertionProvider loadProvider() {
      return ServiceLoader.load(QueryAssertionProvider.class)
          .findFirst()
          .orElseThrow(
              () ->
                  new IllegalStateException(
                      "No QueryAssertionProvider implementation found."
                          + " Add db-tester-core to your classpath."));
    }
  }

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * @throws UnsupportedOperationException always, as this class is not instantiable
   */
  private DatabaseQueryAssertion() {
    throw new UnsupportedOperationException("This class has only static methods");
  }

  /**
   * Returns the provider for delegating operations to the underlying query implementation.
   *
   * @return the query assertion provider instance loaded via ServiceLoader
   */
  private static QueryAssertionProvider getProvider() {
    return ProviderHolder.INSTANCE;
  }

  /**
   * Asserts that the results of a SQL query match the expected dataset, excluding specified columns
   * from comparison.
   *
   * <p>This method executes the provided SQL query against the specified data source and compares
   * the results with the expected dataset. This is useful for validating complex queries,
   * aggregations, or views where direct table comparison is not appropriate.
   *
   * @param expected the expected dataset containing the table to compare
   * @param dataSource the data source for executing the SQL query
   * @param tableName the name of the table in the expected dataset to compare
   * @param sqlQuery the SQL query to execute; results will be compared against the expected data
   * @param ignoreColumnNames collection of column names to exclude from comparison (may be empty)
   * @throws AssertionError if the query results do not match the expected data
   */
  public static void assertEqualsByQuery(
      final TableSet expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    getProvider().assertEqualsByQuery(expected, dataSource, tableName, sqlQuery, ignoreColumnNames);
  }

  /**
   * Asserts that the results of a SQL query match the expected dataset, excluding specified columns
   * from comparison.
   *
   * <p>This is a convenience overload that accepts column names as varargs instead of a collection.
   *
   * @param expected the expected dataset containing the table to compare
   * @param dataSource the data source for executing the SQL query
   * @param tableName the name of the table in the expected dataset to compare
   * @param sqlQuery the SQL query to execute; results will be compared against the expected data
   * @param ignoreColumnNames varargs of column names to exclude from comparison (may be empty)
   * @throws AssertionError if the query results do not match the expected data
   */
  public static void assertEqualsByQuery(
      final TableSet expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final String... ignoreColumnNames) {
    assertEqualsByQuery(expected, dataSource, tableName, sqlQuery, List.of(ignoreColumnNames));
  }

  /**
   * Asserts equality by comparing expected table against SQL query results, excluding specified
   * columns from comparison.
   *
   * @param expected the expected table data
   * @param dataSource database connection source for executing the query
   * @param tableName the name to assign to the query results
   * @param sqlQuery SQL query to retrieve actual data
   * @param ignoreColumnNames collection of column names to exclude from comparison (may be empty)
   * @throws AssertionError if the query results do not match the expected table
   */
  public static void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    getProvider().assertEqualsByQuery(expected, dataSource, tableName, sqlQuery, ignoreColumnNames);
  }

  /**
   * Asserts equality by comparing expected table against SQL query results, excluding specified
   * columns from comparison.
   *
   * @param expected the expected table data
   * @param dataSource database connection source for executing the query
   * @param tableName the name to assign to the query results
   * @param sqlQuery SQL query to retrieve actual data
   * @param ignoreColumnNames varargs of column names to exclude from comparison (may be empty)
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
}
