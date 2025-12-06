package io.github.seijikohara.dbtester.internal.bridge;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import io.github.seijikohara.dbtester.internal.dataset.DataSet;
import io.github.seijikohara.dbtester.internal.dataset.Table;
import io.github.seijikohara.dbtester.internal.dbunit.DbUnitOperations;
import io.github.seijikohara.dbtester.internal.spi.DatabaseBridgeProvider;
import java.util.Collection;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Implementation of {@link DatabaseBridgeProvider} that delegates to DbUnit for database
 * operations.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} and provides the bridge between the
 * public API and the DbUnit implementation.
 */
public final class DatabaseBridgeProviderImpl implements DatabaseBridgeProvider {

  /** The comparator for dataset assertions. */
  private final DataSetComparator comparator;

  /** The DbUnit operations bridge. */
  private final DbUnitOperations dbUnitOperations;

  /** Creates a new instance with default comparator and operations. */
  public DatabaseBridgeProviderImpl() {
    this.comparator = new DataSetComparator();
    this.dbUnitOperations = new DbUnitOperations();
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
    dbUnitOperations.assertEqualsByQuery(
        expected, dataSource, sqlQuery, tableName, ignoreColumnNames);
  }

  @Override
  public void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    dbUnitOperations.assertEqualsByQuery(
        expected, dataSource, tableName, sqlQuery, ignoreColumnNames);
  }
}
