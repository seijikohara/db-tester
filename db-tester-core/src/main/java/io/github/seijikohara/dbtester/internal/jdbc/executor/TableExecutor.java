package io.github.seijikohara.dbtester.internal.jdbc.executor;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.util.List;

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
 *   <li>{@link RefreshExecutor} - REFRESH (upsert) operations
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
}
