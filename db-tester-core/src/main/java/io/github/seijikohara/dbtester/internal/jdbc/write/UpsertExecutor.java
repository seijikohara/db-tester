package io.github.seijikohara.dbtester.internal.jdbc.write;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes UPSERT operations on database tables.
 *
 * <p>This class implements {@link TableExecutor} and provides methods to perform upsert operations
 * - attempting to update first, then inserting if no rows were affected. The first column in each
 * table definition is treated as the primary key for the {@code WHERE} clause of the update
 * statement.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class UpsertExecutor implements TableExecutor {

  /** Logger for this class. */
  private static final Logger logger = LoggerFactory.getLogger(UpsertExecutor.class);

  /** The insert executor for insert operations. */
  private final InsertExecutor insertExecutor;

  /** The update executor for update operations. */
  private final UpdateExecutor updateExecutor;

  /**
   * Creates a new upsert executor with the specified dependencies.
   *
   * @param insertExecutor the insert executor
   * @param updateExecutor the update executor
   */
  public UpsertExecutor(final InsertExecutor insertExecutor, final UpdateExecutor updateExecutor) {
    this.insertExecutor = insertExecutor;
    this.updateExecutor = updateExecutor;
  }

  @Override
  public void execute(final List<Table> tables, final Connection connection) {
    tables.forEach(table -> upsertTable(table, connection, null));
  }

  @Override
  public void execute(
      final List<Table> tables,
      final Connection connection,
      final @Nullable Duration queryTimeout) {
    tables.forEach(table -> upsertTable(table, connection, queryTimeout));
  }

  /**
   * Upserts all rows in a table.
   *
   * <p>For each row, attempts to update first. If no rows were affected, inserts the row. The first
   * column in the table definition is treated as the primary key for the {@code UPDATE ... WHERE}
   * clause. All remaining columns are included in the {@code SET} clause. Dataset files (CSV, JSON,
   * YAML) must define the primary key column as the first column.
   *
   * @param table the table to upsert
   * @param connection the database connection
   * @param queryTimeout the query timeout, or null for no timeout
   * @throws DatabaseOperationException if a database error occurs
   */
  private void upsertTable(
      final Table table, final Connection connection, final @Nullable Duration queryTimeout) {
    if (table.rows().isEmpty() || table.columns().isEmpty()) {
      return;
    }

    final var columns = table.columns();
    final var primaryKeyColumn = columns.getFirst();
    final var updateColumns = columns.subList(1, columns.size());

    table
        .rows()
        .forEach(
            row -> {
              final var updated =
                  updateExecutor.tryUpdateRow(
                      table.name().value(),
                      primaryKeyColumn,
                      updateColumns,
                      row,
                      connection,
                      queryTimeout);
              if (!updated) {
                logger.trace("Update affected no rows, inserting into {}", table.name().value());
                insertExecutor.insertRow(table, row, connection, queryTimeout);
              }
            });
  }
}
