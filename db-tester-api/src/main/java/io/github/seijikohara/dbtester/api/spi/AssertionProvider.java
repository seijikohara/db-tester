package io.github.seijikohara.dbtester.api.spi;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.Collection;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Service Provider Interface for database assertion operations.
 *
 * <p>This SPI abstracts the underlying database testing implementation and allows the API module to
 * remain independent of specific implementations. The actual implementation is provided by the core
 * module and loaded via {@link java.util.ServiceLoader}.
 *
 * <p>The framework discovers implementations automatically via {@link java.util.ServiceLoader}.
 * Users typically do not interact with this interface directly; instead, they use the framework's
 * test extensions (JUnit Jupiter, Spock) which internally delegate to this provider.
 *
 * @see java.util.ServiceLoader
 * @see io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion
 */
public interface AssertionProvider {

  /**
   * Asserts that two datasets are equal.
   *
   * <p>Compares all tables and rows between expected and actual datasets. Uses the default failure
   * handler for assertion failures.
   *
   * @param expected the expected dataset containing reference data
   * @param actual the actual dataset to validate against expected
   * @throws AssertionError if the datasets do not match (row count, column values, or table
   *     structure)
   */
  void assertEquals(final TableSet expected, final TableSet actual);

  /**
   * Asserts that two datasets are equal, using a custom failure handler.
   *
   * <p>Compares all tables and rows between expected and actual datasets. Custom failure handler
   * allows integration with different testing frameworks (JUnit, Spock, etc.).
   *
   * @param expected the expected dataset containing reference data
   * @param actual the actual dataset to validate against expected
   * @param failureHandler custom failure handler for formatting assertion errors, or {@code null}
   *     to use the default handler
   * @throws AssertionError if the datasets do not match (row count, column values, or table
   *     structure)
   */
  void assertEquals(
      final TableSet expected,
      final TableSet actual,
      final @Nullable AssertionFailureHandler failureHandler);

  /**
   * Asserts that two tables are equal.
   *
   * <p>Compares all columns and rows between expected and actual tables. Uses the default failure
   * handler for assertion failures.
   *
   * @param expected the expected table containing reference data
   * @param actual the actual table to validate against expected
   * @throws AssertionError if the tables do not match (row count, column values, or column
   *     structure)
   */
  void assertEquals(final Table expected, final Table actual);

  /**
   * Asserts that two tables are equal, including additional columns.
   *
   * <p>Compares tables with additional columns specified beyond those in the expected table. Useful
   * when actual table contains more columns than expected.
   *
   * @param expected the expected table containing reference data
   * @param actual the actual table to validate against expected
   * @param additionalColumnNames additional columns from actual table to include in the comparison
   * @throws AssertionError if the tables do not match (row count, column values, or column
   *     structure)
   */
  void assertEquals(
      final Table expected, final Table actual, final Collection<String> additionalColumnNames);

  /**
   * Asserts that two tables are equal, using a custom failure handler.
   *
   * <p>Compares all columns and rows between expected and actual tables. Custom failure handler
   * allows integration with different testing frameworks (JUnit, Spock, etc.).
   *
   * @param expected the expected table containing reference data
   * @param actual the actual table to validate against expected
   * @param failureHandler custom failure handler for formatting assertion errors, or {@code null}
   *     to use the default handler
   * @throws AssertionError if the tables do not match (row count, column values, or column
   *     structure)
   */
  void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler);

  /**
   * Asserts that a table in the datasets are equal, ignoring specified columns.
   *
   * <p>Compares a specific table from both datasets while excluding specified columns from
   * comparison. Useful for ignoring auto-generated values like timestamps or IDs.
   *
   * @param expected the expected dataset containing the reference table
   * @param actual the actual dataset containing the table to validate
   * @param tableName the name of the table to compare from both datasets
   * @param ignoreColumnNames columns to exclude from comparison (e.g., auto-generated timestamps)
   * @throws AssertionError if the tables do not match (excluding ignored columns)
   */
  void assertEqualsIgnoreColumns(
      final TableSet expected,
      final TableSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames);

  /**
   * Asserts that two tables are equal, ignoring specified columns.
   *
   * <p>Compares tables while excluding specified columns from comparison. Useful for ignoring
   * auto-generated values like timestamps or IDs.
   *
   * @param expected the expected table containing reference data
   * @param actual the actual table to validate against expected
   * @param ignoreColumnNames columns to exclude from comparison (e.g., auto-generated timestamps)
   * @throws AssertionError if the tables do not match (excluding ignored columns)
   */
  void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final Collection<String> ignoreColumnNames);

  /**
   * Asserts that the results of a SQL query match the expected dataset.
   *
   * <p>Executes the provided SQL query and compares the result set with the expected dataset.
   * Useful for validating queries or views that require custom SQL statements.
   *
   * @param expected the expected dataset containing reference data
   * @param dataSource the data source for executing the SQL query
   * @param sqlQuery the SQL query to execute (SELECT statement)
   * @param tableName the name of the table in the expected dataset to compare against query results
   * @param ignoreColumnNames columns to exclude from comparison (e.g., auto-generated timestamps)
   * @throws AssertionError if the query results do not match the expected dataset
   */
  void assertEqualsByQuery(
      final TableSet expected,
      final DataSource dataSource,
      final String sqlQuery,
      final String tableName,
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
