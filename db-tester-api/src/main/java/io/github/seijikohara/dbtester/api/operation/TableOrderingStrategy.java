package io.github.seijikohara.dbtester.api.operation;

/**
 * Strategy for determining the order in which tables are processed during database operations.
 *
 * <p>This enum defines how the framework should determine the table processing order. The strategy
 * affects both data insertion and deletion operations to ensure foreign key constraints are
 * satisfied.
 *
 * @see io.github.seijikohara.dbtester.api.annotation.DataSet
 * @see io.github.seijikohara.dbtester.api.annotation.ExpectedDataSet
 */
public enum TableOrderingStrategy {

  /**
   * Automatically determines the best ordering strategy.
   *
   * <p>The framework attempts strategies in the following order:
   *
   * <ol>
   *   <li>{@link #LOAD_ORDER_FILE} - Uses {@code load-order.txt} if present in the dataset
   *       directory
   *   <li>{@link #FOREIGN_KEY} - Resolves order based on foreign key relationships via JDBC
   *       metadata
   *   <li>{@link #ALPHABETICAL} - Falls back to alphabetical ordering by table name
   * </ol>
   *
   * <p>This is the default strategy and provides the most flexible behavior.
   */
  AUTO,

  /**
   * Uses the order specified in the {@code load-order.txt} file.
   *
   * <p>The file must exist in the dataset directory. Each line contains one table name. Empty lines
   * and lines starting with '#' are ignored.
   *
   * <p>If the file does not exist, an exception is thrown.
   */
  LOAD_ORDER_FILE,

  /**
   * Determines order based on foreign key relationships.
   *
   * <p>Uses JDBC database metadata to analyze foreign key dependencies and performs a topological
   * sort. Parent tables (those referenced by foreign keys) are processed before child tables.
   *
   * <p>If foreign key metadata cannot be retrieved or circular dependencies are detected, falls
   * back to the original table order.
   */
  FOREIGN_KEY,

  /**
   * Orders tables alphabetically by name.
   *
   * <p>Tables are sorted in ascending alphabetical order (case-insensitive). This provides a
   * deterministic ordering but does not consider foreign key constraints.
   */
  ALPHABETICAL
}
