package io.github.seijikohara.dbtester.internal.jdbc;

import io.github.seijikohara.dbtester.api.dataset.Table;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes REFRESH (upsert) operations on database tables.
 *
 * <p>This class provides methods to perform upsert operations - attempting to update first, then
 * inserting if no rows were affected.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class RefreshExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(RefreshExecutor.class);

  /** The insert executor for insert operations. */
  private final InsertExecutor insertExecutor;

  /** The update executor for update operations. */
  private final UpdateExecutor updateExecutor;

  /**
   * Creates a new refresh executor with the specified dependencies.
   *
   * @param insertExecutor the insert executor
   * @param updateExecutor the update executor
   */
  public RefreshExecutor(final InsertExecutor insertExecutor, final UpdateExecutor updateExecutor) {
    this.insertExecutor = insertExecutor;
    this.updateExecutor = updateExecutor;
  }

  /**
   * Executes REFRESH (upsert) operations for all tables.
   *
   * @param tables the tables to refresh
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void execute(final List<Table> tables, final Connection connection) throws SQLException {
    for (final var table : tables) {
      refreshTable(table, connection);
    }
  }

  /**
   * Refreshes (upserts) all rows in a table.
   *
   * <p>For each row, attempts to update first. If no rows were affected, inserts the row.
   *
   * @param table the table to refresh
   * @param connection the database connection
   * @throws SQLException if a database error occurs
   */
  public void refreshTable(final Table table, final Connection connection) throws SQLException {
    if (table.getRows().isEmpty() || table.getColumns().isEmpty()) {
      return;
    }

    final var columns = table.getColumns();
    final var pkColumn = columns.getFirst();
    final var updateColumns = columns.subList(1, columns.size());

    for (final var row : table.getRows()) {
      final var updated =
          updateExecutor.tryUpdateRow(
              table.getName().value(), pkColumn, updateColumns, row, connection);
      if (!updated) {
        logger.trace("Update affected no rows, inserting into {}", table.getName().value());
        insertExecutor.insertRow(table, row, connection);
      }
    }
  }
}
