package io.github.seijikohara.dbtester.api.config;

/**
 * Defines the strategy for merging tables when multiple {@code @DataSetSource} annotations
 * reference the same table.
 *
 * <p>When multiple {@code @DataSetSource} annotations are specified (either in a single
 * {@code @DataSet} or {@code @ExpectedDataSet}), files for the same table may exist across
 * different datasets. This enum controls how rows from those tables are combined.
 *
 * <p>The default strategy is {@link #UNION_ALL}, which appends all rows from subsequent datasets to
 * the first dataset's rows.
 *
 * <p>Datasets are processed in annotation declaration order.
 *
 * @see ConventionSettings
 * @see io.github.seijikohara.dbtester.api.annotation.DataSetSource
 */
public enum TableMergeStrategy {

  /**
   * Use only the first occurrence of each table.
   *
   * <p>When the same table appears in multiple datasets, only the table from the first dataset (in
   * annotation order) is used. Tables from subsequent datasets are ignored.
   *
   * <p>Example:
   *
   * <pre>
   * DataSet 1: USERS table with rows [A, B]
   * DataSet 2: USERS table with rows [C, D]
   * Result:    USERS table with rows [A, B]
   * </pre>
   */
  FIRST,

  /**
   * Use only the last occurrence of each table.
   *
   * <p>When the same table appears in multiple datasets, only the table from the last dataset (in
   * annotation order) is used. Tables from earlier datasets are replaced.
   *
   * <p>Example:
   *
   * <pre>
   * DataSet 1: USERS table with rows [A, B]
   * DataSet 2: USERS table with rows [C, D]
   * Result:    USERS table with rows [C, D]
   * </pre>
   */
  LAST,

  /**
   * Merge all tables, removing duplicate rows.
   *
   * <p>Similar to SQL UNION. When the same table appears in multiple datasets, all unique rows are
   * combined. Duplicate rows (where all column values match) appear only once in the result.
   *
   * <p>Example:
   *
   * <pre>
   * DataSet 1: USERS table with rows [A, B]
   * DataSet 2: USERS table with rows [B, C]
   * Result:    USERS table with rows [A, B, C]
   * </pre>
   */
  UNION,

  /**
   * Merge all tables, keeping all rows including duplicates.
   *
   * <p>Similar to SQL UNION ALL. When the same table appears in multiple datasets, all rows are
   * appended in order. Duplicate rows are preserved.
   *
   * <p>This is the default strategy.
   *
   * <p>Example:
   *
   * <pre>
   * DataSet 1: USERS table with rows [A, B]
   * DataSet 2: USERS table with rows [B, C]
   * Result:    USERS table with rows [A, B, B, C]
   * </pre>
   */
  UNION_ALL
}
