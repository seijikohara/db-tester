package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.Collection;
import javax.sql.DataSource;

/**
 * Service Provider Interface for query-based database assertion operations.
 *
 * <p>This SPI separates SQL query execution and comparison from pure data comparison provided by
 * {@link AssertionProvider}. Implementations execute SQL queries against a {@link DataSource} and
 * compare the results with expected datasets.
 *
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; instead, they use {@link
 * io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion} which internally delegates
 * to this provider.
 *
 * @see java.util.ServiceLoader
 * @see io.github.seijikohara.dbtester.api.assertion.DatabaseQueryAssertion
 * @see AssertionProvider
 */
public interface QueryAssertionProvider {

  /**
   * Asserts that the results of a SQL query match the expected dataset.
   *
   * <p>Executes the provided SQL query and compares the result set with the expected dataset.
   * Useful for validating queries or views that require custom SQL statements.
   *
   * @param expected the expected dataset containing reference data
   * @param dataSource the data source for executing the SQL query
   * @param tableName the name of the table in the expected dataset to compare against query results
   * @param sqlQuery the SQL query to execute (SELECT statement)
   * @param ignoreColumnNames columns to exclude from comparison (e.g., auto-generated timestamps)
   * @throws AssertionError if the query results do not match the expected dataset
   */
  void assertEqualsByQuery(
      final TableSet expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames);

  /**
   * Asserts that the results of a SQL query match the expected table.
   *
   * <p>Executes the provided SQL query and compares the result set with the expected table. Useful
   * for validating queries or views that require custom SQL statements.
   *
   * @param expected the expected table containing reference data
   * @param dataSource the data source for executing the SQL query
   * @param tableName the name to assign to the query result set for comparison purposes
   * @param sqlQuery the SQL query to execute (SELECT statement)
   * @param ignoreColumnNames columns to exclude from comparison (e.g., auto-generated timestamps)
   * @throws AssertionError if the query results do not match the expected table
   */
  void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames);
}
