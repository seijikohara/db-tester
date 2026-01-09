package io.github.seijikohara.dbtester.api.loader;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.Map;
import java.util.Set;

/**
 * Associates a {@link TableSet} with column comparison configuration for expectation verification.
 *
 * <p>This record combines the expected dataset with:
 *
 * <ul>
 *   <li>A set of column names to exclude during verification
 *   <li>A map of column-specific comparison strategies
 * </ul>
 *
 * <p>The exclusion set is derived from both annotation-level exclusions ({@link
 * io.github.seijikohara.dbtester.api.annotation.DataSetSource#excludeColumns()}) and global
 * exclusions ({@link
 * io.github.seijikohara.dbtester.api.config.ConventionSettings#globalExcludeColumns()}).
 *
 * <p>Column strategies are derived from annotation-level strategies ({@link
 * io.github.seijikohara.dbtester.api.annotation.DataSetSource#columnStrategies()}) and global
 * strategies ({@link
 * io.github.seijikohara.dbtester.api.config.ConventionSettings#globalColumnStrategies()}).
 *
 * <p>Column name matching is case-insensitive for both exclusions and strategies.
 *
 * @param tableSet the expected dataset to verify against the database
 * @param excludeColumns column names to exclude from verification (case-insensitive matching)
 * @param columnStrategies column comparison strategies keyed by uppercase column name
 */
public record ExpectedTableSet(
    TableSet tableSet,
    Set<String> excludeColumns,
    Map<String, ColumnStrategyMapping> columnStrategies) {

  /**
   * Creates an ExpectedTableSet with no column exclusions or strategies.
   *
   * @param tableSet the expected dataset
   * @return an ExpectedTableSet with empty exclusion set and empty strategy map
   */
  public static ExpectedTableSet of(final TableSet tableSet) {
    return new ExpectedTableSet(tableSet, Set.of(), Map.of());
  }

  /**
   * Creates an ExpectedTableSet with specified column exclusions and no strategies.
   *
   * @param tableSet the expected dataset
   * @param excludeColumns column names to exclude from verification
   * @return an ExpectedTableSet with the specified exclusions and empty strategy map
   */
  public static ExpectedTableSet of(final TableSet tableSet, final Set<String> excludeColumns) {
    return new ExpectedTableSet(tableSet, Set.copyOf(excludeColumns), Map.of());
  }

  /**
   * Creates an ExpectedTableSet with specified column exclusions and strategies.
   *
   * @param tableSet the expected dataset
   * @param excludeColumns column names to exclude from verification
   * @param columnStrategies column comparison strategies
   * @return an ExpectedTableSet with the specified exclusions and strategies
   */
  public static ExpectedTableSet of(
      final TableSet tableSet,
      final Set<String> excludeColumns,
      final Map<String, ColumnStrategyMapping> columnStrategies) {
    return new ExpectedTableSet(tableSet, Set.copyOf(excludeColumns), Map.copyOf(columnStrategies));
  }

  /**
   * Checks if there are any columns to exclude.
   *
   * @return true if the exclusion set is non-empty
   */
  public boolean hasExclusions() {
    return !excludeColumns.isEmpty();
  }

  /**
   * Checks if there are any column-specific comparison strategies.
   *
   * @return true if the strategy map is non-empty
   */
  public boolean hasColumnStrategies() {
    return !columnStrategies.isEmpty();
  }
}
