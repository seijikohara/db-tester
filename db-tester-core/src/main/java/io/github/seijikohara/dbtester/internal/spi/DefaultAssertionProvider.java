package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.api.assertion.AssertionFailureHandler;
import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.spi.AssertionProvider;
import io.github.seijikohara.dbtester.internal.assertion.DataSetComparator;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;
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

  /** Creates a new instance with default comparator. */
  public DefaultAssertionProvider() {
    this(OperationDefaults.standard());
  }

  /**
   * Creates a new instance with specified operation defaults.
   *
   * @param operationDefaults the operation defaults to use
   */
  public DefaultAssertionProvider(final OperationDefaults operationDefaults) {
    this(new DataSetComparator(operationDefaults));
  }

  /**
   * Creates a new instance with specified comparator.
   *
   * @param comparator the dataset comparator
   */
  public DefaultAssertionProvider(final DataSetComparator comparator) {
    this.comparator = comparator;
  }

  @Override
  public void assertEquals(final TableSet expected, final TableSet actual) {
    comparator.assertEquals(expected, actual, null);
  }

  @Override
  public void assertEquals(
      final TableSet expected,
      final TableSet actual,
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
      final TableSet expected,
      final TableSet actual,
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
  public void assertEqualsWithStrategies(
      final Table expected,
      final Table actual,
      final Collection<ColumnStrategyMapping> columnStrategies) {
    // Convert collection to map keyed by uppercase column name
    final var strategyMap =
        columnStrategies.stream()
            .collect(
                Collectors.toMap(
                    mapping -> mapping.columnName().toUpperCase(Locale.ROOT),
                    Function.identity(),
                    (existing, replacement) -> replacement));

    // Extract columns marked with IGNORE strategy for the ignore set
    final var ignoreSet =
        columnStrategies.stream()
            .filter(mapping -> mapping.strategy().isIgnore())
            .map(mapping -> mapping.columnName().toUpperCase(Locale.ROOT))
            .collect(Collectors.toSet());

    comparator.assertEqualsWithStrategies(expected, actual, ignoreSet, strategyMap);
  }
}
