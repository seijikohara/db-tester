package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.domain.TableName;
import io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import io.github.seijikohara.dbtester.internal.jdbc.read.TableReader;
import java.util.Collection;
import javax.sql.DataSource;

/**
 * Default implementation of {@link QueryAssertionProvider} that uses JDBC for query execution.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} and provides the implementation for
 * query-based database assertion operations. It executes SQL queries via {@link TableReader} and
 * delegates comparison to {@link DataSetComparator}.
 */
public final class DefaultQueryAssertionProvider implements QueryAssertionProvider {

  /** The comparator for dataset assertions. */
  private final DataSetComparator comparator;

  /** Table reader for database queries. */
  private final TableReader tableReader;

  /** Creates a new instance with default comparator and table reader. */
  public DefaultQueryAssertionProvider() {
    this(OperationDefaults.standard());
  }

  /**
   * Creates a new instance with specified operation defaults.
   *
   * @param operationDefaults the operation defaults to use
   */
  public DefaultQueryAssertionProvider(final OperationDefaults operationDefaults) {
    this(new DataSetComparator(operationDefaults), new TableReader());
  }

  /**
   * Creates a new instance with specified dependencies.
   *
   * @param comparator the dataset comparator
   * @param tableReader the table reader
   */
  public DefaultQueryAssertionProvider(
      final DataSetComparator comparator, final TableReader tableReader) {
    this.comparator = comparator;
    this.tableReader = tableReader;
  }

  @Override
  public void assertEqualsByQuery(
      final TableSet expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
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
