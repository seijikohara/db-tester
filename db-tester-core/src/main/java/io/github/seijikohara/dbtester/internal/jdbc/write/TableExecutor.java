package io.github.seijikohara.dbtester.internal.jdbc.write;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * Defines the contract for table-level database operations.
 *
 * <p>Implementations execute a specific database operation (INSERT, UPDATE, DELETE, etc.) on a list
 * of tables using the provided connection.
 *
 * <p>This is a sealed interface with a fixed set of implementations:
 *
 * <ul>
 *   <li>{@link InsertExecutor} - INSERT operations
 *   <li>{@link UpdateExecutor} - UPDATE operations
 *   <li>{@link DeleteExecutor} - DELETE operations
 *   <li>{@link TruncateExecutor} - TRUNCATE operations
 *   <li>{@link RefreshExecutor} - UPSERT operations
 * </ul>
 *
 * <p>All implementations must be stateless and thread-safe.
 */
public sealed interface TableExecutor
    permits InsertExecutor, UpdateExecutor, DeleteExecutor, TruncateExecutor, RefreshExecutor {

  /**
   * Executes the operation on the given tables.
   *
   * @param tables the tables to operate on
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  void execute(List<Table> tables, Connection connection);

  /**
   * Executes the operation on the given tables with a query timeout.
   *
   * <p>The default implementation ignores the timeout and delegates to {@link #execute(List,
   * Connection)}. Subclasses should override this method to implement timeout support.
   *
   * @param tables the tables to operate on
   * @param connection the database connection
   * @param queryTimeout the query timeout, or null for no timeout
   * @throws DatabaseOperationException if a database error occurs
   */
  default void execute(
      final List<Table> tables,
      final Connection connection,
      final @Nullable Duration queryTimeout) {
    execute(tables, connection);
  }
}
