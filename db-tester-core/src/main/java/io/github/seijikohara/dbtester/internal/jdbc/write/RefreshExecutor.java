package io.github.seijikohara.dbtester.internal.jdbc.write;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes UPSERT operations on database tables.
 *
 * <p>This class implements {@link TableExecutor} and provides methods to perform upsert operations
 * - attempting to update first, then inserting if no rows were affected.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class RefreshExecutor implements TableExecutor {

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

  @Override
  public void execute(final List<Table> tables, final Connection connection) {
    tables.forEach(table -> refreshTable(table, connection));
  }

  /**
   * Refreshes (upserts) all rows in a table.
   *
   * <p>For each row, attempts to update first. If no rows were affected, inserts the row.
   *
   * @param table the table to refresh
   * @param connection the database connection
   * @throws DatabaseOperationException if a database error occurs
   */
  private void refreshTable(final Table table, final Connection connection) {
    if (table.getRows().isEmpty() || table.getColumns().isEmpty()) {
      return;
    }

    final var columns = table.getColumns();
    final var primaryKeyColumn = columns.getFirst();
    final var updateColumns = columns.subList(1, columns.size());

    table
        .getRows()
        .forEach(
            row -> {
              final var updated =
                  updateExecutor.tryUpdateRow(
                      table.getName().value(), primaryKeyColumn, updateColumns, row, connection);
              if (!updated) {
                logger.trace("Update affected no rows, inserting into {}", table.getName().value());
                insertExecutor.insertRow(table, row, connection);
              }
            });
  }
}
