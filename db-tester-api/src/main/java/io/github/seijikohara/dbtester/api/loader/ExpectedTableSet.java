package io.github.seijikohara.dbtester.api.loader;

import io.github.seijikohara.dbtester.api.dataset.TableSet;
import java.util.Set;

/**
 * Associates a {@link TableSet} with column exclusion metadata for expectation verification.
 *
 * <p>This record combines the expected dataset with a set of column names to exclude during
 * verification. The exclusion set is derived from both annotation-level exclusions ({@link
 * io.github.seijikohara.dbtester.api.annotation.DataSetSource#excludeColumns()}) and global
 * exclusions ({@link
 * io.github.seijikohara.dbtester.api.config.ConventionSettings#globalExcludeColumns()}).
 *
 * <p>Column name matching during exclusion is case-insensitive.
 *
 * @param tableSet the expected dataset to verify against the database
 * @param excludeColumns column names to exclude from verification (case-insensitive matching)
 */
public record ExpectedTableSet(TableSet tableSet, Set<String> excludeColumns) {

  /**
   * Creates an ExpectedTableSet with no column exclusions.
   *
   * @param tableSet the expected dataset
   * @return an ExpectedTableSet with an empty exclusion set
   */
  public static ExpectedTableSet of(final TableSet tableSet) {
    return new ExpectedTableSet(tableSet, Set.of());
  }

  /**
   * Creates an ExpectedTableSet with specified column exclusions.
   *
   * @param tableSet the expected dataset
   * @param excludeColumns column names to exclude from verification
   * @return an ExpectedTableSet with the specified exclusions
   */
  public static ExpectedTableSet of(final TableSet tableSet, final Set<String> excludeColumns) {
    return new ExpectedTableSet(tableSet, Set.copyOf(excludeColumns));
  }

  /**
   * Checks if there are any columns to exclude.
   *
   * @return true if the exclusion set is non-empty
   */
  public boolean hasExclusions() {
    return !excludeColumns.isEmpty();
  }
}
