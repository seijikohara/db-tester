package io.github.seijikohara.dbtester.api.config;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Encapsulates optional verification parameters for expectation verification.
 *
 * <p>This record replaces the telescoping method overloads in {@link
 * io.github.seijikohara.dbtester.api.spi.ExpectationProvider} by bundling all optional parameters
 * into a single immutable value object. Use {@link #defaults()} to obtain an instance with default
 * values, and {@code with*()} methods to customize individual parameters.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Default context (no exclusions, no strategies, ordered comparison)
 * var context = ExpectationContext.defaults();
 *
 * // Custom context with excluded columns and unordered comparison
 * var context = ExpectationContext.defaults()
 *     .withExcludeColumns(Set.of("CREATED_AT", "UPDATED_AT"))
 *     .withRowOrdering(RowOrdering.UNORDERED);
 *
 * // Custom context with column strategies
 * var context = ExpectationContext.defaults()
 *     .withColumnStrategies(Map.of(
 *         "EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL")));
 * }</pre>
 *
 * @param excludeColumns column names to exclude from comparison (case-insensitive matching)
 * @param columnStrategies column comparison strategies keyed by uppercase column name
 * @param rowOrdering the row comparison strategy (ORDERED or UNORDERED)
 * @param operationDefaults the operation defaults containing comparison settings
 * @see io.github.seijikohara.dbtester.api.spi.ExpectationProvider
 */
public record ExpectationContext(
    Set<String> excludeColumns,
    Map<String, ColumnStrategyMapping> columnStrategies,
    RowOrdering rowOrdering,
    OperationDefaults operationDefaults) {

  /**
   * Creates an ExpectationContext with defensive copies of collections.
   *
   * @param excludeColumns column names to exclude from comparison
   * @param columnStrategies column comparison strategies keyed by uppercase column name
   * @param rowOrdering the row comparison strategy
   * @param operationDefaults the operation defaults containing comparison settings
   */
  public ExpectationContext {
    excludeColumns = Set.copyOf(excludeColumns);
    columnStrategies = Map.copyOf(columnStrategies);
  }

  /**
   * Returns an instance with default values.
   *
   * <p>The default context has no excluded columns, no column strategies, ordered row comparison,
   * and standard operation defaults.
   *
   * @return a new ExpectationContext with default values
   */
  public static ExpectationContext defaults() {
    return new ExpectationContext(
        Set.of(), Map.of(), RowOrdering.ORDERED, OperationDefaults.standard());
  }

  /**
   * Creates a new ExpectationContext from the provided parameters.
   *
   * <p>This factory method accepts {@link Collection} for exclude columns and converts it to an
   * immutable {@link Set} internally.
   *
   * @param excludeColumns column names to exclude from comparison
   * @param columnStrategies column comparison strategies keyed by uppercase column name
   * @param rowOrdering the row comparison strategy
   * @param operationDefaults the operation defaults containing comparison settings
   * @return a new ExpectationContext
   */
  public static ExpectationContext of(
      final Collection<String> excludeColumns,
      final Map<String, ColumnStrategyMapping> columnStrategies,
      final RowOrdering rowOrdering,
      final OperationDefaults operationDefaults) {
    return new ExpectationContext(
        Set.copyOf(excludeColumns), columnStrategies, rowOrdering, operationDefaults);
  }

  /**
   * Returns a new ExpectationContext with the specified exclude columns.
   *
   * @param excludeColumns column names to exclude from comparison
   * @return a new ExpectationContext with the specified exclude columns
   */
  public ExpectationContext withExcludeColumns(final Collection<String> excludeColumns) {
    return new ExpectationContext(
        Set.copyOf(excludeColumns),
        this.columnStrategies,
        this.rowOrdering,
        this.operationDefaults);
  }

  /**
   * Returns a new ExpectationContext with the specified column strategies.
   *
   * @param columnStrategies column comparison strategies keyed by uppercase column name
   * @return a new ExpectationContext with the specified column strategies
   */
  public ExpectationContext withColumnStrategies(
      final Map<String, ColumnStrategyMapping> columnStrategies) {
    return new ExpectationContext(
        this.excludeColumns, columnStrategies, this.rowOrdering, this.operationDefaults);
  }

  /**
   * Returns a new ExpectationContext with the specified row ordering.
   *
   * @param rowOrdering the row comparison strategy
   * @return a new ExpectationContext with the specified row ordering
   */
  public ExpectationContext withRowOrdering(final RowOrdering rowOrdering) {
    return new ExpectationContext(
        this.excludeColumns, this.columnStrategies, rowOrdering, this.operationDefaults);
  }

  /**
   * Returns a new ExpectationContext with the specified operation defaults.
   *
   * @param operationDefaults the operation defaults containing comparison settings
   * @return a new ExpectationContext with the specified operation defaults
   */
  public ExpectationContext withOperationDefaults(final OperationDefaults operationDefaults) {
    return new ExpectationContext(
        this.excludeColumns, this.columnStrategies, this.rowOrdering, operationDefaults);
  }
}
