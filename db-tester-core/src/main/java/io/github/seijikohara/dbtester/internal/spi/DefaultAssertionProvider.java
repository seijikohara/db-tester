package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.dataset.DataSet;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.spi.AssertionProvider;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import io.github.seijikohara.dbtester.internal.jdbc.TableReader;
import java.util.Collection;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Default implementation of {@link AssertionProvider} that uses JDBC for database operations.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} and provides the implementation for
 * database assertion operations.
 */
public final class DefaultAssertionProvider implements AssertionProvider {

  /** The comparator for dataset assertions. */
  private final DataSetComparator comparator;

  /** Table reader for database queries. */
  private final TableReader tableReader;

  /** Creates a new instance with default comparator and table reader. */
  public DefaultAssertionProvider() {
    this.comparator = new DataSetComparator();
    this.tableReader = new TableReader();
  }

  /**
   * Creates a new instance with specified dependencies.
   *
   * @param comparator the dataset comparator
   * @param tableReader the table reader
   */
  public DefaultAssertionProvider(
      final DataSetComparator comparator, final TableReader tableReader) {
    this.comparator = comparator;
    this.tableReader = tableReader;
  }

  @Override
  public void assertEquals(final DataSet expected, final DataSet actual) {
    comparator.assertEquals(expected, actual, null);
  }

  @Override
  public void assertEquals(
      final DataSet expected,
      final DataSet actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    comparator.assertEquals(expected, actual, failureHandler);
  }

  @Override
  public void assertEquals(final Table expected, final Table actual) {
    comparator.assertEquals(expected, actual, null);
  }

  @Override
  public void assertEquals(
      final Table expected, final Table actual, final Collection<String> additionalColumnNames) {
    comparator.assertEqualsWithAdditionalColumns(expected, actual, additionalColumnNames);
  }

  @Override
  public void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    comparator.assertEquals(expected, actual, failureHandler);
  }

  @Override
  public void assertEqualsIgnoreColumns(
      final DataSet expected,
      final DataSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    comparator.assertEqualsIgnoreColumns(expected, actual, tableName, ignoreColumnNames);
  }

  @Override
  public void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final Collection<String> ignoreColumnNames) {
    comparator.assertEqualsIgnoreColumns(expected, actual, ignoreColumnNames);
  }

  @Override
  public void assertEqualsByQuery(
      final DataSet expected,
      final DataSource dataSource,
      final String sqlQuery,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    final var actualTable = tableReader.executeQuery(dataSource, sqlQuery, tableName);
    final var expectedTable =
        expected
            .getTable(new TableName(tableName))
            .orElseThrow(
                () -> new AssertionError(String.format("Expected table not found: %s", tableName)));

    comparator.assertEqualsIgnoreColumns(expectedTable, actualTable, ignoreColumnNames);
  }

  @Override
  public void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    final var actualTable = tableReader.executeQuery(dataSource, sqlQuery, tableName);
    comparator.assertEqualsIgnoreColumns(expected, actualTable, ignoreColumnNames);
  }
}
